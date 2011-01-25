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

public class MultiMapSpec {
	
	private MultiMap<Integer, String> tree;
	private String s = "testString";
	private String s2 = "string 2";
	

	private final static Injector injector;
	
	static {
		injector = Guice.createInjector(
				new IOModule("/tmp/btreetest"),
				new BTreeModule());
	}
	
	@Before
	public void setUp() throws IOException{
		tree = injector.getInstance(Key.get(new TypeLiteral<BTree<Integer,String>>(){}));
	}
	
	
	@Test
	public void shouldBeEmptyAfterCreation(){
		assertEquals(0, tree.getNumberOfEntries());
	}
	
	@Test
	public void shouldContainAddedEntries() throws Exception{
		tree.add(1, s);
		assertTrue(tree.containsKey(1));
		assertEquals(1, tree.get(1).size());
		assertEquals(s, tree.get(1).get(0));
		assertEquals(1, tree.getNumberOfEntries());
		
		tree.add(1, s2);
		assertTrue(tree.containsKey(1));
		assertEquals(2, tree.get(1).size());
		assertEquals(s, tree.get(1).get(0));
		assertEquals(s2, tree.get(1).get(1));
		assertEquals(2, tree.getNumberOfEntries());
		
		tree.add(2, s2);
		assertTrue(tree.containsKey(2));
		assertEquals(1, tree.get(2).size());
		assertTrue(tree.get(1).contains(s2));
		assertTrue(tree.get(1).contains(s));
		assertTrue(tree.get(1).size() == 2);
		assertEquals(3, tree.getNumberOfEntries());
	}
	
	@Test
	public void shouldReturnEmptyArrayIfKeyNotFound() throws IOException, Exception{
		assertEquals(0, tree.get(1).size());
	}
	
	@Test
	public void shouldBeAbleToRemoveInsertedEntries() throws Exception{
		tree.add(1, s);
		assertTrue(tree.containsKey(1));
		tree.remove(1);
		assertFalse(tree.containsKey(1));
		assertEquals(0, tree.getNumberOfEntries());
	}
	
	@Test
	public void clearShouldRemoveAllElements() throws Exception{
		tree.add(1, s);
		tree.add(2, s2);
		assertEquals(2, tree.getNumberOfEntries());
		tree.clear();
		assertEquals(0, tree.getNumberOfEntries());
	}
	
	@Test
	public void removeWithValueArgumentShouldRemoveOnlyThisValue() throws Exception{
		tree.add(1, s);
		tree.add(1, s2);
		tree.add(2, s2);
		
		assertEquals(3, tree.getNumberOfEntries());
		tree.remove(1, s2);
		assertEquals(1, tree.get(1).size());
		assertEquals(s, tree.get(1).get(0));
		assertEquals(s2, tree.get(2).get(0));
		
	}
	
	@Test
	public void removeWithOnlyKeyArgumentShouldRemoveAllValues() throws Exception{
		tree.add(1, s);
		tree.add(1, s2);
		tree.add(2, s2);
		
		assertEquals(3, tree.getNumberOfEntries());
		tree.remove(1);
		assertEquals(1, tree.getNumberOfEntries());
		assertEquals(0, tree.get(1).size());
	}
	
	@Test public void shouldWorkWithANumberOfValues(){
		for(int i = 0; i < 1000; i++){
			System.out.println(i);
			tree.add(i, s);
		}
		assertEquals(1000, tree.getNumberOfEntries());
	}
	
}
