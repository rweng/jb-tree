/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */
package com.freshbourne.multimap;

public interface MultiMapProvider<K,V> {

	MultiMap<K,V> createNewMultiMap();
	
	K createRandomKey();
	V createRandomValue();
	K createMaxKey();
	K createMinKey();
	
}
