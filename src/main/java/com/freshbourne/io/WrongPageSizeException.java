/**
 * Copyright (C) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.io;

public class WrongPageSizeException extends IllegalStateException {
	
	private static final long serialVersionUID = 1L;

	WrongPageSizeException(RawPage p, int expected){
		super("The Page " + p + " does not have the expected PageSize of " + expected);
	}

}
