/**
 * Copyright (C) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.multimap;

import java.util.Iterator;


/**
 * Interface for a filter over a Multimap.
 * 
 * It can provide an iterator over the values
 * 
 * @author Robin Wenglewski <robin@wenglewski.de>
 *
 */
public interface MMFilter {
	public Iterator<?> getIterator();
}
