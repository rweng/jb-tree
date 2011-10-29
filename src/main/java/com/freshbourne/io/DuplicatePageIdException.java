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

public class DuplicatePageIdException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	public DuplicatePageIdException(final Long id) {
		super("The page with the id " + id + " does already exist.");
	}

}
