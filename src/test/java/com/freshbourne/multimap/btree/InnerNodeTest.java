/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */
package com.freshbourne.multimap.btree;

import java.nio.ByteBuffer;

import org.junit.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

import com.freshbourne.comparator.IntegerComparator;
import com.freshbourne.io.DataPageManager;
import com.freshbourne.io.PageManager;
import com.freshbourne.io.RawPage;
import com.freshbourne.multimap.btree.InnerNode;
import com.freshbourne.serializer.IntegerSerializer;
import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import com.freshbourne.multimap.MultiMap;
import com.freshbourne.multimap.MultiMapSpec;

public class InnerNodeTest {
	
	private InnerNode<Integer, Integer> node;
	
	// dependencies
	private RawPage rawPage;
	@Mock private DataPageManager<Integer> keyPageManager;
	@Mock private PageManager<LeafNode<Integer, Integer>> leafPageManager;
	@Mock private PageManager<InnerNode<Integer, Integer>> innerNodePageManager;
	
	@Before
	public void setUp(){
		MockitoAnnotations.initMocks(this); 
		rawPage = new RawPage(ByteBuffer.allocate(6 + 3 * 4 + 2 * 4), 100);
		node = new InnerNode<Integer, Integer>(rawPage, IntegerSerializer.INSTANCE,
				IntegerComparator.INSTANCE, keyPageManager, leafPageManager, innerNodePageManager);
		node.initialize();
	}
	
	@Test
	public void testInitRootState(){
		node.initRootState(100, 0, 101);
		ByteBuffer buf = rawPage.bufferForReading(InnerNode.Header.size());
		assertEquals(100, buf.getInt());
		assertEquals(0, buf.getInt());
		assertEquals(101, buf.getInt());
	}
	
	@Test
	public void testFirstInsert(){
		testInitRootState();
		LeafNode leafNode = mock(LeafNode.class);
		when(leafPageManager.hasPage(101)).thenReturn(true);
		when(leafPageManager.getPage(101)).thenReturn(leafNode);
		node.insert(10, 11);
		verify(leafNode).insert(10, 11);
	}
}
