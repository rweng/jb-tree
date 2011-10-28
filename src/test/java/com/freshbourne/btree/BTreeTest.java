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
import com.freshbourne.comparator.StringComparator;
import com.freshbourne.io.AutoSaveResourceManager;
import com.freshbourne.io.ResourceManagerBuilder;
import com.freshbourne.serializer.FixedStringSerializer;
import com.freshbourne.serializer.IntegerSerializer;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import org.apache.log4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.*;

import static org.fest.assertions.Assertions.assertThat;
import static org.testng.Assert.*;


public class BTreeTest {

	private BTree<Integer, Integer> tree;
	private static final Logger LOG  = Logger.getLogger(BTreeTest.class);
	private static final File   file = new File("/tmp/btree-small-test");


	private static final Integer key1 = 1;
	private static final Integer key2 = 2;

	private static final Integer value1 = 55;
	private static final Integer value2 = 99;

	// 3 keys, 4 values
	private static final int PAGE_SIZE = InnerNode.Header.size() + 3 * (2 * Integer.SIZE / 8) + Integer.SIZE / 8;
	private static AutoSaveResourceManager rm;

	BTreeTest() {
		rm = new ResourceManagerBuilder().useLock(true).pageSize(PAGE_SIZE).file(file).buildAutoSave();
	}

	@BeforeMethod
	public void setUp() throws IOException {
		file.delete();
		tree = BTree.create(rm, IntegerSerializer.INSTANCE, IntegerSerializer.INSTANCE,
				IntegerComparator.INSTANCE);
		tree.initialize();
	}

	@Test(groups = "skipBeforeFilter")
	public void testInitialize() throws IOException {
		file.delete();
		tree = BTree.create(rm, IntegerSerializer.INSTANCE, IntegerSerializer.INSTANCE,
				IntegerComparator.INSTANCE);
		assertThat(tree.isValid()).isFalse();
		tree.initialize();
		assertThat(tree.isValid()).isTrue();
	}


	@Test
	public void testMultiLevelInsertForward() throws IOException {
		int count = 100;

		for (int i = 0; i < count; i++) {

			assertTrue(tree.isValid());
			tree.add(i, i);
			tree.checkStructure();
			Iterator<Integer> iterator = tree.getIterator();

			int latest = iterator.next();
			for (int j = 0; j <= i - 1; j++) {
				int next = iterator.next();
				assertTrue(latest <= next);
				latest = next;
			}

			assertFalse(iterator.hasNext());
		}


		assertEquals(count, tree.getNumberOfEntries());
		Iterator<Integer> iterator = tree.getIterator();

		int latest = iterator.next();
		for (int i = 0; i < count - 1; i++) {
			int next = iterator.next();
			assertTrue(latest <= next);
			latest = next;
		}
		assertFalse(iterator.hasNext());
	}

	@Test
	public void testMultiLevelInsertBackward() throws IOException {
		int count = 100;

		for (int i = 0; i < count; i++) {

			assertTrue(tree.isValid());
			tree.add(count - i, count - i);
			tree.checkStructure();
			Iterator<Integer> iterator = tree.getIterator();

			int latest = iterator.next();
			for (int j = 0; j <= i - 1; j++) {
				int next = iterator.next();
				assertTrue(latest <= next);
				latest = next;
			}

			assertFalse(iterator.hasNext());
		}


		assertEquals(count, tree.getNumberOfEntries());
		Iterator<Integer> iterator = tree.getIterator();

		int latest = iterator.next();
		for (int i = 0; i < count - 1; i++) {
			int next = iterator.next();
			assertTrue(latest <= next);
			latest = next;
		}
		assertFalse(iterator.hasNext());
	}

	@Test
	public void testLargeKeyValues() throws IOException {
		// initialize new btree
		file.delete();
		AutoSaveResourceManager newRm = new ResourceManagerBuilder().file(file).buildAutoSave();
		BTree<String, String> newTree = BTree.create(newRm, FixedStringSerializer.INSTANCE_1000,
				FixedStringSerializer.INSTANCE_1000,
				StringComparator.INSTANCE);

		assertEquals(1000, newTree.getKeySerializer().getSerializedLength());
		assertEquals(1000, newTree.getValueSerializer().getSerializedLength());

		newTree.initialize();

		// do the actual test
		int count = 100;
		for (int i = 0; i < count; i++) {
			if (i == 24)
				LOG.debug("DEBUG");

			LOG.debug("i=" + i);
			newTree.add("" + i, "" + i);
			newTree.checkStructure();
		}

		Iterator<String> iterator = newTree.getIterator();
		for (int i = 0; i < count; i++) {
			assertTrue(iterator.hasNext());
			LOG.debug("got value: " + iterator.next());
		}
		assertFalse(iterator.hasNext());
	}

