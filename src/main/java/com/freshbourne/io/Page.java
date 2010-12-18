/**
 * Copyright (C) 2010 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.io;

import java.nio.ByteBuffer;

/**
 * Every class implementing thins interface wraps around a <code>byte[]</code>.
 * 
 * ByteBuffer is used instead of <code>byte[]</code> since it is not possible to slice a byte
 * array in smaller pieces. Thus, also for example body returns a ByteBuffer over the body,
 * the Array backing the ByteBuffer is still the full page byte array!
 * 
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
	 * @return a readOnly ByteBuffer over the complete <code>byte[]</code> underneath this page
	 */
	public ByteBuffer buffer();
	
	/**
	 * @return a ByteBuffer over the<code>byte[]</code> underneath this page without the header.
	 */
	public ByteBuffer body();
	
	/**
	 * @return true if the <code>byte[]</code> is valid (e.g. header is written).
	 */
	public boolean valid();

}
