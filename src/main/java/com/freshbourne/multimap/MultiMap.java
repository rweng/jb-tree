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
 * 
 *  Robin Wenglewski <robin@wenglewski.de>"
 */
package com.freshbourne.multimap;


import java.io.IOException;
import java.util.List;

/**
 * This is an Interface like the <a href="http://download.oracle.com/javase/6/docs/api/java/util/Map.html">Map</a> interface, except that
 * multiple value can be associated with one key.
 * 
 * It does not inherit from Map since in Map there are some methods that are
 * hard to implement and depend on the concrete implementation if they make sense.
 * For example entrySet(): a multi-map could store several key-value entries with 
 * the same key, or one entry with a list as value.
 * 
 * To allow method-chaining, the MultiMap is self-referential, meaning that you
 * must provide the concrete implementation when creating a MultiMap.
 * 
 * @version %I%, %G%
 * 
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 * 
 */
public interface MultiMap<K, V> {
	
	/**
	 * @return number of values
	 */
	public int size();
	
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
     * Adds the specified value to the specified key.
     * 
     * @param key
     * @param value
     * 
     */
    public boolean add(K key, V value);
    
    /**
     * Removes the key with all its associated values from the map.
     * If the key was not found, an empty array is returned.
     * 
     * @param key
     * @throws Exception 
     */
    void remove(K key) throws Exception;
    
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
     * removes all keys and values
     * @throws Exception 
     */
    void clear() throws Exception;
}