	@Test
	public void defaultBTreeModule() throws IOException {
		File file = new File("/tmp/defaultBTreeModule");
		file.delete();

		Injector i = Guice.createInjector(new BTreeModule(file.getAbsolutePath()));
		BTree<Integer, Integer> t = i.getInstance(
				Key.get(new TypeLiteral<BTree<Integer, Integer>>() {
				}));
		t.initialize();
		t.sync();

		assertTrue(file.exists());
	}

	@Test
	public void manualConstructor() throws IOException {
		String filePath = "/tmp/manualConstructorTestBtree";
		File file = new File(filePath);
		file.delete();

		AutoSaveResourceManager pm = new ResourceManagerBuilder().file(file).buildAutoSave();
		pm.open();

		BTree<Integer, String> btree = BTree.create(pm, IntegerSerializer.INSTANCE, FixedStringSerializer.INSTANCE,
				IntegerComparator.INSTANCE);

		btree.initialize();
		btree.sync();

		assertTrue(file.exists());
	}

	@Test
	public void staticMethodConstructor() throws IOException {

		BTree<Integer, String> btree =
				BTree.create(createResourceManager(true), IntegerSerializer.INSTANCE, FixedStringSerializer.INSTANCE,
						IntegerComparator.INSTANCE);
		btree.initialize();
		btree.sync();
	}

	private AutoSaveResourceManager createResourceManager(boolean reset) {
		if (reset)
			file.delete();
		return new ResourceManagerBuilder().file(file).buildAutoSave();
	}

