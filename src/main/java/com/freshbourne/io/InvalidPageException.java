/**
 * Copyright (C) 2010 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.io;

public class InvalidPageException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public InvalidPageException() {
		super("The page is invalid!");
	}
	
	public InvalidPageException(Object p){
		super("Page " + p.toString() + " is invalid!");
	}

}
