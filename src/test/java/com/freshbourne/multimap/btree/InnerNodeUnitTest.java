/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */
package com.freshbourne.multimap.btree;

import java.io.IOException;
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
		
		node = getNewNode();
	}

    private InnerNode<Integer, Integer> getNewNode(){
        return new InnerNode<Integer, Integer>(rawPage, IntegerSerializer.INSTANCE,
				IntegerComparator.INSTANCE, keyPageManager, leafPageManager, innerNodePageManager);
    }
	
	/**
	 * sets up the leaf so that node requests page 100 for values < 0, page 101 for values >= 0
	 */
    @Test
	public void initRootState(){
        node.initialize();
		node.initRootState(100, 0, 101);
        ByteBuffer buf = rawPage.bufferForReading(InnerNode.Header.size());
		assertEquals(100, buf.getInt());
		assertEquals(0, buf.getInt());
		assertEquals(101, buf.getInt());
	}

    @Test
    public void loadNode() throws IOException {
        node.initialize();
        node.initRootState(100, 0, 101);
        node = getNewNode();

        // after #getNewNode(), a load or initialize should be required
        assertFalse(node.isValid());

        ByteBuffer buf = rawPage.bufferForWriting(rawPage.bufferForReading(0).limit() - 8);
        buf.putInt(1000); // key
        buf.putInt(102); // pageid

        // set the number of keys to 2
        buf = rawPage.bufferForWriting(InnerNode.Header.NUMBER_OF_KEYS.getOffset());
        buf.putInt(2);

        node.load();

        assertTrue(node.isValid());
        assertEquals(node.getMaxNumberOfKeys(), node.getNumberOfKeys());

        // check if getting the last page works
        when(leafPageManager.hasPage(102)).thenReturn(true);
        when(leafPageManager.getPage(102)).thenReturn(leaf1);
        node.insert(1001, 11);
		verify(leaf1).insert(1001, 11);

    }

    @Test(expected = IOException.class)
    public void loadIllRawPageShouldThrowException() throws IOException {
        node.load();
    }
	
	@Test
	public void testInitRootState(){
		initRootState();

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
	public void whenTheKeyMatchesGoLeft(){
		initRootState();
		when(leafPageManager.hasPage(100)).thenReturn(true);
		when(leafPageManager.getPage(100)).thenReturn(leaf1);
		node.insert(0, 11);
		verify(leaf1).insert(0, 11);
	}
	
	@Test
	public void testLeafSplit() throws IOException {
		loadNode();

        // node.insert(10, 11);
		
		
		// assertEquals(node.getMaxNumberOfKeys(), node.getNumberOfKeys());
	}
}
