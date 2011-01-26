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

import java.io.IOException;
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
	public Long getId();
	
	/**
	 * @return number of values
	 */
	public int getNumberOfEntries();
	
	/**
	 * @return boolean if the key is contained in the map
	 */
	public boolean containsKey(K key) throws Exception;
    
	/**
	 * @param key
	 * @return array of values associated with the key or an empty array if the key does not exist
	 * @throws IOException 
	 * @throws Exception 
	 */
	public List<V> get(K key) throws IOException, Exception;
	
	
	
    // Modification Operations
    
    /**
     * Removes the key with all its associated values from the map.
     * If the key was not found, an empty array is returned.
     * 
     * @param key
     * @return number of removed values
     * @throws Exception 
     */
    int remove(K key) throws Exception;
    
    /**
     * Removes the value under key.
     * IF the key or value was not found, null is returned.
     * 
     * @param key
     * @param value
     * @throws Exception 
     */
    void remove(K key, V value) throws Exception;
    
    /**
     * removes all key and values
     * @throws Exception 
     */
    void clear() throws Exception;
    
}
