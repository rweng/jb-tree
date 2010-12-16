/**
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 * 
 * (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */
package com.freshbourne.multimap.btree;

public class SerializationToLargeException extends Exception {
	
	private static final long serialVersionUID = 1L;

	/**
	 * @param object which caused the exception
	 */
	public SerializationToLargeException(Object o) {
		super("Serialization too large for Object: " + o.getClass().toString());
	}
}
