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

import com.freshbourne.io.IOModule;
import com.freshbourne.multimap.MultiMap;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class BTreeSpec {
	
	private MultiMap<Integer, String> tree;
	private String s = "testString";
	private String s2 = "string 2";
	

	private final static Injector injector;
	
	static {
		injector = Guice.createInjector(
				new IOModule("/tmp/test"),
				new BTreeModule());
	}
	
	@Before
	public void setUp() throws IOException{
		tree = injector.getInstance(Key.get(new TypeLiteral<BTree<Integer,String>>(){}));
	}
	
	
	@Test
	public void shouldBeEmptyAfterCreation(){
		assertEquals(0, tree.size());
	}
	
	@Test
	public void shouldContainAddedEntries() throws Exception{
		tree.add(1, s);
		assertTrue(tree.containsKey(1));
		assertEquals(1, tree.get(1).size());
		assertEquals(s, tree.get(1).get(0));
		assertEquals(1, tree.size());
		
		tree.add(1, s2);
		assertTrue(tree.containsKey(1));
		assertEquals(2, tree.get(1).size());
		assertEquals(s, tree.get(1).get(0));
		assertEquals(s2, tree.get(1).get(1));
		assertEquals(2, tree.size());
		
		tree.add(2, s2);
		assertTrue(tree.containsKey(2));
		assertEquals(1, tree.get(2).size());
		assertTrue(tree.get(1).contains(s2));
		assertTrue(tree.get(1).contains(s));
		assertTrue(tree.get(1).size() == 2);
		assertEquals(3, tree.size());
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
		assertEquals(0, tree.size());
	}
	
	@Test
	public void clearShouldRemoveAllElements() throws Exception{
		tree.add(1, s);
		tree.add(2, s2);
		assertEquals(2, tree.size());
		tree.clear();
		assertEquals(0, tree.size());
	}
	
	@Test
	public void getFirstShouldReturnFirstElement(){fail();}
	
	@Test
	public void getShouldReturnAllElements(){fail();}
	
	@Test
	public void removeWithValueArgumentShouldRemoveOnlyThisValue(){fail();}
	
	@Test
	public void removeWithOnlyKeyArgumentShouldRemoveAllValues(){fail();}
	
}
