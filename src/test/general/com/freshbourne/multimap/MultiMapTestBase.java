/**
 * Copyright (C) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.multimap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;

public class MultiMapTestBase<K,V> {
	
	private MultiMapProvider<K, V> provider;
	
	protected MultiMap<K, V> tree;
	protected K key1;
	protected K key2;
	
	protected V value1;
	protected V value2;
	
	
	protected MultiMapTestBase(MultiMapProvider<K, V> provider){
		this.setProvider(provider);
	}
	
	
	/**
	 * @param provider the provider to set
	 */
	public void setProvider(MultiMapProvider<K, V> provider) {
		if(provider == null)
			throw new IllegalArgumentException("Provider must not be null");
		
		this.provider = provider;
	}

	/**
	 * @return the provider
	 */
	public MultiMapProvider<K, V> getProvider() {
		return provider;
	}
	
	@Before
	public void setUp() {
		tree = getProvider().createMultiMap();
		key1 = getProvider().createRandomKey();
		key2 = getProvider().createRandomKey();
		value1 = getProvider().createRandomValue();
		value2 = getProvider().createRandomValue();
	}
	
	protected void simpleTests(){
		int numOfEntries = tree.getNumberOfEntries();
		
		tree.add(key1, value2);
		assertTrue(tree.containsKey(key1));
		assertEquals(value2, tree.get(key1).get(0));
		assertEquals(numOfEntries + 1, tree.getNumberOfEntries());
		
		tree.remove(key1);
		assertFalse(tree.containsKey(key1));
		assertEquals(0, tree.get(key1).size());		
		assertEquals(numOfEntries, tree.getNumberOfEntries());
	}
	
	protected void fill(int size){
		K key = getProvider().createRandomKey();
		System.out.println("adding " + size + "values to " + tree.getClass().toString());
		for(int i = 0; i < size; i++){
			System.out.println("inserting value " + i);
			tree.add(getProvider().createRandomKey(), getProvider().createRandomValue());
		}
	}
}
