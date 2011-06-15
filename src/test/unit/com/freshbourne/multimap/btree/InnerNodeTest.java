/**
 * Copyright (C) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.multimap.btree;

import org.junit.*;
import org.mockito.Mock;

import com.freshbourne.io.RawPage;
import com.freshbourne.multimap.btree.InnerNode;

public class InnerNodeTest {
	
	private InnerNode<Integer, Integer> node;
	
	// dependencies
	@Mock private RawPage rawpage;
	
	@Before
	public void setUp(){
		RawPage rawPage;
		//node = new InnerNode<Integer, Integer>();
	}
	
}
