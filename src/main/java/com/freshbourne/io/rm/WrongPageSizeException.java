/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 *
 * http://creativecommons.org/licenses/by-nc/3.0/
 *
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2011 "Robin Wenglewski <robin@wenglewski.de>"
 */
package com.freshbourne.io.rm;

public class WrongPageSizeException extends IllegalStateException {
	
	private static final long serialVersionUID = 1L;

	WrongPageSizeException(final RawPage p, final int expected){
		super("The Page " + p + " does not have the expected PageSize of " + expected);
	}

}
