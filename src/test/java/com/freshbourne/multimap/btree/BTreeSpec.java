/**
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 * 
 * (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */
package com.freshbourne.multimap.btree;

import org.junit.Before;
import org.junit.Test;

import com.freshbourne.comparator.IntegerComparator;
import com.freshbourne.io.BufferPoolManagerImpl;
import com.freshbourne.io.ResourceManager;
import com.freshbourne.multimap.MultiMap;

import static org.junit.Assert.*;

public class BTreeSpec {
	
	private MultiMap<Integer, String> tree;
	private String s = "testString";

	@Before
	public void setUp(){
		
		//TODO: tree = new BTree<Integer, String>(IntegerComparator.INSTANCE);
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

}
