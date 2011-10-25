/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 *
 * http://creativecommons.org/licenses/by-nc/3.0/
 *
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2011 "Robin Wenglewski <robin@wenglewski.de>"
 */
package com.freshbourne.multimap;

import com.freshbourne.btree.MultiMap;

import java.io.IOException;

public interface MultiMapProvider<K,V> {

	MultiMap<K,V> createNewMultiMap() throws IOException;
	
	K createRandomKey();
	V createRandomValue();
	K createMaxKey();
	K createMinKey();
	
}
