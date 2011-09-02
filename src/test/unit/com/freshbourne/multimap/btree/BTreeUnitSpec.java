/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */

package com.freshbourne.multimap.btree;

import java.util.AbstractMap.SimpleEntry;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import org.apache.commons.logging.Log;

public class BTreeUnitSpec {

	private BTree<Integer, Integer> tree;
	private static Injector injector;
	private static final Log LOG = LogFactory.getLog(BTreeUnitSpec.class);

	static {
		injector = Guice.createInjector(new BTreeModule("/tmp/bTreeUnitSpec"));
	}

	@Before
	public void setUp() {
		tree = injector.getInstance(
				Key.get(new TypeLiteral<BTree<Integer, Integer>>() {}));
	}

	@Test
	public void bulkInsert() {
		int testSize = 10000;
		
		@SuppressWarnings("unchecked")
		SimpleEntry<Integer, Integer>[] kvs = new SimpleEntry[testSize];
		
		for(int i = 0; i < testSize; i++){
			kvs[i] = new SimpleEntry<Integer, Integer>(i, i + 10000);
		}
		
		tree.bulkInitialize(kvs, true);
		
		// check if its correct
		LOG.debug("checking bulkinsert results...");
		assertEquals(testSize, tree.getNumberOfEntries());
		for(int i = 0; i < testSize; i++){

			if(tree.get(kvs[i].getKey()).size() == 0){
				LOG.error("tree doesn't have key " + i);
			}
			assertEquals("size problem with key " + i, 1, tree.get(kvs[i].getKey()).size());
			assertEquals(kvs[i].getValue(), tree.get(kvs[i].getKey()).get(0));
		}
	}
}
