/**
 * Copyright (C) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.multimap.btree;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;
import java.util.Comparator;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.freshbourne.comparator.IntegerComparator;
import com.freshbourne.io.DataPageManager;
import com.freshbourne.io.PageManager;
import com.freshbourne.io.PagePointer;
import com.freshbourne.io.RawPage;
import com.freshbourne.serializer.FixLengthSerializer;
import com.freshbourne.serializer.PagePointSerializer;

public class LeafNodeTest {
	@Mock DataPageManager<Integer> keyPageManager;
    @Mock DataPageManager<String> valuePageManager;
	@Mock PageManager<LeafNode<Integer,String>> leafPageManager;
	
    FixLengthSerializer<PagePointer, byte[]> pointerSerializer = PagePointSerializer.INSTANCE;
	Comparator<Integer> comparator = IntegerComparator.INSTANCE;
	
	LeafNode<Integer,String> leaf, leaf2;
	
	
	@Before
	public void setUp(){
		MockitoAnnotations.initMocks(this);
		
		leaf = new LeafNode<Integer, String>(new RawPage(ByteBuffer.allocate(1024), 1L), keyPageManager, valuePageManager, pointerSerializer, comparator, leafPageManager);
		leaf2 = new LeafNode<Integer, String>(new RawPage(ByteBuffer.allocate(1024), 1L), keyPageManager, valuePageManager, pointerSerializer, comparator, leafPageManager);
		
		leaf.initialize();
		leaf2.initialize();
	}
	
	@Test
	public void prependFromOtherNode(){
		leaf.setNextLeafId(leaf2.getId());
		
		// fill leaf
		for(int i = 0; i < leaf.getMaximalNumberOfEntries(); i++){
			assertNull(leaf.insert(i, "val"));
		}
		
		// insert key so that move should happen
		AdjustmentAction<Integer, String> action = leaf.insert(1, "value");
		
		// an update key action should be passed up
		assertNotNull(action);
		
		// make sure leaf structures are in tact
		assertEquals(leaf.getLastKey(), leaf.getKeyAtPosition(leaf.getNumberOfKeys() - 1));
	}
	
}
