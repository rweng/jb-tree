/**
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 * 
 * (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */
package com.freshbourne.multimap.btree;

import com.freshbourne.multimap.MultiMap;

/**
 * Abstract class for all nodes of a B-Tree
 * 
 * @author "Robin Wenglewski <robin@wenglewski.de>"
 *
 */
public abstract class Node<K,V> implements MultiMap<K, V> {
	
	/**
	 * @return the number of values in or under this node
	 */
	public abstract int size();
	
	
	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

}
