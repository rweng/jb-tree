/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 *
 * http://creativecommons.org/licenses/by-nc/3.0/
 *
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2011 "Robin Wenglewski <robin@wenglewski.de>"
 */

package com.freshbourne.btree;

import java.util.Iterator;
import java.util.List;

public interface MultiMap<K, V> {
	
	/**
	 * @return number of values
	 */
	public int getNumberOfEntries();
	
	/**
	 * @param key
	 * @return boolean if the key is contained in the map
	 */
	public boolean containsKey(K key);
    
	/**
	 * @param key
	 * @return array of values associated with the key or an empty array if the key does not exist
	 */
	public List<V> get(K key);
	
	
	
    // Modification Operations
    
    /**
     * Adds the specified value to the specified key.
     * 
     * @param key
     * @param value
     * 
     */
    public void add(K key, V value);
    
    /**
     * Removes the key with all its associated values from the map.
     * If the key was not found, an empty array is returned.
     * 
     * @param key
     */
    void remove(K key);
    
    /**
     * Removes the value under key.
     * IF the key or value was not found, null is returned.
     * 
     * @param key
     * @param value
     */
    void remove(K key, V value);
    
    /**
     * removes all keys and values
     */
    void clear();
    
    /**
     * if the MultiMap is backed by some kind of storage, this method forces the synchronization to it
     */
    public void sync();
    
    
    
    /**
     * @return iterator over all values
     */
    public Iterator<V> getIterator();
    
    /**
     * returns an iterator over the values for the keys of the given range
     * 
     * @param from
     * @param to
     * @return
     */
    public Iterator<V> getIterator(K from, K to);
    
}
