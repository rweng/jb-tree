/**
 * Copyright (C) 2010 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.io;

/**
 * Every class implementing this interface wraps around a HashPage body.
 * 
 * @author Robin Wenglewski <robin@wenglewski.de>
 *
 */
public interface Page {
	
	/**
	 * @return the to this page belonging hashPage
	 */
	public HashPage hashPage();

}
