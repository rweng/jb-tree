/*
 * Copyright (c) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 */

/**
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 * 
 * (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */
package com.freshbourne.io;

import java.io.IOException;

/**
 * ResourceManager reads and writes RawPage from and to a resource.
 * 
 * @author "Robin Wenglewski <robin@wenglewski.de>"
 *
 */
public interface ResourceManager {
	
	
	/**
	 * write the provided RawPage to the resource.
	 * 
	 * @param page RawPage to write to the Resource. The ResourceManager and Id of the RawPage must be set.
	 * @throws IOException if the page id was not found or the ResourceManager was not equal to this ResourceManager
	 */
	public void writePage(RawPage page);
	
	
	/**
	 * create a new Page in the Resource. The method returns
	 * a new RawPage with the same backing buffer, but eventually with a different id.
	 * 
	 * A new Page must be created since the id is immutable.
	 * 
	 * @param page page to add
     * @return new instance of page with ResourceManager and id set
	 * @throws IOException
	 */
	public RawPage addPage(RawPage page);
	
	/**
	 * read the page with the given id
	 * 
	 * @param pageId of the page to look up
	 * @return Page
	 * @throws IOException
	 */
	public RawPage readPage(long pageId);
	
	/**
	 * @return a new added RawPage
	 * @throws IOException 
	 */
	public RawPage createPage();
	
	
	/**
	 * @param pageId of the page to remove
	 */
	public void removePage(long pageId);
	
	
	/**
	 * @return size of the pages in this resource
	 */
	public int pageSize();
	
	public void open() throws IOException;
	public boolean isOpen();
	public void close() throws IOException;
	
	/**
	 * @return the number of real pages, not header pages
	 */
	public int numberOfPages();
	
}
