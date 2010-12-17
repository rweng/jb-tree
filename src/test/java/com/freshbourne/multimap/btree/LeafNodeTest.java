/**
 * Copyright (C) 2010 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.multimap.btree;

import java.io.File;

import com.freshbourne.io.FileResourceManagerModule;
import com.freshbourne.io.Serializer;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;

import junit.framework.TestCase;

public class LeafNodeTest extends TestCase {
	

	private Injector i;
	
	public LeafNodeTest() {
		super();
		i = Guice.createInjector(
				new FileResourceManagerModule(new File("/tmp/leafnodetest")), 
				new BTreeTestModule());
	}
	
	public void setUp(){
		
	}
	
	public void test(){
		LeafNode<Integer, String> l = null;
	}
}
