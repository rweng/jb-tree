/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 *
 * http://creativecommons.org/licenses/by-nc/3.0/
 *
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2011 "Robin Wenglewski <robin@wenglewski.de>"
 */
package com.freshbourne.io.rm;

import com.freshbourne.io.AbstractMustInitializeOrLoad;
import com.google.common.annotations.VisibleForTesting;

import java.io.IOException;
import java.nio.ByteBuffer;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;


/**
 * A ResourceHeader that can be applied to different ResourceManagers.
 * <p/>
 * It organizes the underlying byte[] like this:
 * <p/>
 * HEADER: NEXT_PAGE | NUM_OF_FREE_PAGES | PAGE_SIZE | LAST_ID | TOTAL_NUM_OF_FREE_PAGES | FREE_PAGE_1 | FREE_PAGE_X
 * <p/>
 * Page 0 is reserved for the header.
 * <p/>
 * Whenever the ResourceHeader needs extra space for storing free page ids, it can create a page and set this as
 * nextPage. It is not possible to swap the next header page behind this page since the offset for a page is calculated
 * though the PageId.
 * <p/>
 * The additional Header Pages are structured like this:
 * <p/>
 * NUM_OF_FREE_PAGES | FREE_PAGE_1 | FREE_PAGE_X
 */
public class ResourceHeader extends AbstractMustInitializeOrLoad {
	private boolean valid = false;
	private RawPage         firstPage;
	private ResourceManager resourceManager;
	private Integer         pageSize;
	private int             lastId;
	private RawPage         pageForFreeIds;


	public int getPageSize() {
		return pageSize;
	}

	@VisibleForTesting
	static enum Header {
		NEXT_PAGE(0),
		FREE_PAGES_NUM(1 * Integer.SIZE / 8),
		PAGE_SIZE(2 * Integer.SIZE / 8),
		LAST_ID(3 * Integer.SIZE / 8),
		FREE_PAGES_TOTAL(4 * Integer.SIZE / 8);

		int offset;

		Header(int offset) {
			this.offset = offset;
		}

		static int size() {
			return 5 * Integer.SIZE / 8;
		}
	}


	ResourceHeader(ResourceManager rm, Integer pageSize) {
		checkNotNull(rm);

		this.resourceManager = rm;
		this.pageSize = pageSize;
	}


	/** @return 0 if the ResourceHeader is not valid, otherwise a number > 0 */
	public int generateId() {
		if (valid == false)
			return 0;

		if (getFreePagesNum() == 0) {
			setLastId(getLastId() + 1);
			firstPage.sync();
			return getLastId();
		}

		int result = firstPage.bufferForReading(Header.size() + (getFreePagesNum()-1) * Integer.SIZE / 8).getInt();
		setFreePagesNum(getFreePagesNum() - 1);
		setTotalNumberOfFreePages(getTotalNumberOfFreePages() - 1);
		firstPage.sync();
		
		return result;
	}

	private int getLastId() {
		return firstPage.bufferForReading(Header.LAST_ID.offset).getInt();
	}

	private void setLastId(int id) {
		firstPage.bufferForWriting(Header.LAST_ID.offset).putInt(id);
	}

	@Override
	public void load() throws IOException {
		firstPage = resourceManager.getPage(0);
		pageForFreeIds = firstPage;

		final int ps = firstPage.bufferForReading(Header.PAGE_SIZE.offset).getInt();

		if (pageSize == null)
			pageSize = ps;
		else if (!pageSize.equals(ps))
			throw new RuntimeException("Resource has a different page size");

		valid = true;
	}

	boolean contains(final int id) {
		if (id == 0)
			return true;

		if (id > getLastId())
			return false;

		if (containsFreePageId(id))
			return false;

		int nextId = getNextPageId();

		while (nextId != 0) {
			ResourceHeaderOverflowPage next = new ResourceHeaderOverflowPage(resourceManager.getPage(nextId));

			if (next.containsFreePageId(id))
				return false;

			nextId = next.getNextPageId();
		}

		return true;
	}

