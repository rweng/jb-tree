/*
 * Copyright (c) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 */
package com.freshbourne.io;

import java.io.IOException;

/**
 * Exception thrown if an open Resource is required but the Resource is not open.
 * 
 * @author Robin Wenglewski <robin@wenglewski.de>
 *
 */
public class ResourceNotOpenException extends IllegalStateException{
	
	private static final long serialVersionUID = 1L;
	
	public ResourceNotOpenException(Object s){
		super("Resource not open: " + s.toString());
	}
	public ResourceNotOpenException(){
		super("Resource not open!");
	}

}
