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
import com.freshbourne.io.BufferPoolManager;
import com.freshbourne.io.DataPageManager;
import com.freshbourne.io.DynamicDataPage;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

public class LeafNodeSpec {
	
	private final static Injector injector;
	private LeafNode<Integer, String> leaf;
	
	private int key1 = 1;
	private int key2 = 2;
	
	private String val1 = "val1";
	private String val2 = "value2";
	
	
	static {
		injector = Guice.createInjector(new BTreeModule("/tmp/leaf_spec")); 
	}
	
	@Before public void setUp(){
		leaf = injector.getInstance(Key.get(new TypeLiteral<LeafPageManager<Integer, String>>(){})).createPage();
		}
	
	@Test public void shouldBeAbleToInsertAndGet(){
		leaf.insert(key1, val1);
		assertTrue(leaf.containsKey(key1));
		assertEquals(1, leaf.getNumberOfEntries());
		assertEquals(1, leaf.get(key1).size());
		assertEquals(val1, leaf.get(key1).get(0));
	}
	
	@Test public void shouldBeAbleToGetLastKeyAndPointer(){
		leaf.insert(key1, val1);
		assertNotNull(leaf.getLastKey());
		assertNotNull(leaf.getLastKeyPointer());
		
		leaf.insert(key2, val2);
		assertNotNull(leaf.getLastKey());
		assertNotNull(leaf.getLastKeyPointer());
	}
	
	@Test public void shouldAlwaysWorkAfterReload(){
		for(int i = 0; i < 5; i++){
			leaf.insert(key1, val1);
		}
		leaf.insert(key2, val2);
		assertEquals(6, leaf.getNumberOfEntries());
		leaf.load();
		assertEquals(6, leaf.getNumberOfEntries());
		assertEquals(1, leaf.get(key2).size());
		
	}
	
	@Test public void shouldAtSomePointReturnAValidAdjustmentAction(){
		AdjustmentAction<Integer, String> action;
		do{
			action = leaf.insert(key1, val1);
		} while(action == null);
		
		DataPageManager<Integer> keyPageManager = injector.getInstance(Key.get(new TypeLiteral<DataPageManager<Integer>>(){}));
		
		assertNotNull(leaf.getLastKey());
		assertEquals(AdjustmentAction.ACTION.INSERT_NEW_NODE, action.getAction());
		
		assertNotNull(action.getKeyPointer());
		assertNotNull(action.getKeyPointer().getOffset());
		assertNotNull(action.getKeyPointer().getId());
		
		assertNotNull(keyPageManager.getPage(action.getKeyPointer().getId()));
		assertNotNull(keyPageManager.getPage(action.getKeyPointer().getId()).get(action.getKeyPointer().getOffset()));
		
		
	}
	
	
	

}
