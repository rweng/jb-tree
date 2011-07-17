/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */
package com.freshbourne.multimap.btree;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import com.freshbourne.multimap.MultiMap;
import com.freshbourne.multimap.MultiMapSpec;

public class BTreeSpec extends MultiMapSpec<Integer, String> {
	
	private static String path = "/tmp/btree_spec";
	private static BTreeProvider provider = new BTreeProvider(path); 
	
	public BTreeSpec() {
		super(provider);
	}
	
	@Test
	public void shouldBeAbleToOpenAndLoad(){
		Integer smaller, larger;
		if(key1.compareTo(key2) > 0){
			larger = key1;
			smaller = key2;
		} else {
			smaller = key1;
			larger = key2;
		}
		
		shouldBeAbleToOpenAndLoad(smaller, larger);
		shouldBeAbleToOpenAndLoad(larger, smaller);
	}
	
	private void shouldBeAbleToOpenAndLoad(Integer key1, Integer key2){

		BTree<Integer, String>tree = (BTree<Integer, String>)getMultiMap();
		
		tree.initialize();
		tree.add(key1, value1);
		tree.add(key2, value2);
		tree.sync();
		
		tree = provider.getInstance();
		tree.load();
		assertEquals(2, tree.getNumberOfEntries());
		assertEquals(value1, tree.get(key1).get(0));
		assertEquals(value2, tree.get(key2).get(0));
	}

	
}
