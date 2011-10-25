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

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Iterator;

import static org.testng.Assert.*;
import static org.testng.Assert.assertEquals;


public class BTreeTest {

	private static       String path = "/tmp/btree_spec";
	private static final Log    LOG  = LogFactory.getLog(BTreeTest.class);

	protected Integer key1;
	protected Integer key2;

	protected Integer                 value1;
	protected Integer                 value2;
	private   BTree<Integer, Integer> tree;

	private static Injector     injector;
	private static SecureRandom srand;

	static {
		injector = Guice.createInjector(new BTreeModule(path));
	}

	private BTree<Integer, Integer> createNewMultiMap() throws IOException {
		File f = new File(path);
		if (f.exists())
			f.delete();

		BTree<Integer, Integer> tree = getInstance();
		tree.initialize();
		return tree;
	}

	private static SecureRandom srand() {
		if (srand == null)
			srand = new SecureRandom();

		return srand;
	}

	public BTree<Integer, Integer> getInstance() {
		return injector.getInstance(Key.get(new TypeLiteral<BTree<Integer, Integer>>() {
		}));
	}


	public Integer createRandomKey() {
		return srand().nextInt();
	}


	public Integer createRandomValue() {
		// for String:
		// return (new BigInteger(130, srand())).toString(32);

		// for Integer
		return srand().nextInt();
	}


	public Integer createMaxKey() {
		return Integer.MAX_VALUE;
	}


	public Integer createMinKey() {
		return Integer.MIN_VALUE;
	}

	@BeforeMethod(alwaysRun = true)
	public void setUp() throws IOException {
		this.tree = createNewMultiMap();

		key1 = createRandomKey();
		do {
			key2 = createRandomKey();
		} while (key2.equals(key1));
		value1 = createRandomValue();
		value2 = createRandomValue();
	}

	protected void simpleTests() {
		int numOfEntries = tree.getNumberOfEntries();

		tree.add(key1, value2);
		assertTrue(tree.containsKey(key1));
		assertEquals(value2, tree.get(key1).get(0));
		assertEquals(numOfEntries + 1, tree.getNumberOfEntries());

		tree.remove(key1);
		assertFalse(tree.containsKey(key1));
		assertEquals(0, tree.get(key1).size());
		assertEquals(numOfEntries, tree.getNumberOfEntries());
	}

	protected void fill(int size) {
		for (int i = 0; i < size; i++) {
			tree.add(createRandomKey(), createRandomValue());
		}
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
		key1 = createMaxKey();
		key2 = createMinKey();
		removeWithValueArgumentShouldRemoveOnlyThisValue(key1, key2);
		tree.clear();
		removeWithValueArgumentShouldRemoveOnlyThisValue(key2, key1);
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

	@Test public void shouldWorkOnTheEdgeToCreateNewInnerNode() {
		int size = 170;
		fill(size);

		assertEquals(size, tree.getNumberOfEntries());
		simpleTests();
	}

	@Test public void iterator() {
		Integer val;

		key1 = createMinKey();
		key2 = createMaxKey();

		tree.add(key1, value1);
		tree.add(key1, value2);
		tree.add(key2, value2);

		Iterator<Integer> i = tree.getIterator();
		assertTrue(i.hasNext());
		val = i.next();
		assertTrue(val.equals(value1) || val.equals(value2));
		assertTrue(i.hasNext());
		val = i.next();
		assertTrue(val.equals(value1) || val.equals(value2));
		assertTrue(i.hasNext());
		assertEquals(value2, i.next());
		assertFalse(i.hasNext());
	}

	@Test
	public void shouldBeAbleToOpenAndLoad() throws IOException {
		Integer smaller, larger;
		if (key1.compareTo(key2) > 0) {
			larger = key1;
			smaller = key2;
		} else {
			smaller = key1;
			larger = key2;
		}

		shouldBeAbleToOpenAndLoad(smaller, larger);
		shouldBeAbleToOpenAndLoad(larger, smaller);
	}

	private void shouldBeAbleToOpenAndLoad(Integer key1, Integer key2) throws IOException {

		tree.initialize();
		tree.add(key1, value1);
		tree.add(key2, value2);
		tree.sync();

		tree = getInstance();
		tree.load();
		assertEquals(2, tree.getNumberOfEntries());
		assertEquals(value1, tree.get(key1).get(0));
		assertEquals(value2, tree.get(key2).get(0));
	}

	@Test(groups = "slow")
	public void shouldWorkWithMassiveValues() {
		int size = 100000;

		fill(size);

		assertEquals(size, tree.getNumberOfEntries());
		key1 = createMaxKey();
		simpleTests();
		key1 = createMinKey();
		simpleTests();
	}

	@Test(enabled = false)
	public void shouldNotHaveTooMuchOverhead() {
		int key = createRandomKey();
		int val = createRandomValue();

		int sizeForKey = Integer.SIZE / 8;
		int sizeForVal = Integer.SIZE / 8;

		// insert 10.000 K/V pairs
		int size = 100000;
		long start = System.currentTimeMillis();
		for (int i = 0; i < size; i++) {
			tree.add(key, val);
		}

		tree.sync();
		long end = System.currentTimeMillis();

		File file = new File(path);
		Long sizeOfData = (long) (size * (sizeForKey + sizeForVal));
		float realSizePercent = file.length() / sizeOfData * 100;

		System.out.println("====== BTREE: SIZE OVERHEAD TEST ======");
		System.out.println("key + value data inserted:" + sizeOfData / 1024 + "k");
		System.out.println("fileSize: " + file.length() / 1024 + "k (" + realSizePercent + "%)");
		System.out.println("time for insert w/ sync in millis: " + (end - start));
		//assertThat("current Size: " + realSizePercent + "%", realSizePercent, lessThan(1000f));
	}
}
