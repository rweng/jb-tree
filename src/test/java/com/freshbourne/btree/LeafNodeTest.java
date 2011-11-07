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
import com.freshbourne.io.ResourceManager;
import com.freshbourne.io.ResourceManagerBuilder;
import com.freshbourne.serializer.IntegerSerializer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import static org.fest.assertions.Assertions.assertThat;
import static org.testng.Assert.*;

public class LeafNodeTest {

	private final static File file = new File("/tmp/LeafNodeTest");
	private LeafNode<Integer, Integer> leaf;
	private LeafPageManager<Integer, Integer> lpm;

	private Integer key1 = 1;
	private Integer key2 = 2;

	private Integer value1 = 101;
	private Integer value2 = 102;
	private ResourceManager rm;

	LeafNodeTest(){
		file.delete();
		this.rm = new ResourceManagerBuilder().file(file).open().useCache(false).build();
	}

	@BeforeMethod
	public void setUp() throws IOException {
		rm.clear();
		lpm = BTree.create(rm, IntegerSerializer.INSTANCE, IntegerSerializer.INSTANCE, IntegerComparator.INSTANCE).getLeafPageManager();
		leaf = lpm.createPage();
	}

	@Test public void shouldBeAbleToInsertAndGet() {
		leaf.insert(key1, value1);
		assertTrue(leaf.containsKey(key1));
		assertEquals(1, leaf.getNumberOfEntries());
		assertEquals(1, leaf.get(key1).size());
		assertEquals(value1, leaf.get(key1).get(0));
	}

	@Test public void shouldBeAbleToGetLastKeyAndPointer() {
		leaf.insert(key1, value1);
		assertNotNull(leaf.getLastLeafKey());
		assertNotNull(leaf.getLastLeafKeySerialized());

		leaf.insert(key2, value2);
		assertNotNull(leaf.getLastLeafKey());
		assertNotNull(leaf.getLastLeafKeySerialized());
	}

	@Test public void shouldAlwaysWorkAfterReload() {
		for (int i = 0; i < 5; i++) {
			leaf.insert(key1, value1);
		}
		leaf.insert(key2, value2);
		assertEquals(6, leaf.getNumberOfEntries());
		leaf.load();
		assertEquals(6, leaf.getNumberOfEntries());
		assertEquals(1, leaf.get(key2).size());

	}

	@Test public void shouldAtSomePointReturnAValidAdjustmentAction() {
		AdjustmentAction<Integer, Integer> action;
		do {
			action = leaf.insert(key1, value1);
		} while (action == null);

		assertNotNull(leaf.getLastLeafKey());
		assertEquals(AdjustmentAction.ACTION.INSERT_NEW_NODE, action.getAction());

		assertNotNull(action.getSerializedKey());


		// this should still work and not throw an exception
		stateTest(leaf);
		final LeafNode<Integer, Integer> newLeaf = lpm.getPage(action.getPageId());
		;
		stateTest(newLeaf);
	}

	private void stateTest(final LeafNode<Integer, Integer> leaf) {
		final Integer k = leaf.getLastLeafKey();
		assertNotNull(leaf.get(k));
		assertTrue(leaf.containsKey(k));

		// all keys should be accessible
		for (int i = 0; i < leaf.getNumberOfEntries(); i++) {
			final Integer key = leaf.getKeyAtPosition(i);
			assertNotNull(k);
			assertTrue(leaf.containsKey(key));

		}
		assertEquals(k, leaf.getKeyAtPosition(leaf.getNumberOfEntries() - 1));

	}

	@Test
	public void iterators() {
		fillLeaf(leaf, 10);

		Iterator<Integer> iterator = leaf.getIterator(-5, 5);
		for (int i = 0; i <= 5; i++)
			assertEquals(i, (int) iterator.next());
		assertFalse(iterator.hasNext());

		iterator = leaf.getIterator(5, 15);
		for (int i = 5; i < 10; i++)
			assertEquals(i, (int) iterator.next());
		assertFalse(iterator.hasNext());

		iterator = leaf.getIterator(0, 9);
		for (int i = 0; i < 10; i++)
			assertEquals(i, (int) iterator.next());
		assertFalse(iterator.hasNext());


		iterator = leaf.getIterator(5, null);
		for (int i = 5; i < 10; i++)
			assertEquals(i, (int) iterator.next());
		assertFalse(iterator.hasNext());

		iterator = leaf.getIterator(null, 5);
		for (int i = 0; i <= 5; i++)
			assertEquals(i, (int) iterator.next());
		assertFalse(iterator.hasNext());

		iterator = leaf.getIterator(null, null);
		for (int i = 0; i < 10; i++)
			assertEquals(i, (int) iterator.next());
		assertFalse(iterator.hasNext());
	}


