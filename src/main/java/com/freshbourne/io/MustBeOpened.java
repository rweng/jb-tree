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

public interface MustBeOpened {
	
	/**
	 * opens a closed Object.
	 * @throws Exception
	 */
	public void open() throws Exception;
	
	/**
	 * @return true if the object is open, false if the object is closed
	 */
	public boolean isOpen();
	
	/**
	 * Closes the Object or throws an Exception (and leaves the resource open). The Exception is thrown to give the programmer the chance to save the data in
	 * the object or to make sure that the Resource is closable. For instance, if an Array is synchronized with a File, close might
	 * throw an Exception if the File is not accessible. The programmer can then either make the file accessible or copy the values
	 * of the array to another place.
	 * 
	 * @throws Exception
	 */
	public void close() throws Exception;

}
