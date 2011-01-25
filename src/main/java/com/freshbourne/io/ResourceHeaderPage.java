/**
 * Copyright (C) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.io;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;


/**
 * organizes the underlying byte[] like this:
 * 
 * TOTAL_NUM_OF_PAGES | virtual_page_id 1 | real_offset 1 | virtual_page_id 2 | real_offset 2 | ..
 * 
 * The virtual page ids are kept in order.
 * 
 * An alternative to this design would be to only store the virtual page_id and the real_offset is calculated though the pos * page_size.
 * However, this would force a decision when deleting pages, either to destroy the order by copying the last virtual id and its byte[] in the spot of the deleted page,
 * or to preserve the order on high costs by moving all pages behind the deleted page one page backwards (which essentially means, rewriting everything behind the
 * deleted page since deleting in the middle of a file is not supported in most FS).
 * 
 */
public class ResourceHeaderPage implements ComplexPage {
	private final RawPage rawPage;
	private final List<Long> dictionary = new ArrayList<Long>();
	private boolean valid = false;
	
	ResourceHeaderPage(RawPage rawPage){
		this.rawPage = rawPage;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.ComplexPage#initialize()
	 */
	@Override
	public void initialize() {
		ByteBuffer buf = rawPage.bufferAtZero();
		buf.putInt(1); // 1 pages in this file, this page
		valid = true;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.ComplexPage#load()
	 */
	@Override
	public void load() {
		ByteBuffer buf = rawPage.bufferAtZero();
		int numberOfpages = buf.getInt();
		for( int i = 1; i < numberOfpages; i++){
			long id = buf.getLong();
			dictionary.add(id);
		}
		valid = true;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.ComplexPage#isValid()
	 */
	@Override
	public boolean isValid() {
		return valid;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.ComplexPage#rawPage()
	 */
	@Override
	public RawPage rawPage() {
		return rawPage;
	}
	
	public void add(Long pageId) throws DuplicatePageIdException{
		if(contains(pageId))
			throw new DuplicatePageIdException(pageId);
		
		int offset = offsetOfPageNr(dictionary.size());
		rawPage.buffer().position( offset );
		rawPage.buffer().putLong(pageId);
		dictionary.add(pageId);
		
		rawPage.buffer().position(0);
		rawPage.buffer().putInt(dictionary.size());
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

	/**
	 * sets the location of the virtual id to be pos, meaning that the offset in the File where the page is stored is pageSize * pos
	 * 
	 * @param id
	 * @param pos
	 * @throws DuplicatePageIdException 
	 */
	private void setRealPageNr(Long id, int pos) {
		if(dictionary.contains(id))
			throw new DuplicatePageIdException(id);
		
		dictionary.set(pos, id);
		rawPage().buffer().position( offsetOfPageNr(pos) );
		rawPage().buffer().putLong(id);
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
		rawPage.buffer().position(0);
		rawPage.buffer().putInt(dictionary.size());
		
		
	}
	
	public Long getLastPageId(){
		return dictionary.get(dictionary.size() - 1);
	}
}
