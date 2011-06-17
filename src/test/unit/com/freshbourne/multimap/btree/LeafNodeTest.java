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
	private int minNumberOfValues = 3;
	private int rawPageSize = 100;
	@Mock private PageManager<LeafNode<Integer, Integer>> leafPageManager;
	
	@Before
	public void setUp(){
		MockitoAnnotations.initMocks(this); 
		rawPage = new RawPage(ByteBuffer.allocate(rawPageSize), 100);
		node = new LeafNode<Integer, Integer>(rawPage, IntegerSerializer.INSTANCE,
				IntegerSerializer.INSTANCE, IntegerComparator.INSTANCE, leafPageManager, minNumberOfValues);
		node.initialize();
	}
	
	@Test
	public void minNumberOfValues(){
		int tmpValues = minNumberOfValues;
		int tmpSize = rawPageSize;
		
		minNumberOfValues = 1;
		rawPageSize = Header.size() + 2*IntegerSerializer.INSTANCE.getSerializedLength();
		
		// this should work
		setUp();
		
		// this shouldn't work
		try{
			rawPageSize--;
			setUp();
			throw new IllegalStateException("this shouldn't work");
		} catch (Exception e) {
		}
		
		minNumberOfValues = 0;
		rawPageSize = Header.size();
		
		// should work
		setUp();
		
		// this shouldn't work
		try{
			rawPageSize--;
			setUp();
			throw new IllegalStateException("this shouldn't work");
		} catch (Exception e) {
		}
		
		minNumberOfValues = 2;
		rawPageSize = Header.size() + 4*IntegerSerializer.INSTANCE.getSerializedLength();
		
		// should work
		setUp();
		
		// this shouldn't work
		try{
			rawPageSize--;
			setUp();
			throw new IllegalStateException("this shouldn't work");
		} catch (Exception e) {
		}
		
		rawPageSize +=2;
		
		// should work
		setUp();
		
		// reset values
		minNumberOfValues = tmpValues;
		rawPageSize = tmpSize;
	}
	
	@Test
	public void testInitialize(){
		ByteBuffer buf = rawPage.bufferForReading(0);
		assertEquals(NodeType.LEAF_NODE.serialize(), buf.getChar());
		assertEquals(0, buf.getInt());
		assertEquals((int)LeafNode.NO_NEXT_LEAF, buf.getInt());
	}
	
	@Test
	public void firstInsert(){
		node.insert(1, 2);
		ensureKeyValueInRawPage(Header.size(), 1, 2);
	}
	
	private void ensureKeyValueInRawPage(int offset, int key, int value){
		ByteBuffer buf = rawPage.bufferForReading(offset);
		byte[] bytes = new byte[IntegerSerializer.INSTANCE.getSerializedLength()];
		buf.get(bytes);
		assertEquals(key, (int) IntegerSerializer.INSTANCE.deserialize(bytes));
		buf.get(bytes);
		assertEquals(value, (int) IntegerSerializer.INSTANCE.deserialize(bytes));
	}
	
	@Test
	public void secondInsert(){
		firstInsert();
		node.insert(3, 3);
		ensureKeyValueInRawPage(Header.size() + 2*IntegerSerializer.INSTANCE.getSerializedLength(), 3, 3);
	}
	
	@Test
	public void doubleInsert(){
		secondInsert();
		node.insert(1, 3);
		ensureKeyValueInRawPage(Header.size(), 1, 3);
	}
	
	@Test
	public void insertionInTheMiddle(){
		secondInsert();
		node.insert(2, 44);
		ensureKeyValueInRawPage(Header.size() + 0*IntegerSerializer.INSTANCE.getSerializedLength(), 1, 2);
		ensureKeyValueInRawPage(Header.size() + 2*IntegerSerializer.INSTANCE.getSerializedLength(), 2, 44);
		ensureKeyValueInRawPage(Header.size() + 4*IntegerSerializer.INSTANCE.getSerializedLength(), 3, 3);
	}
}
