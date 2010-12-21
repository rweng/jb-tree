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
 * ByteBuffer is used instead of <code>byte[]</code> since it is not possible to slice a byte
 * array in smaller pieces. Thus, also for example body returns a ByteBuffer over the body,
 * the Array backing the ByteBuffer is still the full page byte array!
 * 
 * The Page interface is only for definition.
 * 
 * @author Robin Wenglewski <robin@wenglewski.de>
 *
 */
public interface Page {
	

}
