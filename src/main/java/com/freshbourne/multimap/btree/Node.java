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

import java.util.Iterator;
import java.util.List;

import com.freshbourne.io.PagePointer;

/**
 * Abstract class for all nodes of a B-Tree. It does not extend MultiMap anymore since although many methods are similar
 * the use-case is different. MultiMaps should never be full, whereas Nodes can be full. This fact changes the signatures of
 * methods like insert and remove.
 * 
 * TODO: make this interface package-wide?
 * 
 * @author "Robin Wenglewski <robin@wenglewski.de>"
 *
 */
public interface Node<K, V> {
	
	/**
	 * inserts the key and value into the node
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
	public Integer getId();
	
	/**
	 * @return number of values in or under a node
	 */
	public int getNumberOfEntries();
	
	/**
	 * @return number of keys in a node
	 */
	public int getNumberOfKeys();
	
	/**
	 * @return boolean if the key is contained in the map
	 */
	public boolean containsKey(K key);
    
	/**
	 * @param key
	 * @return array of values associated with the key or an empty array if the key does not exist
	 */
	public List<V> get(K key);
	
	/**
	 * @param key
	 * @return first element of get(key)
	 */
	public V getFirst(K key);
	
    // Modification Operations
    
    /**
     * Removes the key with all its associated values from the map.
     * If the key was not found, an empty array is returned.
     * 
     * @param key
     * @return number of removed values
     */
    int remove(K key);
    
    /**
     * Removes the value under key.
     * IF the key or value was not found, null is returned.
     * 
     * Note: This method might use value.equals to determine the values to remove. Make sure this method works correctly.
     * 
     * @param key
     * @param value
     * @return number of removed values
     */
    int remove(K key, V value);
    
    /**
     * removes all key and values, destroying all rawPages with the keyPages, valuePages, leafPages and innerNodePages
     */
    void destroy();
    
    
    /**
     * @return first key of first leaf
     */
    public K getFirstLeafKey();
    
    /**
     * @return last key of last leaf
     */
    public K getLastLeafKey();
    
    public Iterator<V> getIterator(K from, K to);
    
}
