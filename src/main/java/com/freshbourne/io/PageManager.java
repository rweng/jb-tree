/*
 * Copyright (c) 2011 Robin Wenglewski <robin@wenglewski.de>
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
public interface PageManager<T> {
	/**
	 * creates a new valid Page whith a valid id for which space has been reserved in
	 * the resource.
	 * 
	 * @return HashPage
	 */
	public T createPage();
	
	/**
	 * @param id of the page to be fetched
	 * @return page with given id from resource or cache, null if page could not be found
	 */
	public T getPage(long id);
	
	/**
	 * removes the Page with the given id
	 * @param id of the Page to be removed
	 */
	public void removePage(long id);
	
}
