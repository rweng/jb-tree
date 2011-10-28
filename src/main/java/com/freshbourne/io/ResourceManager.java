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

import java.io.IOException;

/**
 * Provides RawPages from a Resource.
 * It is not ensured that the pages are automatically persisted.
 * To make sure that this is the case, call #writePage().
 *
 * An alternative Interface is {@link AutoSaveResourceManager}.
 */
public interface ResourceManager extends PageManager<RawPage> {
	
	
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
	 * @return size of the pages in this resource
	 */
	public int getPageSize();
	
	public void open() throws IOException;
	public boolean isOpen();
	public void close() throws IOException;
	
	/**
	 * @return the number of real pages, not header pages
	 */
	public int numberOfPages();
	
}
