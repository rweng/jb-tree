/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 *
 * http://creativecommons.org/licenses/by-nc/3.0/
 *
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2011 "Robin Wenglewski <robin@wenglewski.de>"
 */
package com.freshbourne.btree;


import com.freshbourne.comparator.IntegerComparator;
import com.freshbourne.io.AutoSaveResourceManager;
import com.freshbourne.io.DataPageManager;
import com.freshbourne.io.ResourceManagerBuilder;
import com.freshbourne.serializer.IntegerSerializer;
import com.freshbourne.serializer.PagePointSerializer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import static org.fest.assertions.Assertions.assertThat;
import static org.testng.Assert.*;

public class LeafNodeTest {

	private final static File file = new File("/tmp/LeafNodeTest");
	private LeafNode<Integer, Integer>        leafInt;
	private LeafPageManager<Integer, Integer> lpmInt;

	private Integer key1 = 1;
	private Integer key2 = 2;

	private Integer value1 = 101;
	private Integer value2 = 102;
	private AutoSaveResourceManager rm;

	LeafNodeTest(){
		this.rm = new ResourceManagerBuilder().file(file).buildAutoSave();
	}

	@BeforeMethod
	public void setUp() throws IOException {
		lpmInt = BTree.create(rm, IntegerSerializer.INSTANCE, IntegerSerializer.INSTANCE, IntegerComparator.INSTANCE).getLeafPageManager();
		leafInt = lpmInt.createPage();
	}

	@Test public void shouldBeAbleToInsertAndGet() {
		leafInt.insert(key1, value1);
		assertTrue(leafInt.containsKey(key1));
		assertEquals(1, leafInt.getNumberOfEntries());
		assertEquals(1, leafInt.get(key1).size());
		assertEquals(value1, leafInt.get(key1).get(0));
	}

	@Test public void shouldBeAbleToGetLastKeyAndPointer() {
		leafInt.insert(key1, value1);
		assertNotNull(leafInt.getLastLeafKey());
		assertNotNull(leafInt.getLastLeafKeySerialized());

		leafInt.insert(key2, value2);
		assertNotNull(leafInt.getLastLeafKey());
		assertNotNull(leafInt.getLastLeafKeySerialized());
	}

	@Test public void shouldAlwaysWorkAfterReload() {
		for (int i = 0; i < 5; i++) {
			leafInt.insert(key1, value1);
		}
		leafInt.insert(key2, value2);
		assertEquals(6, leafInt.getNumberOfEntries());
		leafInt.load();
		assertEquals(6, leafInt.getNumberOfEntries());
		assertEquals(1, leafInt.get(key2).size());

	}

	@Test public void shouldAtSomePointReturnAValidAdjustmentAction() {
		AdjustmentAction<Integer, Integer> action;
		do {
			action = leafInt.insert(key1, value1);
		} while (action == null);

		DataPageManager<Integer> keyPageManager = new DataPageManager<Integer>(rm, PagePointSerializer.INSTANCE, IntegerSerializer.INSTANCE);

		assertNotNull(leafInt.getLastLeafKey());
		assertEquals(AdjustmentAction.ACTION.INSERT_NEW_NODE, action.getAction());

		assertNotNull(action.getSerializedKey());


		// this should still work and not throw an exception
		stateTest(leafInt);
		LeafNode<Integer, Integer> newLeaf = lpmInt.getPage(action.getPageId());
		;
		stateTest(newLeaf);
	}

	private void stateTest(LeafNode<Integer, Integer> leaf) {
		Integer k = leaf.getLastLeafKey();
		assertNotNull(leaf.get(k));
		assertTrue(leaf.containsKey(k));

		// all keys should be accessible
		for (int i = 0; i < leaf.getNumberOfEntries(); i++) {
			Integer key = leaf.getKeyAtPosition(i);
			assertNotNull(k);
			assertTrue(leaf.containsKey(key));

		}
		assertEquals(k, leaf.getKeyAtPosition(leaf.getNumberOfEntries() - 1));

	}

