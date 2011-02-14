/*
 * Copyright (c) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 */

package com.freshbourne.multimap;

import com.freshbourne.multimap.MultiMap;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public abstract class MultiMapSpec<K, V> {
	
	protected MultiMap<K, V> tree;
	private K key1;
	private K key2;
	
	private V value1;
	private V value2;
	
	@Before
	public void setUp() {
		tree = createMultiMap();
		key1 = createRandomKey();
		key2 = createRandomKey();
		value1 = createRandomValue();
		value2 = createRandomValue();
	}
	
	protected abstract MultiMap<K, V> createMultiMap();
	protected abstract K createRandomKey();
	protected abstract K createMaxKey();
	protected abstract K createMinKey();
	protected abstract V createRandomValue();
	
	
	@Test
	public void shouldBeEmptyAfterCreation(){
		assertEquals(0, tree.getNumberOfEntries());
	}
	
	@Test
	public void shouldContainAddedEntries() {
		tree.add(key1, value1);
		assertTrue(tree.containsKey(key1));
		assertEquals(1, tree.get(key1).size());
		assertEquals(value1, tree.get(key1).get(0));
		assertEquals(1, tree.getNumberOfEntries());
		
		tree.add(key1, value2);
		assertTrue(tree.containsKey(key1));
		assertEquals(2, tree.get(key1).size());
		assertEquals(value1, tree.get(key1).get(0));
		assertEquals(value2, tree.get(key1).get(1));
		assertEquals(2, tree.getNumberOfEntries());
		
		tree.add(key2, value2);
		assertTrue(tree.containsKey(key2));
		assertEquals(1, tree.get(key2).size());
		assertTrue(tree.get(key1).contains(value2));
		assertTrue(tree.get(key1).contains(value1));
		assertTrue(tree.get(key1).size() == 2);
		assertEquals(3, tree.getNumberOfEntries());
	}
	
	@Test
	public void shouldReturnEmptyArrayIfKeyNotFound() {
		assertEquals(0, tree.get(key1).size());
	}
	
	@Test
	public void shouldBeAbleToRemoveInsertedEntries() {
		tree.add(key1, value1);
		assertTrue(tree.containsKey(key1));
		tree.remove(key1);
		assertFalse(tree.containsKey(key1));
		assertEquals(0, tree.getNumberOfEntries());
	}
	
	@Test
	public void clearShouldRemoveAllElements() {
		tree.add(key1, value1);
		tree.add(key2, value2);
		assertEquals(2, tree.getNumberOfEntries());
		tree.clear();
		assertEquals(0, tree.getNumberOfEntries());
	}
	
	@Test
	public void removeWithValueArgumentShouldRemoveOnlyThisValue() {
		tree.add(key1, value1);
		tree.add(key1, value2);
		tree.add(key2, value2);
		
		assertEquals(3, tree.getNumberOfEntries());
		tree.remove(key1, value2);
		assertEquals(1, tree.get(key1).size());
		assertEquals(value1, tree.get(key1).get(0));
		assertEquals(value2, tree.get(key2).get(0));
	}
	
	@Test
	public void removeWithOnlyKeyArgumentShouldRemoveAllValues() {
		tree.add(key1, value1);
		tree.add(key1, value2);
		tree.add(key2, value2);
		
		assertEquals(3, tree.getNumberOfEntries());
		tree.remove(key1);
		assertEquals(1, tree.getNumberOfEntries());
		assertEquals(0, tree.get(key1).size());
	}
	
	protected void fill(int size){
		K key = createRandomKey();
		System.out.println("adding " + size + "values to " + tree.getClass().toString());
		for(int i = 0; i < size; i++){
			System.out.println("inserting value " + i);
			tree.add(createRandomKey(), createRandomValue());
		}
		
	}
	
	@Test public void shouldWorkOnTheEdgeToCreateNewInnerNode(){
		int size = 170;
		fill(size);
		
		assertEquals(size, tree.getNumberOfEntries());
		simpleTests();
	}
	
	@Test
	public void shouldWorkWithMassiveValues(){
		int size = 10000;

		fill(size);
		
		assertEquals(size, tree.getNumberOfEntries());
		key1 = createMaxKey();
		simpleTests();
		key1 = createMinKey();
		simpleTests();
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
	
}
