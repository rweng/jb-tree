/**
 * Copyright (C) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.io;

import java.nio.ByteBuffer;

import org.junit.Before;
import org.junit.Test;


import static org.mockito.Mockito.*;

import com.freshbourne.comparator.IntegerComparator;
import com.freshbourne.multimap.btree.InnerNode;
import com.freshbourne.serializer.FixLengthSerializer;
import com.freshbourne.serializer.IntegerSerializer;
import com.freshbourne.serializer.PagePointSerializer;


public class InnerNodeTest {
	
	
	private InnerNode<Integer, Integer> innerNode;
	
	@Before
	public void setUp(){
		RawPage p = new RawPage(ByteBuffer.allocate(1024), 1);
		
//		innerNode = new InnerNode<Integer, Integer>(
//				p, IntegerSerializer.INSTANCE, IntegerComparator.INSTANCE);
	}
	
	@Test
	public void testNewInnerNodeSplit(){
		
	}
	

}
