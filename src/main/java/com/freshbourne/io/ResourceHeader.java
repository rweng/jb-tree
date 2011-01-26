/**
 * Copyright (C) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;


/**
 * organizes the underlying byte[] like this:
 * 
 * (int) TOTAL_NUM_OF_PAGES_OVER_ALL_HEADER_PAGES (this only in first headerpage) | (int) NUMBER_OF_ADITIONAL_HEADER_PAGES | virtual_page_id 1 | virtual_page_id 2 | ..
 * 
 * The real offset is calculated. All pages are read into memory when the page is loaded.
 * 
 * This way seems superior compared to keeping only the first page in memory and reading the others over and over again, 
 * and compared to keeping a sort order either in an environment where we also
 * store the realoffset in the header (| virtual_page_id 1 | real_offset 1 | ...) or by rewriting the end of the file on deletes.
 * 
 * The header is completely rewritten when the Resource is closed. This is done at the end of the file (except the first page). Thus, when loading the header, the 
 * header pages at the end of the index can be removed to continue appending real pages.
 * 
 */
public class ResourceHeader implements MustInitializeOrLoad{
	private final List<Long> dictionary = new ArrayList<Long>();
	private int pageSize;
	private FileChannel ioChannel;
	private boolean valid = false;
	
	ResourceHeader(FileChannel ioChannel, int pageSize){
		this.ioChannel = ioChannel;
		this.pageSize = pageSize;
	}
	
	@Override
	public void load() throws IOException {
		ByteBuffer buf = ByteBuffer.allocate(pageSize);
		ioChannel.position(0);
		ioChannel.read(buf);
		buf.rewind();
		
		int numberOfpages = buf.getInt();
		int additionalHeaderPages = buf.getInt();
		int longSize = Long.SIZE / 8;
		while(dictionary.size() < numberOfpages && buf.remaining() >= longSize){
			dictionary.add(buf.getLong());
		}
		
		if(additionalHeaderPages > 0)
			throw new UnsupportedOperationException("reading additional header pages is not yet supported");
		
		valid = true;
	}

	public boolean contains(Long id){
		return dictionary.contains(id);
	}
	
	public int getRealPageNr(Long id){
		return dictionary.indexOf(id) + 1; // skip first header page
	}
	
	public int getNumberOfPages(){
		return dictionary.size();
	}
	
	public void add(Long id){
		dictionary.add(id);
	}

	
	private int offsetOfPageNr(int pos){
		return Integer.SIZE / 8 + (pos * Long.SIZE / 8);
	}
	
	public Long getLastPageId(){
		return dictionary.get(dictionary.size() - 1);
	}

	/**
	 * @throws IOException 
	 */
	public void writeToFile() throws IOException {
		
		
		ByteBuffer firstPageBuf = ByteBuffer.allocate(pageSize);
		firstPageBuf.putInt(0); // number of pages, we fill this value later
		firstPageBuf.putInt(0); // number of additional header pages, we fill this value later
		
		
		
		int longSize = Long.SIZE / 8;
		int additionalHeaderPages = 0;
		
		ByteBuffer nextPage = ByteBuffer.allocate(pageSize);
		ioChannel.position(ioChannel.size());
		for(Long id : dictionary){
			if(firstPageBuf.remaining() >= longSize){
				firstPageBuf.putLong(id);
			} else {
				if(nextPage.remaining() >= longSize){
					nextPage.putLong(id);
				} else {
					nextPage.position(0);
					ioChannel.write(nextPage);
					additionalHeaderPages++;
					nextPage.position(0);
					nextPage.putLong(id);
				}
			}
		}
		
		if(nextPage.position() > 0){
			nextPage.position(0);
			ioChannel.write(nextPage);
			additionalHeaderPages++;
		}
		
		firstPageBuf.position(0);
		firstPageBuf.putInt(dictionary.size());
		firstPageBuf.putInt(additionalHeaderPages);
		firstPageBuf.position(0);
		
		ioChannel.position(0);
		ioChannel.write(firstPageBuf);	
		ioChannel.force(true);
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.MustInitializeOrLoad#initialize()
	 */
	@Override
	public void initialize() throws IOException {
		dictionary.clear();
		ioChannel.truncate(0);
		ioChannel.position(0);
		ioChannel.write(ByteBuffer.allocate(pageSize));
		valid = true;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.MustInitializeOrLoad#isValid()
	 */
	@Override
	public boolean isValid() {
		return valid;
	}

	public long getPageOffset(Long id) {
		return getRealPageNr(id) * pageSize;
	}

	/**
	 * @param pageIdToReplace id to replace
	 * @param replacementId id to put at this position instead
	 */
	public void replaceId(long pageIdToReplace, Long replacementId) {
		dictionary.set(dictionary.indexOf(pageIdToReplace), replacementId);
	}

	
	public void removeLastId() {
		dictionary.remove(dictionary.size() - 1);
	}
}
