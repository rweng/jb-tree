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

import org.junit.Test;

public class InnerNodeUnitTest {
	
	private InnerNode<Integer, Integer> node;
	
	// dependencies
	private RawPage rawPage;
	@Mock private DataPageManager<Integer> keyPageManager;
	@Mock private PageManager<LeafNode<Integer, Integer>> leafPageManager;
	@Mock private PageManager<InnerNode<Integer, Integer>> innerNodePageManager;
	@Mock LeafNode<Integer, Integer> leaf1, leaf2, leaf3;
	
	@Before
	public void setUp(){
		MockitoAnnotations.initMocks(this); 
		
		// allocate space for header + 2 keys + 3 pointer
		rawPage = new RawPage(ByteBuffer.allocate(6 + 3 * 4 + 2 * 4), 100);
		
		node = new InnerNode<Integer, Integer>(rawPage, IntegerSerializer.INSTANCE,
				IntegerComparator.INSTANCE, keyPageManager, leafPageManager, innerNodePageManager);
		node.initialize();
	}
	
	private void initRootState(){
		node.initRootState(100, 0, 101);
	}
	
	@Test
	public void testInitRootState(){
		initRootState();
		ByteBuffer buf = rawPage.bufferForReading(InnerNode.Header.size());
		assertEquals(100, buf.getInt());
		assertEquals(0, buf.getInt());
		assertEquals(101, buf.getInt());
	}
	
	@Test
	public void testFirstInsert(){
		initRootState();
		when(leafPageManager.hasPage(101)).thenReturn(true);
		when(leafPageManager.getPage(101)).thenReturn(leaf1);
		node.insert(10, 11);
		verify(leaf1).insert(10, 11);
	}
	
	@Test
	public void testLeafSplit(){
		initRootState();
		
		
		// assertEquals(node.getMaxNumberOfKeys(), node.getNumberOfKeys());
	}
}
