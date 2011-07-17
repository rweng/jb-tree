/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */

ackage com.freshbourne.io;

public class NoSpaceException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	NoSpaceException(){
		super("Not enough space!");
	}
}
