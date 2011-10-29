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

/**
 *
 * None of the methods throw an IOException.
 * It is assumed, that the PageManager has an open Resource to which it can write.
 *
 * @param <T>
 */
public interface PageManager<T> {

    /**
	 * creates a new valid Page with a valid id for which space has been reserved in
	 * the resource.
	 * 
	 * @return page
	 */
	public T createPage();
	
	/**
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
	 * @param id of the page
	 * @return true, if the page exists
	 */
	public boolean hasPage(int id);
	
	/**
	 * Forces the sync of pages to resources.
     * If the ResouceManager does not cache, this implementation remains empty.
	 */
	public void sync();

}
