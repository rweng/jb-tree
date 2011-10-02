/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */
package com.freshbourne.multimap.btree;

import java.nio.ByteBuffer;
import java.util.Iterator;

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

import com.freshbourne.multimap.MultiMap;
import com.freshbourne.multimap.MultiMapSpec;

public class LeafNodeUnitTest {
	
	private LeafNode<Integer, Integer> node;
	
	// dependencies
	private RawPage rawPage;
	private RawPage rawPage2;
	private int minNumberOfValues = 3;
	private int rawPageSize = 34;
	@Mock private PageManager<LeafNode<Integer, Integer>> leafPageManager;

	private LeafNode<Integer, Integer> node2;

	
	@Before
	public void setUp(){
		MockitoAnnotations.initMocks(this); 
		rawPage = new RawPage(ByteBuffer.allocate(rawPageSize), 100);
		rawPage2 = new RawPage(ByteBuffer.allocate(rawPageSize), 101);
		node = new LeafNode<Integer, Integer>(rawPage, IntegerSerializer.INSTANCE,
				IntegerSerializer.INSTANCE, IntegerComparator.INSTANCE, leafPageManager, minNumberOfValues);
		node2 = new LeafNode<Integer, Integer>(rawPage2, IntegerSerializer.INSTANCE,
				IntegerSerializer.INSTANCE, IntegerComparator.INSTANCE, leafPageManager, minNumberOfValues);
		node.initialize();
	}
	
	@Test
	public void load(){
		node.insert(1, 101);
		node.insert(2, 201);
		assertEquals(2, node.getNumberOfEntries());
		
		node = new LeafNode<Integer, Integer>(rawPage, IntegerSerializer.INSTANCE,
				IntegerSerializer.INSTANCE, IntegerComparator.INSTANCE, leafPageManager, minNumberOfValues);
		
		
		node.load();
		assertEquals(2, node.getNumberOfEntries());
		assertEquals(101, (int) node.getFirst(1));
		assertEquals(201, (int) node.getFirst(2));
		
	}
	
	@Test
	public void destroy(){
		node.insert(1, 101);
		node.destroy();
		verify(leafPageManager).removePage(100);
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
		node.insert(1, 101);
		ensureKeyValueInRawPage(rawPage, Header.size(), 1, 101);
		
		assertEquals(1, node.get(1).size());
		assertEquals(101, (int) node.get(1).get(0));
	}
	
	private void ensureKeyValueInRawPage(RawPage rp, int offset, int key, int value){
		ByteBuffer buf = rp.bufferForReading(offset);
		byte[] bytes = new byte[IntegerSerializer.INSTANCE.getSerializedLength()];
		buf.get(bytes);
		assertEquals(key, (int) IntegerSerializer.INSTANCE.deserialize(bytes));
		buf.get(bytes);
		assertEquals(value, (int) IntegerSerializer.INSTANCE.deserialize(bytes));
	}
	
	@Test
	public void secondInsert(){
		firstInsert();
		node.insert(10, 1001);
		ensureKeyValueInRawPage(rawPage, Header.size() + 2*IntegerSerializer.INSTANCE.getSerializedLength(), 10, 1001);
		

		assertEquals(1, node.get(1).size());
		assertEquals(101, (int) node.get(1).get(0));

		assertEquals(1, node.get(10).size());
		assertEquals(1001, (int) node.get(10).get(0));
	}
	
	@Test
	public void doubleInsert(){
		secondInsert();
		node.insert(1, 102);
		ensureKeyValueInRawPage(rawPage, Header.size(), 1, 102);
		
		assertEquals(2, node.get(1).size());
		assertEquals(102, (int) node.get(1).get(0));
		assertEquals(101, (int) node.get(1).get(1));
	}
	
	@Test
	public void insertionInTheMiddle(){
		secondInsert();
		node.insert(5, 501);
		ensureKeyValueInRawPage(rawPage, Header.size() + 0*IntegerSerializer.INSTANCE.getSerializedLength(), 1, 101);
		ensureKeyValueInRawPage(rawPage, Header.size() + 2*IntegerSerializer.INSTANCE.getSerializedLength(), 5, 501);
		ensureKeyValueInRawPage(rawPage, Header.size() + 4*IntegerSerializer.INSTANCE.getSerializedLength(), 10, 1001);
	}
}
