/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */

package com.freshbourne.multimap.btree;

import com.freshbourne.multimap.KeyValueObj;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import org.junit.Before;
import org.junit.Test;

public class BTreeUnitSpec {

	private BTree<Integer, Integer> tree;
	private static Injector injector;

	static {
		injector = Guice.createInjector(new BTreeModule("/tmp/bTreeUnitSpec"));
	}

	@Before
	public void setUp() {
		tree = injector.getInstance(
				Key.get(new TypeLiteral<BTree<Integer, Integer>>() {
				}));
	}

	@Test
	public void bla() {
		KeyValueObj<Integer, Integer>[] kvs = new KeyValueObj[1];
		tree.bulkInitialize(kvs, true);

		
	}
}
