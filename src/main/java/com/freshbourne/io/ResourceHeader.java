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
 * HEADER: PAGESIZE | LAST_ID | NUM_OF_FREED_PAGES | FREE_PAGE_1 | FREE_PAGE_
 * 
 * Page Zero is reserved for the header.
 * 
 */
public class ResourceHeader implements MustInitializeOrLoad{
	private int pageSize;
	private FileChannel ioChannel;
	private boolean valid = false;
	private int lastId;
	private ByteBuffer firstPage;
	
	ResourceHeader(FileChannel ioChannel, int pageSize){
		this.ioChannel = ioChannel;
		this.pageSize = pageSize;
		this.firstPage = ByteBuffer.allocate(pageSize);
	}
	

	public int generateId(){
		firstPage.position(Integer.SIZE / 8);
		firstPage.putInt(++lastId);
		firstPage.rewind();
		
		try {
			ioChannel.write(firstPage, 0);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		return lastId;
    }
	
	@Override
	public void load() throws IOException {
		ioChannel.position(0);
		ioChannel.read(firstPage);
		firstPage.position(0);
		
		int ps = firstPage.getInt();
		if(pageSize != ps)
			throw new RuntimeException("index has a different page size");
		
		lastId = firstPage.getInt();
		valid = true;
	}

	public boolean contains(int id){
		return id <= lastId && id != 0;
	}
	
	/** 
	 * 
	 * @return number of pages without page 0
	 */
	public int getNumberOfPages(){
		return lastId;
	}
	
	public int getLastPageId(){
		return lastId;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.MustInitializeOrLoad#initialize()
	 */
	@Override
	public void initialize(){
		try {
			lastId = 0;
				
			firstPage.position(0);
			firstPage.putInt(pageSize);
			firstPage.putInt(lastId);
			firstPage.rewind();
		
			ioChannel.truncate(0);
			ioChannel.position(0);
			ioChannel.write(firstPage);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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
	public Long getPageOffset(int id) {
		if(!contains(id))
			return null;
		
		// first page reserved for header
		return new Long((id) * pageSize);
	}
}
