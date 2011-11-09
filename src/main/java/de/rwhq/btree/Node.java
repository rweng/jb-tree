/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 *
 * http://creativecommons.org/licenses/by-nc/3.0/
 *
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2011 "Robin Wenglewski <robin@wenglewski.de>"
 */

package de.rwhq.btree;

import de.rwhq.io.rm.PagePointer;

import java.util.Iterator;
import java.util.List;

interface Node<K, V> {
	
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
	 * @return number of keys in a node
	 */
	public int getNumberOfKeys();
	
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

    public byte[] getFirstLeafKeySerialized();
    
    /**
     * @return last key of last leaf
     */
    public K getLastLeafKey();

    public byte[] getLastLeafKeySerialized();
    
    public Iterator<V> getIterator(K from, K to);

    /**
     * @return 1 if the node is a leaf, otherwise the depth of the innernode
     */
    public int getDepth();

    /**
     * @return true if all sub-nodes are in the right order and are valid
     * @throws IllegalStateException
     */
    public void checkStructure() throws IllegalStateException;
}
