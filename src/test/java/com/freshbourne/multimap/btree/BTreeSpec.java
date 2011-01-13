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

import com.freshbourne.io.FileResourceManagerModule;
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

	private final static Injector injector;
	
	static {
		injector = Guice.createInjector(
				new FileResourceManagerModule("/tmp/test"),
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
	public void shouldContainAnAddedEntry() throws Exception{
		tree.add(1, s);
		assertTrue(tree.containsKey(1));
		assertEquals(s, tree.get(1));
	}
	
	@Test
	public void shouldBeAbleToRemoveInsertedEntries(){
		fail();
	}
	
	@Test
	public void shouldThrowExceptionIfTryingToAccessNonexistantElements(){fail();}
	
	@Test
	public void clearShouldRemoveAllElements(){fail();}
	
	@Test
	public void getFirstShouldReturnFirstElement(){fail();}
	
	@Test
	public void getShouldReturnAllElements(){fail();}
	
	@Test
	public void removeWithValueArgumentShouldRemoveOnlyThisValue(){fail();}
	
	@Test
	public void removeWithOnlyKeyArgumentShouldRemoveAllValues(){fail();}
	
}