	@Test
	public void iterators() {
		fillLeaf(leafInt, 10);

		Iterator<Integer> iterator = leafInt.getIterator(-5, 5);
		for (int i = 0; i <= 5; i++)
			assertEquals(i, (int) iterator.next());
		assertFalse(iterator.hasNext());

		iterator = leafInt.getIterator(5, 15);
		for (int i = 5; i < 10; i++)
			assertEquals(i, (int) iterator.next());
		assertFalse(iterator.hasNext());

		iterator = leafInt.getIterator(0, 9);
		for (int i = 0; i < 10; i++)
			assertEquals(i, (int) iterator.next());
		assertFalse(iterator.hasNext());


		iterator = leafInt.getIterator(5, null);
		for (int i = 5; i < 10; i++)
			assertEquals(i, (int) iterator.next());
		assertFalse(iterator.hasNext());

		iterator = leafInt.getIterator(null, 5);
		for (int i = 0; i <= 5; i++)
			assertEquals(i, (int) iterator.next());
		assertFalse(iterator.hasNext());

		iterator = leafInt.getIterator(null, null);
		for (int i = 0; i < 10; i++)
			assertEquals(i, (int) iterator.next());
		assertFalse(iterator.hasNext());
	}


	private void fillLeaf(LeafNode<Integer, Integer> leaf, int count) {
		for (int i = 0; i < count; i++) {
			leaf.insert(i, i);
		}
	}

	@Test
	public void shouldContainAddedEntries() {
		leafInt.insert(key1, value1);
		assertTrue(leafInt.containsKey(key1));
		assertEquals(1, leafInt.get(key1).size());
		assertEquals(value1, leafInt.get(key1).get(0));
		assertEquals(1, leafInt.getNumberOfEntries());

		leafInt.insert(key1, value2);
		assertTrue(leafInt.containsKey(key1));
		assertEquals(2, leafInt.get(key1).size());
		assertTrue(leafInt.get(key1).contains(value1));
		assertTrue(leafInt.get(key1).contains(value2));
		assertEquals(2, leafInt.getNumberOfEntries());

		leafInt.insert(key2, value2);
		assertTrue(leafInt.containsKey(key2));
		assertEquals(1, leafInt.get(key2).size());
		assertTrue(leafInt.get(key1).contains(value2));
		assertTrue(leafInt.get(key1).contains(value1));
		assertTrue(leafInt.get(key1).size() == 2);
		assertEquals(3, leafInt.getNumberOfEntries());
	}

	@Test
	public void removeWithValueArgumentShouldRemoveOnlyThisValue() {
		leafInt.insert(key1, value1);
		leafInt.insert(key1, value2);
		leafInt.insert(key2, value2);

		assertEquals(3, leafInt.getNumberOfEntries());
		leafInt.remove(key1, value2);
		assertEquals(1, leafInt.get(key1).size());
		assertEquals(value1, leafInt.get(key1).get(0));
		assertEquals(value2, leafInt.get(key2).get(0));
	}


	@Test
	public void prependEntriesShouldWork() {
		LeafNode<Integer, Integer> leaf2 = lpmInt.createPage();

		int totalInserted = 0;

		// fill leafInt
		for (int i = 0; i < leafInt.getMaximalNumberOfEntries(); i++) {
			assertThat(leafInt.insert(i, i)).isNull();
			totalInserted++;
		}

		// in testPrepend, one entry is inserted
		testPrepend(leafInt, leaf2);
		totalInserted++;

		assertThat(leafInt.getNumberOfEntries() + leaf2.getNumberOfEntries()).isEqualTo(totalInserted);

		// should work again, when we have to actually move some entries in leaf2
		for (int i = leafInt.getNumberOfEntries(); i < leafInt
				.getMaximalNumberOfEntries(); i++) {
			assertThat(leafInt.insert(-1 * i, i)).isNull();
			totalInserted++;
		}

		testPrepend(leafInt, leaf2);
		totalInserted++;
		assertThat(leafInt.getNumberOfEntries() + leaf2.getNumberOfEntries()).isEqualTo(totalInserted);

	}

	/** in testPrepend, one entry is inserted* */
	private void testPrepend(LeafNode<Integer, Integer> leaf1, LeafNode<Integer, Integer> leaf2) {
		leaf1.setNextLeafId(leaf2.getId());

		// insert key so that move should happen
		AdjustmentAction<Integer, Integer> action = leaf1.insert(1, 1);

		// an update key action should be passed up
		assertThat(action).isNotNull();

		// make sure leafInt structures are in tact
		assertThat(leaf1.getKeyAtPosition(leaf1.getNumberOfEntries() - 1)).isEqualTo(leaf1.getLastLeafKey());

		for (int key : leaf1.getKeySet()) {
			assertThat(leaf1.get(key)).isNotNull();
		}

		for (int key : leaf2.getKeySet()) {
			assertThat(leaf2.get(key)).isNotNull();
		}
	}


}
