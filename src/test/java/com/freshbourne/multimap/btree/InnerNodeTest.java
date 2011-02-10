/**
 * Copyright (C) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.multimap.btree;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.nio.ByteBuffer;
import java.util.Comparator;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.freshbourne.comparator.IntegerComparator;
import com.freshbourne.io.DataPageManager;
import com.freshbourne.io.DynamicDataPage;
import com.freshbourne.io.PageManager;
import com.freshbourne.io.PagePointer;
import com.freshbourne.io.RawPage;
import com.freshbourne.multimap.btree.BTree.NodeType;
import com.freshbourne.serializer.FixLengthSerializer;
import com.freshbourne.serializer.PagePointSerializer;

public class InnerNodeTest {
	
	private InnerNode<Integer, String> node;
	
	RawPage rawPage;
	FixLengthSerializer<PagePointer, byte[]> pointerSerializer = PagePointSerializer.INSTANCE;
	Comparator<Integer> comparator = IntegerComparator.INSTANCE;
	
	// some mocks
	@Mock DataPageManager<Integer> keyPageManager;
	@Mock PageManager<LeafNode<Integer, String>> leafPageManager;
	@Mock PageManager<InnerNode<Integer, String>> innerNodePageManager;
	@Mock DynamicDataPage<Integer> dataPage;
	@Mock InnerNode<Integer, String> innerNode;
	@Mock LeafNode<Integer, String> leafNode1, leafNode2, leafNode3;
	
	
	// some testing data
	PagePointer keyPointer = new PagePointer(1111L, 1112); // value 100, see setUp
	PagePointer keyPointer2 = new PagePointer(2222L, 2223);
	
	long pageId1 = 111L;
	long pageId2 = 222L;
	long pageId3 = 333L;
	
	int key1 = 100;
	int key2 = 200;
	
	String val1 = "val1";
	
	@Before
	public void setUp(){
		MockitoAnnotations.initMocks(this);
		rawPage = new RawPage(ByteBuffer.allocate(1024), 1L);
		
		when(keyPageManager.getPage(anyLong())).thenReturn(dataPage);
		when(dataPage.get(keyPointer.getOffset())).thenReturn(key1);
		
		
		when(innerNodePageManager.hasPage(anyLong())).thenReturn(true);
		when(innerNodePageManager.getPage(anyLong())).thenReturn(innerNode);
		
		when(leafPageManager.hasPage(anyLong())).thenReturn(true);
		when(leafPageManager.getPage(anyLong())).thenReturn(leafNode1);
		
		node = new InnerNode<Integer, String>(rawPage, pointerSerializer, comparator, keyPageManager, leafPageManager, innerNodePageManager);
	}
	
	
	@Test
	public void initialize(){
		assertFalse(node.isValid());
		node.initialize();
		assertTrue(node.isValid());
		ByteBuffer buffer = node.rawPage().bufferForReading(0);
		assertEquals(NodeType.INNER_NODE.serialize(), buffer.getChar());
		assertEquals(0, buffer.getInt());
	}
	
	@Test
	public void initRootState(){
		node.initialize();
		
		node.initRootState(keyPointer, pageId1, pageId2);
		
		RawPage page = node.rawPage();
		ByteBuffer buffer = page.bufferForReading(InnerNode.Header.size());
		
		assertEquals(pageId1, buffer.getLong());
		
		byte[] pointerBuf = new byte[pointerSerializer.serializedLength(PagePointer.class)];
		buffer.get(pointerBuf);
		
		assertEquals(keyPointer, pointerSerializer.deserialize(pointerBuf));
		assertEquals(pageId2, buffer.getLong());
	}
	
	@Test
	public void insertSmallerNextLeaf(){
		node.initialize();
		node.initRootState(keyPointer, pageId1, pageId2);
		
		// next thing is a leaf
		when(innerNodePageManager.hasPage(anyLong())).thenReturn(false);
		node.insert(key1 - 1, val1);
		verify(leafNode1).insert(key1 - 1, val1);
	}
	
	// not yet supported
	@Test(expected= UnsupportedOperationException.class)
	public void insertSmallerNextInner(){
		node.initialize();
		node.initRootState(keyPointer, pageId1, pageId2);
		
		// next thing is a leaf
		when(leafPageManager.hasPage(anyLong())).thenReturn(false);
		node.insert(key1 - 1, val1);
		verify(innerNode).insert(key1 - 1, val1);
	}
	
	@Test
	public void contains(){
		insertSmallerNextLeaf();
		
		node.containsKey(key1 - 1);
		verify(leafNode1).containsKey(key1 - 1);
	}
}