	private void fillLeaf(final LeafNode<Integer, Integer> leaf, final int count) {
		for (int i = 0; i < count; i++) {
			leaf.insert(i, i);
		}
	}

	@Test
	public void shouldContainAddedEntries() {
		leaf.insert(key1, value1);
		assertTrue(leaf.containsKey(key1));
		assertEquals(1, leaf.get(key1).size());
		assertEquals(value1, leaf.get(key1).get(0));
		assertEquals(1, leaf.getNumberOfEntries());

		leaf.insert(key1, value2);
		assertTrue(leaf.containsKey(key1));
		assertEquals(2, leaf.get(key1).size());
		assertTrue(leaf.get(key1).contains(value1));
		assertTrue(leaf.get(key1).contains(value2));
		assertEquals(2, leaf.getNumberOfEntries());

		leaf.insert(key2, value2);
		assertTrue(leaf.containsKey(key2));
		assertEquals(1, leaf.get(key2).size());
		assertTrue(leaf.get(key1).contains(value2));
		assertTrue(leaf.get(key1).contains(value1));
		assertTrue(leaf.get(key1).size() == 2);
		assertEquals(3, leaf.getNumberOfEntries());
	}

	@Test
	public void removeWithValueArgumentShouldRemoveOnlyThisValue() {
		leaf.insert(key1, value1);
		leaf.insert(key1, value2);
		leaf.insert(key2, value2);

		assertEquals(3, leaf.getNumberOfEntries());
		leaf.remove(key1, value2);
		assertEquals(1, leaf.get(key1).size());
		assertEquals(value1, leaf.get(key1).get(0));
		assertEquals(value2, leaf.get(key2).get(0));
	}


	@Test
	public void prependEntriesShouldWork() {
		final int leaf2Id = lpm.createPage().getId();


		int totalInserted = 0;

		// fill leaf
		for (int i = 0; i < leaf.getMaximalNumberOfEntries(); i++) {
			assertThat(leaf.insert(i, i)).isNull();
			totalInserted++;
		}

		// in testPrepend, one entry is inserted
		testPrepend(leaf, lpm.getPage(leaf2Id));
		totalInserted++;

		assertThat(leaf.getNumberOfEntries() + lpm.getPage(leaf2Id).getNumberOfEntries()).isEqualTo(totalInserted);

		// should work again, when we have to actually move some entries in leaf2
		for (int i = leaf.getNumberOfEntries(); i < leaf
				.getMaximalNumberOfEntries(); i++) {
			assertThat(leaf.insert(-1 * i, i)).isNull();
			totalInserted++;
		}

		testPrepend(leaf, lpm.getPage(leaf2Id));
		totalInserted++;
		assertThat(leaf.getNumberOfEntries() + lpm.getPage(leaf2Id).getNumberOfEntries()).isEqualTo(totalInserted);

	}

	/** in testPrepend, one entry is inserted*
	 * @param leaf1
	 * @param leaf2*/
	private void testPrepend(final LeafNode<Integer, Integer> leaf1, final LeafNode<Integer, Integer> leaf2) {
		leaf1.setNextLeafId(leaf2.getId());

		// insert key so that move should happen
		final AdjustmentAction<Integer, Integer> action = leaf1.insert(1, 1);

		// an update key action should be passed up
		assertThat(action).isNotNull();

		// make sure leaf structures are in tact
		assertThat(leaf1.getKeyAtPosition(leaf1.getNumberOfEntries() - 1)).isEqualTo(leaf1.getLastLeafKey());

		for (final int key : leaf1.getKeySet()) {
			assertThat(leaf1.get(key)).isNotNull();
		}

		for (final int key : leaf2.getKeySet()) {
			assertThat(leaf2.get(key)).isNotNull();
		}
	}


}