	private boolean containsFreePageId(int id) {
		int free = getTotalNumberOfFreePages();
		if (free == 0)
			return false;


		ByteBuffer buffer = firstPage.bufferForReading(Header.size());
		for (int i = 0; i < free; i++) {
			if (buffer.getInt() == id)
				return true;
		}

		return false;
	}

	/** @return number of pages without page or overflow pages */
	int getNumberOfPages() {
		return getLastId() - getTotalNumberOfFreePages() - getNumberOfOverflowPages();
	}

	private int getNumberOfOverflowPages() {
		int next = getNextPageId();
		if (next == 0)
			return 0;

		int num = 0;
		while (next != 0) {
			num++;
			next = new ResourceHeaderOverflowPage(resourceManager.getPage(next)).getNextPageId();
		}

		return num;
	}

	private int getNextPageId() {
		return firstPage.bufferForReading(Header.NEXT_PAGE.offset).getInt();
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.MustInitializeOrLoad#initialize()
	 */
	@Override
	public void initialize() {
		checkState(valid == false, "FileResourceHeader already valid.");
		checkNotNull(pageSize, "pageSize must not be null when initializing the FileResourceManager");
		checkState(pageSize >= Header.size(), "pageSize must be larger than %s byte. It is %s byte", Header.size(), pageSize);

		firstPage = resourceManager.createPage();
		pageForFreeIds = firstPage;

		firstPage.bufferForWriting(Header.PAGE_SIZE.offset).putInt(pageSize);
		firstPage.bufferForWriting(Header.FREE_PAGES_NUM.offset).putInt(0);
		firstPage.bufferForWriting(Header.FREE_PAGES_TOTAL.offset).putInt(0);
		firstPage.bufferForWriting(Header.NEXT_PAGE.offset).putInt(0);
		firstPage.bufferForWriting(Header.LAST_ID.offset).putInt(0);
		firstPage.sync();

		valid = true;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.MustInitializeOrLoad#isValid()
	 */
	@Override
	public boolean isValid() {
		return valid;
	}


	/**
	 * @param id
	 * @return
	 */
	public Long getPageOffset(final int id) {
		if (!contains(id))
			return null;

		// first page reserved for header
		return new Long((id) * pageSize);
	}

	public void removePage(int pageId) {
		addFreePage(pageId);

		setTotalNumberOfFreePages(getTotalNumberOfFreePages() + 1);
		firstPage.sync();
	}

	private int getFreePagesNum() {
		return firstPage.bufferForReading(Header.FREE_PAGES_NUM.offset).getInt();
	}

	private void setFreePagesNum(int num) {
		firstPage.bufferForWriting(Header.FREE_PAGES_NUM.offset).putInt(num);
	}

	private void addFreePage(int pageId) {
		if (getOffsetForFreePageId() + Integer.SIZE / 8 <= pageSize) {
			firstPage.bufferForWriting(getOffsetForFreePageId()).putInt(pageId);
			incrFreePagesNum();
			return;
		}

		throw new UnsupportedOperationException("ResourceHeaderOverflowPages are not supported yet");
	}

	private void incrFreePagesNum() {
		firstPage.bufferForWriting(Header.FREE_PAGES_NUM.offset).putInt(getFreePagesNum() + 1);
	}

	private int getOffsetForFreePageId() {
		int offset = Header.size();
		offset += firstPage.bufferForReading(Header.FREE_PAGES_NUM.offset).getInt() * Integer.SIZE / 8;
		return offset;
	}

	private void setTotalNumberOfFreePages(int i) {
		firstPage.bufferForWriting(Header.FREE_PAGES_TOTAL.offset).putInt(i);
	}

	private int getTotalNumberOfFreePages() {
		return firstPage.bufferForReading(Header.FREE_PAGES_TOTAL.offset).getInt();
	}
}
