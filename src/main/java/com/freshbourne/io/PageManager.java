/*
 * Copyright (c) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 */
package com.freshbourne.io;

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
	 * IMPORTANT NOTE: A PageManager should cache so it can return the same Object here.
	 * 
	 * @param id of the page to be fetched
	 * @return page with given id from resource or cache, null if page could not be found
	 */
	public T getPage(int id);
	
	/**
	 * removes the Page with the given id
	 * @param id of the Page to be removed
	 */
	public void removePage(int id);

	/**
	 * @param page id
	 * @return true, if the page exists
	 */
	public boolean hasPage(int id);
	
	/**
	 * forces the sync of pages to resources
	 */
	public void sync();

}
