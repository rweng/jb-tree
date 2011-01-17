/**
 * Copyright (C) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.io;

public interface ComplexPage {
	
	/**
	 * uses the underlying byte array to create a fresh valid ComplexPage
	 */
	public void initialize();
	
	/**
	 * tries to load an old ComplexPage from the underlying byte array
	 */
	public void load();
	
	/**
	 * @return if the page has been initialized or loaded
	 */
	public boolean isValid();
	
	
	/**
	 * @return the underlying RawPage
	 */
	public RawPage rawPage();

}
