/**
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 * 
 * (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */
package com.freshbourne.multimap.btree;

import junit.framework.TestCase;

public class BTreeTest extends TestCase {
	
	BTree<String, String> tree;
	
	public void setUp(){
		tree = new BTree<String, String>();
	}
	
	public void testInitState(){
		assertTrue(tree instanceof BTree);
		assertEquals(0, tree.size());
		assertTrue(tree.isEmpty());
	}

}
