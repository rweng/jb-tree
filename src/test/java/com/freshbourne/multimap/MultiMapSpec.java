/*
 * Copyright (c) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 */

package com.freshbourne.multimap;

import com.freshbourne.io.IOModule;
import com.freshbourne.multimap.MultiMap;
import com.freshbourne.multimap.btree.BTree;
import com.freshbourne.multimap.btree.BTreeModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public abstract class MultiMapSpec<K, V> {
	
	private MultiMap<K, V> tree;
	private K key1;
	private K key2;
	
	private V value1;
	private V value2;
	
	@Before
	public void setUp() throws IOException{
		tree = createMultiMap();
		key1 = createRandomKey();
		key2 = createRandomKey();
		value1 = createRandomValue();
		value2 = createRandomValue();
	}
	
	protected abstract MultiMap<K, V> createMultiMap();
	protected abstract K createRandomKey();
	protected abstract V createRandomValue();
	
	
	@Test
	public void shouldBeEmptyAfterCreation(){
		assertEquals(0, tree.getNumberOfEntries());
	}
	
	@Test
	public void shouldContainAddedEntries() throws Exception{
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
	public void shouldReturnEmptyArrayIfKeyNotFound() throws IOException, Exception{
		assertEquals(0, tree.get(key1).size());
	}
	
	@Test
	public void shouldBeAbleToRemoveInsertedEntries() throws Exception{
		tree.add(key1, value1);
		assertTrue(tree.containsKey(key1));
		tree.remove(key1);
		assertFalse(tree.containsKey(key1));
		assertEquals(0, tree.getNumberOfEntries());
	}
	
	@Test
	public void clearShouldRemoveAllElements() throws Exception{
		tree.add(key1, value1);
		tree.add(key2, value2);
		assertEquals(2, tree.getNumberOfEntries());
		tree.clear();
		assertEquals(0, tree.getNumberOfEntries());
	}
	
	@Test
	public void removeWithValueArgumentShouldRemoveOnlyThisValue() throws Exception{
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
	public void removeWithOnlyKeyArgumentShouldRemoveAllValues() throws Exception{
		tree.add(key1, value1);
		tree.add(key1, value2);
		tree.add(key2, value2);
		
		assertEquals(3, tree.getNumberOfEntries());
		tree.remove(key1);
		assertEquals(1, tree.getNumberOfEntries());
		assertEquals(0, tree.get(key1).size());
	}
	
	@Test public void shouldWorkWithANumberOfValues(){
		for(int i = 0; i < 1000; i++){
			System.out.println(i);
			tree.add(createRandomKey(), value1);
		}
		assertEquals(1000, tree.getNumberOfEntries());
	}
	
}
