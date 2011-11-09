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
	 * @return size of the pages in this resource
	 */
	public Integer getPageSize();
	
	public void open() throws IOException;
	public boolean isOpen();
	public void close() throws IOException;
	
	/**
	 * @return the number of real pages, not header pages
	 */
	public int numberOfPages();

	/**
	 * removes all pages from the ResourceManager
	 */
	public void clear();
}
