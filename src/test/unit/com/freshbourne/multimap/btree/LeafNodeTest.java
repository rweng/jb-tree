/**
 * Copyright (C) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.multimap.btree;

import java.nio.ByteBuffer;

import org.junit.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

import com.freshbourne.comparator.IntegerComparator;
import com.freshbourne.io.DataPage;
import com.freshbourne.io.DataPageManager;
import com.freshbourne.io.PageManager;
import com.freshbourne.io.RawPage;
import com.freshbourne.multimap.btree.BTree.NodeType;
import com.freshbourne.multimap.btree.InnerNode;
import com.freshbourne.multimap.btree.LeafNode.Header;
import com.freshbourne.serializer.IntegerSerializer;
import com.freshbourne.serializer.PagePointSerializer;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import com.freshbourne.multimap.MultiMap;
import com.freshbourne.multimap.MultiMapSpec;

public class LeafNodeTest {
	
	private LeafNode<Integer, Integer> node;
	
	// dependencies
	private RawPage rawPage;
	@Mock private DataPageManager<Integer> keyPageManager;
	@Mock private PageManager<LeafNode<Integer, Integer>> leafPageManager;
	@Mock private DataPageManager<Integer> valuePageManager;
	
	@Before
	public void setUp(){
		MockitoAnnotations.initMocks(this); 
		rawPage = new RawPage(ByteBuffer.allocate(22), 100);
		node = new LeafNode<Integer, Integer>(rawPage, IntegerSerializer.INSTANCE,
				IntegerSerializer.INSTANCE, IntegerComparator.INSTANCE, leafPageManager);
		node.initialize();
	}
	
	@Test
	public void testInitialize(){
		ByteBuffer buf = rawPage.bufferForReading(0);
		assertEquals(NodeType.LEAF_NODE.serialize(), buf.getChar());
		assertEquals(0, buf.getInt());
		assertEquals((int)LeafNode.NO_NEXT_LEAF, buf.getInt());
	}
	
	@Test
	public void testFirstInsert(){
		DataPage dPageMock = mock(DataPage.class);
		RawPage rp = new RawPage(ByteBuffer.allocate(1000), 101);
		when(valuePageManager.createPage()).thenReturn(dPageMock);
		when(dPageMock.rawPage()).thenReturn(rp);
		node.insert(1, 2);
		verify(dPageMock).add(2);
	}
}
