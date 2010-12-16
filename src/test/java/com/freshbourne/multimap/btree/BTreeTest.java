/**
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 * 
 * (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */
package com.freshbourne.multimap.btree;

import java.io.File;

import com.freshbourne.io.FileResourceManagerModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import junit.framework.TestCase;

public class BTreeTest extends TestCase {
	
	private BTree<String, String> tree;
	private final Injector injector;
	
	public BTreeTest(){
		super();
		injector = Guice.createInjector(new FileResourceManagerModule(new File("/tmp/btree_test_file")));
	}
	
	public void setUp(){
		tree = injector.getInstance(BTree.class);
	}
	
	public void testInitState(){
		assertTrue(tree instanceof BTree);
		assertEquals(0, tree.size());
	}

}
