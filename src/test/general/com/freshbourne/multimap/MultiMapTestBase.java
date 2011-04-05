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
	
	private MultiMap<K, V> multiMap;
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
		setMultiMap(getProvider().createNewMultiMap());
			key1 = getProvider().createRandomKey();
		do{
			key2 = getProvider().createRandomKey();
		} while (key2.equals(key1));
		value1 = getProvider().createRandomValue();
		value2 = getProvider().createRandomValue();
	}
	
	protected void simpleTests(){
		int numOfEntries = getMultiMap().getNumberOfEntries();
		
		getMultiMap().add(key1, value2);
		assertTrue(getMultiMap().containsKey(key1));
		assertEquals(value2, getMultiMap().get(key1).get(0));
		assertEquals(numOfEntries + 1, getMultiMap().getNumberOfEntries());
		
		getMultiMap().remove(key1);
		assertFalse(getMultiMap().containsKey(key1));
		assertEquals(0, getMultiMap().get(key1).size());		
		assertEquals(numOfEntries, getMultiMap().getNumberOfEntries());
	}
	
	protected void fill(int size){
		for(int i = 0; i < size; i++){
			getMultiMap().add(getProvider().createRandomKey(), getProvider().createRandomValue());
		}
	}


	/**
	 * @param multiMap the multiMap to set
	 */
	public void setMultiMap(MultiMap<K, V> multiMap) {
		this.multiMap = multiMap;
	}


	/**
	 * @return the multiMap
	 */
	public MultiMap<K, V> getMultiMap() {
		return multiMap;
	}
}
