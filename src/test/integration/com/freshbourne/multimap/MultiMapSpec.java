/*
 * Copyright (c) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 */

package com.freshbourne.multimap;

import org.junit.Test;

import static org.junit.Assert.*;

public abstract class MultiMapSpec<K, V> extends MultiMapTestBase<K,V> {
	
	
	protected MultiMapSpec(MultiMapProvider<K,V> provider) {
		super(provider);
	}

	@Test
	public void shouldBeEmptyAfterCreation(){
		assertEquals(0, getMultiMap().getNumberOfEntries());
	}
	
	@Test
	public void shouldContainAddedEntries() {
		getMultiMap().add(key1, value1);
		assertTrue(getMultiMap().containsKey(key1));
		assertEquals(1, getMultiMap().get(key1).size());
		assertEquals(value1, getMultiMap().get(key1).get(0));
		assertEquals(1, getMultiMap().getNumberOfEntries());
		
		getMultiMap().add(key1, value2);
		assertTrue(getMultiMap().containsKey(key1));
		assertEquals(2, getMultiMap().get(key1).size());
		assertEquals(value1, getMultiMap().get(key1).get(0));
		assertEquals(value2, getMultiMap().get(key1).get(1));
		assertEquals(2, getMultiMap().getNumberOfEntries());
		
		getMultiMap().add(key2, value2);
		assertTrue(getMultiMap().containsKey(key2));
		assertEquals(1, getMultiMap().get(key2).size());
		assertTrue(getMultiMap().get(key1).contains(value2));
		assertTrue(getMultiMap().get(key1).contains(value1));
		assertTrue(getMultiMap().get(key1).size() == 2);
		assertEquals(3, getMultiMap().getNumberOfEntries());
	}
	
	@Test
	public void shouldReturnEmptyArrayIfKeyNotFound() {
		assertEquals(0, getMultiMap().get(key1).size());
	}
	
	@Test
	public void shouldBeAbleToRemoveInsertedEntries() {
		getMultiMap().add(key1, value1);
		assertTrue(getMultiMap().containsKey(key1));
		getMultiMap().remove(key1);
		assertFalse(getMultiMap().containsKey(key1));
		assertEquals(0, getMultiMap().getNumberOfEntries());
	}
	
	@Test
	public void clearShouldRemoveAllElements() {
		getMultiMap().add(key1, value1);
		getMultiMap().add(key2, value2);
		assertEquals(2, getMultiMap().getNumberOfEntries());
		getMultiMap().clear();
		assertEquals(0, getMultiMap().getNumberOfEntries());
	}
	
	@Test
	public void removeWithValueArgumentShouldRemoveOnlyThisValue() {
		getMultiMap().add(key1, value1);
		getMultiMap().add(key1, value2);
		getMultiMap().add(key2, value2);
		
		assertEquals(3, getMultiMap().getNumberOfEntries());
		getMultiMap().remove(key1, value2);
		assertEquals(1, getMultiMap().get(key1).size());
		assertEquals(value1, getMultiMap().get(key1).get(0));
		assertEquals(value2, getMultiMap().get(key2).get(0));
	}
	
	@Test
	public void removeWithOnlyKeyArgumentShouldRemoveAllValues() {
		getMultiMap().add(key1, value1);
		getMultiMap().add(key1, value2);
		getMultiMap().add(key2, value2);
		
		assertEquals(3, getMultiMap().getNumberOfEntries());
		getMultiMap().remove(key1);
		assertEquals(1, getMultiMap().getNumberOfEntries());
		assertEquals(0, getMultiMap().get(key1).size());
	}
	
	@Test public void shouldWorkOnTheEdgeToCreateNewInnerNode(){
		int size = 170;
		fill(size);
		
		assertEquals(size, getMultiMap().getNumberOfEntries());
		simpleTests();
	}
	
}