	@Test
	public void integerStringTree() throws IOException {
		AutoSaveResourceManager rm = createResourceManager(true);
		BTree<Integer, String> btree =
				BTree.create(rm, IntegerSerializer.INSTANCE, FixedStringSerializer.INSTANCE_1000,
						IntegerComparator.INSTANCE);
		btree.initialize();

		int count = 50;

		for (int i = 0; i < count; i++) {
			LOG.debug("ROUND: " + i);

			btree.add(i, "" + i);
			btree.sync();

			BTree<Integer, String> btree2 =
					BTree.create(rm, IntegerSerializer.INSTANCE, FixedStringSerializer.INSTANCE_1000,
							IntegerComparator.INSTANCE);
			btree2.load();


			Iterator<String> iterator = btree2.getIterator();

			for (int j = 0; j <= i; j++) {
				assertTrue(iterator.hasNext());
				LOG.debug("next value:" + iterator.next());
			}

			assertFalse(iterator.hasNext());


		}

		btree.sync();

		btree = BTree.create(rm, IntegerSerializer.INSTANCE,
				FixedStringSerializer.INSTANCE_1000,
				IntegerComparator.INSTANCE);
		btree.load();

		Iterator<String> iterator = btree.getIterator();

		for (int i = 0; i < count; i++) {
			assertTrue(iterator.hasNext());
			LOG.debug("next value:" + iterator.next());
		}

		assertFalse(iterator.hasNext());

	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void exceptionIfValidTreeIsLoaded() throws IOException {
		tree.load();
	}


	@Test(expectedExceptions = IllegalStateException.class)
	public void exceptionIfValidTreeIsInitialized() throws IOException {
		tree.initialize();
	}

	@Test
	public void iteratorsWithoutParameters() throws IOException {
		fillTree(tree, 1000);
		Iterator<Integer> iterator = tree.getIterator();
		for (int i = 0; i < 1000; i++)
			assertThat(iterator.next()).isEqualTo(i);

		assertThat(iterator.hasNext()).isFalse();
	}

	@Test
	public void ranges() throws IOException {
		fillTree(tree, 100);
		List<Range<Integer>> rangeList = new ArrayList<Range<Integer>>();
		rangeList.add(new Range(-5, 5));

		// 49 - 56
		rangeList.add(new Range(50, 55));
		rangeList.add(new Range(52, 53));
		rangeList.add(new Range(49, 53));
		rangeList.add(new Range(52, 56));

		rangeList.add(new Range(95, 1000));

		Iterator<Integer> iterator = tree.getIterator(rangeList);

		for (int i = 0; i <= 5; i++) {
			assertEquals(i, (int) iterator.next());
		}

		for (int i = 49; i <= 56; i++)
			assertEquals(i, (int) iterator.next());

		for (int i = 95; i < 100; i++) {
			assertEquals(i, (int) iterator.next());
		}
		assertFalse(iterator.hasNext());


		rangeList.clear();
		iterator = tree.getIterator(rangeList);
		for (int i = 0; i < 100; i++) {
			assertNotNull(iterator.next());
		}
		assertNull(iterator.next());

		rangeList.add(new Range(null, 50));
		iterator = tree.getIterator(rangeList);
		for (int i = 0; i <= 50; i++) {
			assertNotNull(iterator.next());
		}
		assertNull(iterator.next());

		rangeList.add(new Range<Integer>(0, 75));
		iterator = tree.getIterator(rangeList);
		for (int i = 0; i <= 75; i++) {
			assertNotNull(iterator.next());
		}
		assertNull(iterator.next());

		rangeList.clear();
		rangeList.add(new Range(50, null));
		iterator = tree.getIterator(rangeList);
		for (int i = 50; i <= 99; i++) {
			assertNotNull(iterator.next());
		}
		assertNull(iterator.next());

		rangeList.add(new Range(25, 99));
		iterator = tree.getIterator(rangeList);
		for (int i = 25; i <= 99; i++) {
			assertNotNull(iterator.next());
		}
		assertNull(iterator.next());


		rangeList.add(new Range(null, null));
		rangeList.add(new Range(null, 23));
		iterator = tree.getIterator(rangeList);
		for (int i = 0; i <= 99; i++) {
			assertNotNull(iterator.next());
		}
		assertNull(iterator.next());


		// with null as search range
		iterator = tree.getIterator(null);
		for (int i = 0; i <= 99; i++) {
			assertNotNull(iterator.next());
		}
		assertNull(iterator.next());

		// with an empty list as search range
		rangeList.clear();
		iterator = tree.getIterator(rangeList);
		for (int i = 0; i <= 99; i++) {
			assertNotNull(iterator.next());
		}
		assertNull(iterator.next());
	}

	@Test
	public void shouldBeEmptyAfterCreation() {
		assertEquals(0, tree.getNumberOfEntries());
	}

	@Test
	public void shouldContainAddedEntries() {
		tree.add(key1, value1);
		assertTrue(tree.containsKey(key1));
		assertEquals(1, tree.get(key1).size());
		assertEquals(value1, tree.get(key1).get(0));
		assertEquals(1, tree.getNumberOfEntries());

		tree.add(key1, value2);
		assertTrue(tree.containsKey(key1));
		assertEquals(2, tree.get(key1).size());
		assertTrue(tree.get(key1).contains(value1));
		assertTrue(tree.get(key1).contains(value2));
		assertEquals(2, tree.getNumberOfEntries());

		tree.add(key2, value2);
		assertTrue(tree.containsKey(key2));
		assertEquals(1, tree.get(key2).size());
		assertTrue(tree.get(key1).contains(value2));
		assertTrue(tree.get(key1).contains(value1));
		assertTrue(tree.get(key1).size() == 2);
		assertEquals(3, tree.getNumberOfEntries());
	}

	@Test
	public void shouldReturnEmptyArrayIfKeyNotFound() {
		assertEquals(0, tree.get(key1).size());
	}

	@Test
	public void shouldBeAbleToRemoveInsertedEntries() {
		tree.add(key1, value1);
		assertTrue(tree.containsKey(key1));
		tree.remove(key1);
		assertFalse(tree.containsKey(key1));
		assertEquals(0, tree.getNumberOfEntries());
	}

	@Test
	public void clearShouldRemoveAllElements() {
		tree.add(key1, value1);
		tree.add(key2, value2);
		assertEquals(2, tree.getNumberOfEntries());
		tree.clear();
		assertEquals(0, tree.getNumberOfEntries());
	}

	@Test
	public void removeWithValueArgumentShouldRemoveOnlyThisValue() {
		int k1 = Integer.MAX_VALUE;
		int k2 = Integer.MIN_VALUE;
		removeWithValueArgumentShouldRemoveOnlyThisValue(k1, k2);
		tree.clear();
		removeWithValueArgumentShouldRemoveOnlyThisValue(k2, k1);
	}

	public void removeWithValueArgumentShouldRemoveOnlyThisValue(Integer key1, Integer key2) {
		tree.add(key1, value1);
		tree.add(key1, value2);
		tree.add(key2, value2);

		assertEquals(3, tree.getNumberOfEntries());
		assertEquals(2, tree.get(key1).size());
		assertEquals(1, tree.get(key2).size());

		tree.remove(key1, value2);
		assertEquals(2, tree.getNumberOfEntries());
		assertEquals(1, tree.get(key1).size());
		assertEquals(value1, tree.get(key1).get(0));
		assertEquals(value2, tree.get(key2).get(0));
	}

	@Test
	public void removeWithOnlyKeyArgumentShouldRemoveAllValues() {
		tree.add(key1, value1);
		tree.add(key1, value2);
		tree.add(key2, value2);

		assertEquals(3, tree.getNumberOfEntries());
		tree.remove(key1);
		assertEquals(1, tree.getNumberOfEntries());
		assertEquals(0, tree.get(key1).size());
	}

	private void simpleTests(Integer keyToAdd) {
		int numOfEntries = tree.getNumberOfEntries();

		tree.add(keyToAdd, value2);
		assertThat(tree.containsKey(keyToAdd)).isTrue();
		assertThat(tree.get(keyToAdd).get(0)).isEqualTo(value2);
		assertThat(tree.getNumberOfEntries()).isEqualTo(numOfEntries+1);

		tree.remove(keyToAdd);
		assertThat(tree.containsKey(keyToAdd)).isFalse();

		assertThat(tree.get(keyToAdd).size()).isEqualTo(0);
		assertThat(tree.getNumberOfEntries()).isEqualTo(numOfEntries);
	}

	protected void fill(int size) {
		for (int i = 0; i < size; i++) {
			tree.add(i, i);
		}
	}

	@Test public void shouldWorkOnTheEdgeToCreateNewInnerNode() {
		int size = 170;
		fill(size);

		assertThat(tree.getNumberOfEntries()).isEqualTo(size);
		simpleTests(5000);
	}

	@Test public void iterator() {
		Integer val;

		int k1 = Integer.MIN_VALUE;
		int k2 = Integer.MAX_VALUE;

		tree.add(k1, value1);
		tree.add(k1, value2);
		tree.add(k2, value2);

		Iterator<Integer> i = tree.getIterator();
		assertThat(i.hasNext()).isTrue();
		val = i.next();
		assertThat(val.equals(value1) || val.equals(value2)).isTrue();
		assertThat(i.hasNext()).isTrue();
		val = i.next();
		assertThat(val.equals(value1) || val.equals(value2)).isTrue();
		assertThat(i.hasNext()).isTrue();
		assertThat(i.next()).isEqualTo(value2);
		assertThat(i.hasNext()).isFalse();
	}

	@Test(groups = "slow")
	public void shouldWorkWithMassiveValues() {
		int size = 100000;

		fill(size);

		assertEquals(size, tree.getNumberOfEntries());
		simpleTests(Integer.MAX_VALUE);
		simpleTests(Integer.MIN_VALUE);
	}

	@Test(enabled = false)
	public void shouldNotHaveTooMuchOverhead() {
		int sizeForKey = Integer.SIZE / 8;
		int sizeForVal = Integer.SIZE / 8;

		// insert 10.000 K/V pairs
		int size = 100000;
		long start = System.currentTimeMillis();
		for (int i = 0; i < size; i++) {
			tree.add(i, i);
		}

		tree.sync();
		long end = System.currentTimeMillis();

		Long sizeOfData = (long) (size * (sizeForKey + sizeForVal));
		float realSizePercent = file.length() / sizeOfData * 100;

		System.out.println("====== BTREE: SIZE OVERHEAD TEST ======");
		System.out.println("key + value data inserted:" + sizeOfData / 1024 + "k");
		System.out.println("fileSize: " + file.length() / 1024 + "k (" + realSizePercent + "%)");
		System.out.println("time for insert w/ sync in millis: " + (end - start));
		//assertThat("current Size: " + realSizePercent + "%", realSizePercent, lessThan(1000f));
	}

	@Test
	public void shouldBeAbleToOpenAndLoad() throws IOException {
		Integer smaller, larger;

		smaller = 10;
		larger = 20;

		shouldBeAbleToOpenAndLoad(smaller, larger);
		shouldBeAbleToOpenAndLoad(larger, smaller);
	}

	@Test
	public void iteratorsWithStartEndGiven() throws IOException {
		fillTree(tree, 100);
		Iterator<Integer> iterator = tree.getIterator();
		for (int i = 0; i < 100; i++)
			assertEquals(i, (int) iterator.next());
		assertFalse(iterator.hasNext());

		iterator = tree.getIterator(-50, 50);
		for (int i = 0; i <= 50; i++)
			assertEquals(i, (int) iterator.next());
		assertFalse(iterator.hasNext());

		iterator = tree.getIterator(50, 150);
		for (int i = 50; i < 100; i++)
			assertEquals(i, (int) iterator.next());
		assertFalse(iterator.hasNext());
	}

	private void fillTree(BTree<Integer, Integer> tree, int count) {
		for (int i = 0; i < count; i++) {
			tree.add(i, i);
		}
	}

	@Test
	public void close() throws IOException {
		fillTree(tree, 100);
		tree.close();
		assertFalse(tree.isValid());
		assertFalse(tree.getResourceManager().isOpen());
	}

	@Test
	public void bulkInsert1Layer() throws IOException {
		bulkInsert(tree.getMaxLeafKeys()); // exactly one leaf
	}

	@Test
	public void bulkInsert2Layers() throws IOException {
		bulkInsert((tree.getMaxInnerKeys() + 1) * tree.getMaxLeafKeys());

		// make sure the test is checking 2 layers
		assertEquals(2, tree.getDepth());
	}

	/**
	 * causes java.lang.IllegalArgumentException: for bulkinsert, you must have at least 2 page ids and keys.size() ==
	 * (pageIds.size() - 1)
	 */
	@Test
	public void bulkInsertWithOnlyOnePageForNextInnerNode() throws IOException {
		bulkInsert((tree.getMaxInnerKeys() + 1) * tree.getMaxLeafKeys() + 1);
	}

	@Test
	public void bulkInsert3Layers() throws IOException {
		bulkInsert((tree.getMaxInnerKeys() + 1) * (tree.getMaxInnerKeys() + 1) * tree.getMaxLeafKeys());

		// just to make sure that the test is really checking 3 layers
		assertTrue(tree.getDepth() >= 3);
	}


	@Test
	public void bulkInsertWithSortAndCloseAndRange() throws IOException {
		int count = 50;
		int from = 10;
		int to = 39;
		int realCount = to - from + 1;

		AbstractMap.SimpleEntry<Integer, Integer>[] kvs = new AbstractMap.SimpleEntry[count];
		SecureRandom srand = new SecureRandom();

		List<Integer> keys = new LinkedList<Integer>();

		for (int i = 0; i < count; i++) {
			int newKey = srand.nextInt();

			while (keys.contains(newKey)) {
				newKey = srand.nextInt();
			}

			if (i >= from && i <= to)
				keys.add(newKey);
			kvs[i] = new AbstractMap.SimpleEntry<Integer, Integer>(newKey, newKey);
		}

		tree.bulkInitialize(kvs, from, to, false);

		assertEquals(to - from + 1, tree.getNumberOfEntries());
		tree.checkStructure();

		Collections.sort(keys, IntegerComparator.INSTANCE);
		for (int i = 0; i < realCount; i++) {
			assertEquals(1, tree.get(keys.get(i)).size(), "size problem with key " + i);
			assertEquals(keys.get(i), tree.get(keys.get(i)).get(0));
		}

		tree.close();
		tree.load();

		// test everything again
		assertEquals(realCount, tree.getNumberOfEntries());
		tree.checkStructure();

		for (int i = 0; i < realCount; i++) {
			assertEquals(1, tree.get(keys.get(i)).size(), "size problem with key " + i);
			assertEquals(keys.get(i), tree.get(keys.get(i)).get(0));
		}
	}

	public void bulkInsert(int count) throws IOException {
		@SuppressWarnings("unchecked")
		AbstractMap.SimpleEntry<Integer, Integer>[] kvs = new AbstractMap.SimpleEntry[count];

		for (int i = 0; i < count; i++) {
			kvs[i] = new AbstractMap.SimpleEntry<Integer, Integer>(i, i);
		}

		tree.bulkInitialize(kvs, true);

		// check if its correct
		LOG.debug("checking bulkinsert results...");
		assertEquals(count, tree.getNumberOfEntries());

		tree.checkStructure();

		for (int i = 0; i < count; i++) {
			assertTrue(tree.get(kvs[i].getKey()).size() > 0, "tree doesn't have key " + i);
			assertEquals(1, tree.get(kvs[i].getKey()).size(), "size problem with key " + i);
			assertEquals(kvs[i].getValue(), tree.get(kvs[i].getKey()).get(0));
		}
	}


	private void shouldBeAbleToOpenAndLoad(Integer key1, Integer key2) throws IOException {
		Integer value1 = 1;
		Integer value2 = 2;

		tree.add(key1, value1);
		tree.add(key2, value2);
		tree.close();

		tree = BTree.create(rm, IntegerSerializer.INSTANCE, IntegerSerializer.INSTANCE, IntegerComparator.INSTANCE);
		tree.load();
		assertEquals(2, tree.getNumberOfEntries());
		assertEquals(value1, tree.get(key1).get(0));
		assertEquals(value2, tree.get(key2).get(0));
	}

}
