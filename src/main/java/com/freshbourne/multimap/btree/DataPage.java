/**
 * Copyright (C) 2010 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.multimap.btree;

import com.freshbourne.io.Page;

/**
 * Pages implementing this interface use the provided buffer to store data.
 * 
 * @author Robin Wenglewski <robin@wenglewski.de>
 *
 */
public interface DataPage extends Page{
	
	/**
	 * adds some bytes to the underlying body. It is possible that the header
	 * also grows through this process.
	 * 
	 * @param bytes array to be written
	 * @return id of the entry/byte[] within this page
	 */
	public int add(byte[] bytes) throws Exception;
	
	
	/**
	 * @param id within this page
	 * @return byte array with this id
	 */
	public byte[] get(int id) throws Exception;
	
	
	/**
	 * removes the byte array with the given id and truncates the page
	 * @param id of the byte array to be removed
	 * @throws ElementNotFoundException 
	 */
	public void remove(int id) throws ElementNotFoundException;
}
