/*
 * Copyright (c) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 */

/**
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 * 
 * (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */
package com.freshbourne.multimap.btree;

import com.freshbourne.io.PagePointer;
import com.freshbourne.multimap.MultiMap;

/**
 * Abstract class for all nodes of a B-Tree
 * 
 * @author "Robin Wenglewski <robin@wenglewski.de>"
 *
 */
public interface Node<K, V> extends MultiMap<K, V> {
	/**
	 * replaces the add method for Leafs and InnerNodes
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public AdjustmentAction<K, V> insert(K key, V value);
	
	
	/**
	 * @param pos of the key, can also be e.g. -1, which returns the last key
	 * @return
	 */
	public PagePointer getKeyPointer(int pos);
	
	
	/**
	 * @return id of this node
	 */
	public Long getId();
}
