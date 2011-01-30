/**
 * Copyright (C) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.multimap.btree;


import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.freshbourne.comparator.IntegerComparator;
import com.freshbourne.io.DataPageManager;
import com.freshbourne.io.DynamicDataPage;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

public class LeafNodeSpec {
	
	private final static Injector injector;
	private LeafNode<Integer, String> leaf;
	private LeafPageManager<Integer, String> lpm;
	
	private int key1 = 1;
	private int key2 = 2;
	
	private String value1 = "val1";
	private String value2 = "value2";
	
	
	static {
		injector = Guice.createInjector(new BTreeModule("/tmp/leaf_spec"));
	}
	
	@Before public void setUp(){
		lpm = injector.getInstance(Key.get(new TypeLiteral<LeafPageManager<Integer, String>>(){}));
		leaf = lpm.createPage();
		}
	
	@Test public void shouldBeAbleToInsertAndGet(){
		leaf.insert(key1, value1);
		assertTrue(leaf.containsKey(key1));
		assertEquals(1, leaf.getNumberOfEntries());
		assertEquals(1, leaf.get(key1).size());
		assertEquals(value1, leaf.get(key1).get(0));
	}
	
	@Test public void shouldBeAbleToGetLastKeyAndPointer(){
		leaf.insert(key1, value1);
		assertNotNull(leaf.getLastKey());
		assertNotNull(leaf.getLastKeyPointer());
		
		leaf.insert(key2, value2);
		assertNotNull(leaf.getLastKey());
		assertNotNull(leaf.getLastKeyPointer());
	}
	
	@Test public void shouldAlwaysWorkAfterReload(){
		for(int i = 0; i < 5; i++){
			leaf.insert(key1, value1);
		}
		leaf.insert(key2, value2);
		assertEquals(6, leaf.getNumberOfEntries());
		leaf.load();
		assertEquals(6, leaf.getNumberOfEntries());
		assertEquals(1, leaf.get(key2).size());
		
	}
	
	@Test public void shouldAtSomePointReturnAValidAdjustmentAction(){
		AdjustmentAction<Integer, String> action;
		do{
			action = leaf.insert(key1, value1);
		} while(action == null);
		
		DataPageManager<Integer> keyPageManager = injector.getInstance(Key.get(new TypeLiteral<DataPageManager<Integer>>(){}));
		
		assertNotNull(leaf.getLastKey());
		assertEquals(AdjustmentAction.ACTION.INSERT_NEW_NODE, action.getAction());
		
		assertNotNull(action.getKeyPointer());
		assertNotNull(action.getKeyPointer().getOffset());
		assertNotNull(action.getKeyPointer().getId());
		
		assertNotNull(keyPageManager.getPage(action.getKeyPointer().getId()));
		assertNotNull(keyPageManager.getPage(action.getKeyPointer().getId()).get(action.getKeyPointer().getOffset()));
		
		// this should still work and not throw an exception
		Integer k = leaf.getLastKey();
		assertNotNull(leaf.get(k));
		
		// same for the newly create leaf
		leaf = lpm.getPage(action.getPageId());
		k = leaf.getLastKey();
		assertNotNull(leaf.get(k));
		
	}
	
	@Test
	public void shouldContainAddedEntries() {
		leaf.insert(key1, value1);
		assertTrue(leaf.containsKey(key1));
		assertEquals(1, leaf.get(key1).size());
		assertEquals(value1, leaf.get(key1).get(0));
		assertEquals(1, leaf.getNumberOfEntries());
		
		leaf.insert(key1, value2);
		assertTrue(leaf.containsKey(key1));
		assertEquals(2, leaf.get(key1).size());
		assertEquals(value1, leaf.get(key1).get(0));
		assertEquals(value2, leaf.get(key1).get(1));
		assertEquals(2, leaf.getNumberOfEntries());
		
		leaf.insert(key2, value2);
		assertTrue(leaf.containsKey(key2));
		assertEquals(1, leaf.get(key2).size());
		assertTrue(leaf.get(key1).contains(value2));
		assertTrue(leaf.get(key1).contains(value1));
		assertTrue(leaf.get(key1).size() == 2);
		assertEquals(3, leaf.getNumberOfEntries());
	}
	
	@Test
	public void removeWithValueArgumentShouldRemoveOnlyThisValue() {
		leaf.insert(key1, value1);
		leaf.insert(key1, value2);
		leaf.insert(key2, value2);
		
		assertEquals(3, leaf.getNumberOfEntries());
		leaf.remove(key1, value2);
		assertEquals(1, leaf.get(key1).size());
		assertEquals(value1, leaf.get(key1).get(0));
		assertEquals(value2, leaf.get(key2).get(0));
	}
	
	
	
	

}
