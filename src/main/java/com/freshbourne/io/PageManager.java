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
public interface PageManager<T extends Page> {
	/**
	 * creates a new valid Page whith a valid id for which space has been reserved in
	 * the resource.
	 * 
	 * @return HashPage
	 * @throws IOException
	 */
	public T createPage() throws IOException;
	
	/**
	 * @param id
	 * @return page with given id from resource or cache
	 */
	public T getPage(int id) throws IOException;
	
	/**
	 * removes the Page with the given id
	 * @param id
	 */
	public void removePage(int id);
}
