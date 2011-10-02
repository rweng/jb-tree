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
import com.freshbourne.serializer.IntegerSerializer;

import static org.junit.Assert.*;

import org.junit.Test;

public class InnerNodeUnitTest {

    private InnerNode<Integer, Integer> node1, node2;
    private RawPage rawPage, rawPage2;
    private int key1 = 0;
    private int key2 = 1000;

    @Mock private DataPageManager<Integer>                 keyPageManager;
    @Mock private PageManager<LeafNode<Integer, Integer>>  leafPageManager;
    @Mock private PageManager<InnerNode<Integer, Integer>> innerNodePageManager;
    @Mock private LeafNode<Integer, Integer>               leaf1, leaf2, leaf3;
    @Mock private InnerNode<Integer, Integer> inner1, inner2;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        // allocate space for header + 2 keys + 3 pointer
        rawPage = new RawPage(ByteBuffer.allocate(6 + 3 * 4 + 2 * 4), 1000);
        rawPage2 = new RawPage(ByteBuffer.allocate(6 + 3 * 4 + 2 * 4), 1001);

        node1 = getNewNode(rawPage);
        node2 = getNewNode(rawPage2);

        when(leafPageManager.hasPage(100)).thenReturn(true);
        when(leafPageManager.getPage(100)).thenReturn(leaf1);


        when(leafPageManager.hasPage(101)).thenReturn(true);
        when(leafPageManager.getPage(101)).thenReturn(leaf2);


        when(leafPageManager.hasPage(102)).thenReturn(true);
        when(leafPageManager.getPage(102)).thenReturn(leaf3);

    }

    private InnerNode<Integer, Integer> getNewNode(RawPage rawPage) {
        return new InnerNode<Integer, Integer>(rawPage, IntegerSerializer.INSTANCE,
                IntegerComparator.INSTANCE, keyPageManager, leafPageManager, innerNodePageManager);
    }

    /**
     * sets up the leaf so that node1 requests page 100 for values < 0, page 101 for values >= 0
     */
    @Test
    public void initRootState() {
        node1.initialize();
        node1.initRootState(100, key1, 101);
        ByteBuffer buf = rawPage.bufferForReading(InnerNode.Header.size());
        assertEquals(100, buf.getInt());
        assertEquals(key1, buf.getInt());
        assertEquals(101, buf.getInt());
    }

    /**
     * sets up memory like this: 100 0 101 1000 102
     *
     * @throws IOException
     */
    @Test
    public void loadNode() throws IOException {
        node1.initialize();
        node1.initRootState(100, 0, 101);
        node1 = getNewNode(rawPage);

        // after #getNewNode(), a load or initialize should be required
        assertFalse(node1.isValid());

        ByteBuffer buf = rawPage.bufferForWriting(rawPage.bufferForReading(0).limit() - 8);
        buf.putInt(key2); // key
        buf.putInt(102); // pageid

        // set the number of keys to 2
        buf = rawPage.bufferForWriting(InnerNode.Header.NUMBER_OF_KEYS.getOffset());
        buf.putInt(2);

        node1.load();

        assertTrue(node1.isValid());
        assertEquals(node1.getMaxNumberOfKeys(), node1.getNumberOfKeys());

        // check if getting the last page works
        node1.insert(1001, 11);
        verify(getLeafForKey(1001)).insert(1001, 11);

    }

    private LeafNode<Integer, Integer> getLeafForKey(int key) {
        if (key <= key1) {
            return leaf1;
        } else if (key <= key2) {
            return leaf2;
        } else {
            return leaf3;
        }
    }

    @Test(expected = IOException.class)
    public void loadIllRawPageShouldThrowException() throws IOException {
        node1.load();
    }

    @Test
    public void testInitRootState() {
        initRootState();

    }

    @Test
    public void testFirstInsert() {
        initRootState();
        node1.insert(10, 11);
        verify(getLeafForKey(10)).insert(10, 11);
    }

    @Test
    public void whenTheKeyMatchesGoLeft() {
        initRootState();
        node1.insert(0, 11);
        verify(getLeafForKey(0)).insert(0, 11);
    }

    @Test
    public void insertAdjustmentWithSpace() throws IOException {
        initRootState();
        int key = 500;

        ByteBuffer serializedPageBuffer = ByteBuffer.allocate(4).putInt(key);
        AdjustmentAction<Integer, Integer> adjustment =
                new AdjustmentAction<Integer, Integer>(AdjustmentAction.ACTION.INSERT_NEW_NODE,
                        serializedPageBuffer.array(), 102);

        when(getLeafForKey(key).insert(eq(1), anyInt())).thenReturn(adjustment);
        node1.insert(1, 199);

        ByteBuffer buf = rawPage.bufferForReading(InnerNode.Header.size());

        assertEquals(100, buf.getInt());
        assertEquals(0, buf.getInt());
        assertEquals(101, buf.getInt());
        assertEquals(500, buf.getInt());
        assertEquals(102, buf.getInt());

        assertEquals(node1.getMaxNumberOfKeys(), node1.getNumberOfKeys());
    }


    /**
     * loads a full node, and calls insert on it. The leaf for the given key will return a
     * new node adjustment action with the adjustmentKey as key.
     *
     * This method also ensures that the adjustment action returned from node.insert() is also
     * a insert-new-node action.
     *
     * @param key
     * @param adjustmentKey
     * @return
     * @throws IOException
     */
    private AdjustmentAction<Integer, Integer> insertWithNewNodeAdjustment(int key, int adjustmentKey) throws IOException {
        loadNode();

        when(getLeafForKey(key).insert(eq(key), anyInt())).thenReturn(getNewAdjustmentAction(adjustmentKey));
        when(innerNodePageManager.createPage()).thenReturn(node2);


        AdjustmentAction<Integer, Integer> result = node1.insert(key, 1);

        assertNotNull(result);
        assertEquals(AdjustmentAction.ACTION.INSERT_NEW_NODE, result.getAction());
        verify(innerNodePageManager).createPage();

        return result;
    }

    private AdjustmentAction<Integer, Integer> getNewAdjustmentAction(int key) {
        ByteBuffer serializedPageBuffer = ByteBuffer.allocate(4).putInt(key);
        AdjustmentAction<Integer, Integer> adjustment =
                new AdjustmentAction<Integer, Integer>(AdjustmentAction.ACTION.INSERT_NEW_NODE,
                        serializedPageBuffer.array(), 102);
        return adjustment;
    }
}