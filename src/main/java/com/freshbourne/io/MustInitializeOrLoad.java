/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */
package com.freshbourne.io;

import java.io.IOException;

public interface MustInitializeOrLoad {
	
	/**
	 * create a valid object
	 */
	public void initialize();
	
	/**
	 * @throws IOException
	 */
	public void load() throws IOException;
	
	/**
	 * @return if the object has been initialized or loaded
	 */
	public boolean isValid();
}
