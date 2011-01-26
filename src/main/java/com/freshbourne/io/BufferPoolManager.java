/*
 * Copyright (c) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 */
package com.freshbourne.io;

public interface BufferPoolManager extends PageManager<RawPage> {
	
	/**
	 * writes all pages in the cache to the resource
	 */
	public void flush();
	
	/**
	 * @return resourceManager backing this BuferPoolManager
	 */
	public ResourceManager getResourceManager();
	
	/**
	 * clears the cache
	 */
	public void clearCache();
	
	
}
