/**
 * Copyright (C) 2010 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.io;

import java.io.IOException;

/**
 * Classes implementing this interface provide controlled access to pages.
 * The pages are usually cached by the implementing class.
 * 
 * @author Robin Wenglewski <robin@wenglewski.de>
 *
 */
public interface BufferPoolManager {
	/**
	 * creates an valid in-memory HashPage for the cases where it is unsure whether
	 * the page should be persisted or not. However, the Page is not added to the 
	 * cache and unless writePage is called discarded.
	 * 
	 * The id of a new page is 0
	 * 
	 * @return new in-memory Page
	 */
	public HashPage newPage();
	
	
	/**
	 * creates a new valid Page whith a valid id for which space has been reserved in
	 * the resource.
	 * 
	 * @return HashPage
	 * @throws IOException
	 */
	public HashPage createPage() throws IOException;
	
	/**
	 * @param id
	 * @return page with given id from resource or cache
	 */
	public HashPage getPage(int id) throws IOException;
}
