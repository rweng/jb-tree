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
public class ResourceHeader {
	private final List<Long> dictionary = new ArrayList<Long>();
	private int pageSize;
	private FileChannel ioChannel;
	
	ResourceHeader(FileChannel ioChannel, int pageSize){
		this.ioChannel = ioChannel;
		this.pageSize = pageSize;
	}
	
	public void load() throws IOException {
		ByteBuffer buf = ByteBuffer.allocate(pageSize);
		ioChannel.position(0);
		ioChannel.read(buf);
		
		int numberOfpages = buf.getInt();
		for( int i = 1; i < numberOfpages; i++){
			long id = buf.getLong();
			dictionary.add(id);
		}
	}

	public boolean contains(Long id){
		return dictionary.contains(id);
	}
	
	public int getRealPageNr(Long id){
		return dictionary.indexOf(id);
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
	
	/**
	 * before calling this function, you should fetch the last page and write it imidiately after the method returns.
	 * The reason is that the method moves the last Id in the spot of the inserted id
	 * 
	 * @param id of the page to be removed
	 */
	public void remove(Long id){
		if(!contains(id)){
			return;
		}
		
		int pageNr = getRealPageNr(id);
		
		// overwrite the current pageNr with the last index in the dictionary
		dictionary.set(pageNr, dictionary.get(dictionary.size() - 1 ));
		dictionary.remove(dictionary.size() - 1);
	}
	
	public Long getLastPageId(){
		return dictionary.get(dictionary.size() - 1);
	}

	/**
	 * 
	 */
	public void writeToFile() {
		// TODO Auto-generated method stub
		
	}
}
