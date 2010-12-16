/**
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 * 
 * (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */
package com.freshbourne.multimap.btree;

public class NoSpaceException extends Exception {
	
	private static final long serialVersionUID = 1L;

	NoSpaceException(){
		super("No space in this B-Tree Node");
	}
}
