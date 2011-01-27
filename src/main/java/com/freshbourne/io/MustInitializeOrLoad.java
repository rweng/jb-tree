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
 * This interface is for objects that can be loaded or initialized. Note that initialize should always work.
 * If it could fail due to closed Resources, consider implementing both, this interface and MustBeOpened to
 * ensure that the Resource can be accessed.
 * 
 * Classes can implement this interface if they need to be initialized or loaded to be usable.
 * 
 * @author Robin Wenglewski <robin@wenglewski.de>
 *
 */
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
