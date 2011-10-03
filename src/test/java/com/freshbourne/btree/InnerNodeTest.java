/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */
package com.freshbourne.btree;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import org.junit.Before;
import org.junit.Test;

public class InnerNodeTest {
	
	protected final static Injector injector;
	protected InnerNode<Integer, String> node;

	protected int key1 = 1;
	protected int key2 = 2;

	protected String val1 = "val1";
	protected String val2 = "value2";

	static {
		injector = Guice.createInjector(new BTreeModule("/tmp/innernode_spec"));
	}

	@Before
	public void setUp() {
		node = injector.getInstance(
				Key.get(new TypeLiteral<InnerNodeManager<Integer, String>>() {
				})).createPage();
	}
	
	@Test(expected= IllegalStateException.class)
	public void insertShouldRequireInitializedRoot(){
		node.insert(key1, val1);
	}
}
