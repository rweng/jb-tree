/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 *
 * http://creativecommons.org/licenses/by-nc/3.0/
 *
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2011 "Robin Wenglewski <robin@wenglewski.de>"
 */
package com.freshbourne.io;

import com.google.common.base.Objects;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;

import static com.google.common.base.Preconditions.checkState;


/**
 * Writes Pages to a File. It does not cache, and Pages have to be written back manually or the changes will not be
 * written to disk.
 */
public class FileResourceManager implements ResourceManager {
	private       RandomAccessFile handle;
	private final File             file;
	private final int              pageSize;
	private       FileLock         fileLock;
	private       FileChannel      ioChannel;
	private       ResourceHeader   header;
	private       boolean          doLock;

	private static Log LOG = LogFactory.getLog(FileResourceManager.class);

	FileResourceManager(final ResourceManagerBuilder builder) {
		this.file = builder.getFile();
		this.pageSize = builder.getPageSize();
		this.doLock = builder.useLock();
	}

	/* (non-Javadoc)
		 * @see com.freshbourne.io.ResourceManager#open()
		 */
	@Override
	public void open() throws IOException {
		if (isOpen())
			throw new IllegalStateException("Resource already open");

		// if the file does not exist already
		if (!getFile().exists()) {
			getFile().createNewFile();
		}

		initIOChannel(getFile());
		this.header = new ResourceHeader(ioChannel, pageSize);


		if (handle.length() == 0) {
			header.initialize();
		} else {
			// load header if file existed
			header.load();
		}
	}

	@Override
	public void writePage(final RawPage page) {
		if (LOG.isDebugEnabled())
			LOG.debug("writing page to disk: " + page.id());
		ensureOpen();
		ensurePageExists(page.id());

		final ByteBuffer buffer = page.bufferForReading(0);

		try {
			final long offset = header.getPageOffset(page.id());
			ioChannel.write(buffer, offset);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.PageManager#getPage(long)
	 */
	@Override
	public RawPage getPage(final int pageId) {

		ensureOpen();
		ensurePageExists(pageId);

		final RawPage result;

		final ByteBuffer buf = ByteBuffer.allocate(pageSize);

		try {
			ioChannel.read(buf, header.getPageOffset(pageId));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		result = new RawPage(buf, pageId, this);
		return result;
	}

	/**
	 * @param pageId
	 * @throws PageNotFoundException
	 */
	private void ensurePageExists(final int pageId) {
		if (!header.contains(pageId))
			throw new PageNotFoundException(this, pageId);
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.ResourceManager#close()
	 */
	@Override
	public void close() throws IOException {
		if (header != null) {
			header = null;
		}

		try {
			if (fileLock != null && fileLock.isValid()) {
				fileLock.release();
				fileLock = null;
			}

			if (ioChannel != null) {
				ioChannel.close();
				ioChannel = null;
			}

			if (handle != null) {
				handle.close();
				handle = null;
			}
		} catch (Exception ignored) {
		}
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.ResourceManager#getPageSize()
	 */
	@Override
	public int getPageSize() {
		return pageSize;
	}

	/**
	 * Generic private initializer that takes the random access file and initializes the I/O channel and locks it for
	 * exclusive use by this instance.
	 * <p/>
	 * from minidb
	 *
	 * @param file
	 * 		The random access file representing the index.
	 * @throws IOException
	 * 		Thrown, when the I/O channel could not be opened.
	 */
	private void initIOChannel(final File file)
			throws IOException {
		handle = new RandomAccessFile(file, "rw");

		// Open the channel. If anything fails, make sure we close it again
		try {
			ioChannel = handle.getChannel();
			try {
				if (doLock)
					fileLock = ioChannel.tryLock();
			} catch (OverlappingFileLockException oflex) {
				throw new IOException("Index file locked by other consumer.");
			}
		} catch (Throwable t) {
			// something failed.
			close();

			// propagate the exception
			if (t instanceof IOException) {
				throw (IOException) t;
			} else {
				throw new IOException("An error occured while opening the index: " + t.getMessage());
			}
		}
	}

	@Override
	public boolean isOpen() {
		return !(ioChannel == null || !ioChannel.isOpen());
	}

	/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
	@Override
	public String toString() {
		Objects.ToStringHelper helper = Objects.toStringHelper(this)
				.add("file", getFile().getAbsolutePath())
				.add("isOpen", isOpen())
				.add("pageSize", getPageSize());

		if(isOpen())
			helper.add("numberOfPages", numberOfPages());

		return helper.toString();
	}

	/**
	 * @param page
	 * @throws WrongPageSizeException
	 */
	private void ensureCorrectPageSize(final RawPage page) {
		if (page.bufferForReading(0).limit() != pageSize)
			throw new WrongPageSizeException(page, pageSize);
	}

	private void ensureOpen() {
		if (!isOpen())
			throw new IllegalStateException("Resource is not open: " + toString());

		checkState(file.exists(), "File (%s) has been deleted externally.", getFile().getAbsolutePath());
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.ResourceManager#numberOfPages()
	 */
	@Override
	public int numberOfPages() {
		ensureOpen();
		return header.getNumberOfPages();
	}

	@Override public void clear() {
		ensureOpen();
		// file is already truncated when header initialize is called
		header.initialize();
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.ResourceManager#createPage()
	 */
	@Override
	public RawPage createPage() {
		ensureOpen();

		final ByteBuffer buf = ByteBuffer.allocate(pageSize);
		final RawPage result = new RawPage(buf, header.generateId(), this);

		return result;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.ResourceManager#removePage(long)
	 */
	@Override
	public void removePage(final int pageId) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			close();
		} catch (Exception e) {
			super.finalize();
		}
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.PageManager#hasPage(long)
	 */
	@Override
	public boolean hasPage(final int id) {
		ensureOpen();
		return header.contains(id);
	}

	/** @return the handle */
	public RandomAccessFile getHandle() {
		return handle;
	}

	/** @return the file */
	public File getFile() {
		return file;
	}
}
