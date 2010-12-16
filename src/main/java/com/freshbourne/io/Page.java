/**
 * Copyright (C) 2010 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.io;

/**
 * Every class implementing thins interface wraps around a <code>byte[]</code>.
 * 
 * @author Robin Wenglewski <robin@wenglewski.de>
 *
 */
public interface Page {
	/**
	 * writes the header to the <code>byte[]</code> and makes the page valid
	 */
	public void initialize();
	
	/**
	 * @return the complete <code>byte[]</code> underneath this page.
	 */
	public byte[] buffer();
	
	/**
	 * @return the <code>byte[]</code> underneath this page without the header.
	 */
	public byte[] body();
	
	/**
	 * @return true if the <code>byte[]</code> is valid (e.g. header is written).
	 */
	public boolean valid();

}
