/**
 * Copyright (C) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.io;

import java.io.IOException;

/**
 * Classes should not have too complex logic in the constructor. Instead, they should provide a separate method to open the object.
 * 
 * Classes can implement this interface if they need to be initialized or loaded to be usable.
 * 
 * @author Robin Wenglewski <robin@wenglewski.de>
 *
 */
public interface MustInitializeOrLoad {
	
	/**
	 * uses the underlying byte array to create a fresh valid ComplexPage
	 * @throws IOException 
	 */
	public void initialize() throws IOException;
	
	/**
	 * tries to load an old ComplexPage from the underlying byte array
	 * @throws IOException 
	 */
	public void load() throws IOException;
	
	/**
	 * @return if the page has been initialized or loaded
	 */
	public boolean isValid();
}
