/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 *
 * http://creativecommons.org/licenses/by-nc/3.0/
 *
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2011 "Robin Wenglewski <robin@wenglewski.de>"
 */
package de.rwhq.btree;


import de.rwhq.comparator.IntegerComparator;
import de.rwhq.io.rm.ResourceManager;
import de.rwhq.io.rm.ResourceManagerBuilder;
import de.rwhq.serializer.IntegerSerializer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import static org.fest.assertions.Assertions.assertThat;

public class LeafNodeTest {
	private static final Log LOG = LogFactory.getLog(LeafNodeTest.class);

	private final static File file = new File("/tmp/LeafNodeTest");
	private LeafNode<Integer, Integer> leaf;
	private LeafPageManager<Integer, Integer> lpm;

	private Integer key1 = 1;
	private Integer key2 = 2;

	private Integer value1 = 101;
	private Integer value2 = 102;
	private ResourceManager rm;

	public LeafNodeTest(){
		file.delete();
		this.rm = new ResourceManagerBuilder().file(file).open().cacheSize(0).build();
	}

	@Before
	public void setUp() throws IOException {
		LOG.warn("setting up leafnodetest");
		rm.clear();
		lpm = BTree.create(rm, IntegerSerializer.INSTANCE, IntegerSerializer.INSTANCE, IntegerComparator.INSTANCE).getLeafPageManager();
		leaf = lpm.createPage();
	}

	@Test
	public void shouldBeAbleToInsertAndGet() {
		leaf.insert(key1, value1);
		assertThat(leaf.containsKey(key1)).isTrue();
		assertThat( leaf.getNumberOfEntries()).isEqualTo(1);
		assertThat( leaf.get(key1).size()).isEqualTo(1);
		assertThat( leaf.get(key1).get(0)).isEqualTo(value1);
	}

	@Test public void shouldBeAbleToGetLastKeyAndPointer() {
		leaf.insert(key1, value1);
		assertThat(leaf.getLastLeafKey()).isNotNull();
		assertThat(leaf.getLastLeafKeySerialized()).isNotNull();

		leaf.insert(key2, value2);
		assertThat(leaf.getLastLeafKey()).isNotNull();
		assertThat(leaf.getLastLeafKeySerialized()).isNotNull();
	}

	@Test public void shouldAlwaysWorkAfterReload() {
		for (int i = 0; i < 5; i++) {
			leaf.insert(key1, value1);
		}
		leaf.insert(key2, value2);
		assertThat( leaf.getNumberOfEntries()).isEqualTo(6);
		leaf.load();
		assertThat( leaf.getNumberOfEntries()).isEqualTo(6);
		assertThat( leaf.get(key2).size()).isEqualTo(1);

	}

	@Test public void shouldAtSomePointReturnAValidAdjustmentAction() {
		AdjustmentAction<Integer, Integer> action;
		do {
			action = leaf.insert(key1, value1);
		} while (action == null);

		assertThat(leaf.getLastLeafKey()).isNotNull();
		assertThat( action.getAction()).isEqualTo(AdjustmentAction.ACTION.INSERT_NEW_NODE);

		assertThat(action.getSerializedKey()).isNotNull();


		// this should still work and not throw an exception
		stateTest(leaf);
		final LeafNode<Integer, Integer> newLeaf = lpm.getPage(action.getPageId());
		;
		stateTest(newLeaf);
	}

	private void stateTest(final LeafNode<Integer, Integer> leaf) {
		final Integer k = leaf.getLastLeafKey();
		assertThat(leaf.get(k)).isNotNull();
		assertThat(leaf.containsKey(k)).isTrue();

		// all keys should be accessible
		for (int i = 0; i < leaf.getNumberOfEntries(); i++) {
			final Integer key = leaf.getKeyAtPosition(i);
			assertThat(k).isNotNull();
			assertThat(leaf.containsKey(key)).isTrue();

		}
		assertThat( leaf.getKeyAtPosition(leaf.getNumberOfEntries() - 1)).isEqualTo(k);

	}

	@Test
	public void iterators() {
		fillLeaf(leaf, 10);

		Iterator<Integer> iterator = leaf.getIterator(-5, 5);
		for (int i = 0; i <= 5; i++)
			assertThat( (int) iterator.next()).isEqualTo(i);
		assertThat(iterator.hasNext()).isFalse();

		iterator = leaf.getIterator(5, 15);
		for (int i = 5; i < 10; i++)
			assertThat( (int) iterator.next()).isEqualTo(i);
		assertThat(iterator.hasNext()).isFalse();

		iterator = leaf.getIterator(0, 9);
		for (int i = 0; i < 10; i++)
			assertThat( (int) iterator.next()).isEqualTo(i);
		assertThat(iterator.hasNext()).isFalse();


		iterator = leaf.getIterator(5, null);
		for (int i = 5; i < 10; i++)
			assertThat( (int) iterator.next()).isEqualTo(i);
		assertThat(iterator.hasNext()).isFalse();

		iterator = leaf.getIterator(null, 5);
		for (int i = 0; i <= 5; i++)
			assertThat( (int) iterator.next()).isEqualTo(i);
		assertThat(iterator.hasNext()).isFalse();

		iterator = leaf.getIterator(null, null);
		for (int i = 0; i < 10; i++)
			assertThat( (int) iterator.next()).isEqualTo(i);
		assertThat(iterator.hasNext()).isFalse();
	}

	@Test
	public void iteratorStartingInTheMiddle(){
		fillLeaf(leaf, 10);
		Iterator<Integer> iterator = leaf.getIterator(5, null);
		for(int i = 5; i<10;i++)
			assertThat(iterator.next()).isEqualTo(i);
		
		assertThat(iterator.next()).isNull();
	}


	private void fillLeaf(final LeafNode<Integer, Integer> leaf, final int count) {
		for (int i = 0; i < count; i++) {
			leaf.insert(i, i);
		}
	}

	@Test
	public void shouldContainAddedEntries() {
		leaf.insert(key1, value1);
		assertThat(leaf.containsKey(key1)).isTrue();
		assertThat( leaf.get(key1).size()).isEqualTo(1);
		assertThat( leaf.get(key1).get(0)).isEqualTo(value1);
		assertThat( leaf.getNumberOfEntries()).isEqualTo(1);

		leaf.insert(key1, value2);
		assertThat(leaf.containsKey(key1)).isTrue();
		assertThat( leaf.get(key1).size()).isEqualTo(2);
		assertThat(leaf.get(key1).contains(value1)).isTrue();
		assertThat(leaf.get(key1).contains(value2)).isTrue();
		assertThat( leaf.getNumberOfEntries()).isEqualTo(2);

		leaf.insert(key2, value2);
		assertThat(leaf.containsKey(key2)).isTrue();
		assertThat( leaf.get(key2).size()).isEqualTo(1);
		assertThat(leaf.get(key1).contains(value2)).isTrue();
		assertThat(leaf.get(key1).contains(value1)).isTrue();
		assertThat(leaf.get(key1).size() == 2).isTrue();
		assertThat( leaf.getNumberOfEntries()).isEqualTo(3);
	}

	@Test
	public void removeWithValueArgumentShouldRemoveOnlyThisValue() {
		leaf.insert(key1, value1);
		leaf.insert(key1, value2);
		leaf.insert(key2, value2);

		assertThat( leaf.getNumberOfEntries()).isEqualTo(3);
		leaf.remove(key1, value2);
		assertThat( leaf.get(key1).size()).isEqualTo(1);
		assertThat( leaf.get(key1).get(0)).isEqualTo(value1);
		assertThat( leaf.get(key2).get(0)).isEqualTo(value2);
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
