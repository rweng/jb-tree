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
import com.freshbourne.io.FileResourceManager;
import com.freshbourne.io.FileResourceManagerFactory;
import com.freshbourne.io.PageSize;
import com.freshbourne.serializer.FixedStringSerializer;
import com.freshbourne.serializer.IntegerSerializer;
import com.google.inject.*;
import com.google.inject.util.Modules;
import org.apache.log4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.*;

import static org.testng.Assert.*;


public class BTreeSmallTest {

	private static Injector                injector;
	private        BTree<Integer, Integer> tree;
	private static final Logger LOG = Logger.getLogger(BTreeSmallTest.class);

	// 3 keys, 4 values
	private static final int PAGE_SIZE = InnerNode.Header.size() + 3 * (2 * Integer.SIZE / 8) + Integer.SIZE / 8;


	private static BTreeFactory factory;

	static {
		injector = Guice.createInjector(
				Modules.override(new BTreeModule()).with(new AbstractModule() {
					@Override protected void configure() {
						bind(Integer.class).annotatedWith(PageSize.class).toInstance(PAGE_SIZE);
					}
				}));
		factory = injector.getInstance(BTreeFactory.class);
	}

	private static File getFile() {
		return new File("/tmp/btree-small-test-" + System.currentTimeMillis());
	}

	@BeforeMethod
	public void setUp() throws IOException {
		getFile().delete();
		tree = factory.get(getFile(), IntegerSerializer.INSTANCE, IntegerSerializer.INSTANCE,
				IntegerComparator.INSTANCE);
	}

	@Test
	public void ensurePageSizeIsSmall() throws IOException {
		assertEquals(PAGE_SIZE,
				injector.getInstance(FileResourceManagerFactory.class).get(getFile(), false).getPageSize());
	}

	@Test
	public void testsWorking() {
		assertTrue(true);
	}

	@Test
	public void testInitialize() throws IOException {
		tree.initialize();
		assertTrue(tree.isValid());
	}


	@Test
	public void testMultiLevelInsertForward() throws IOException {
		int count = 100;

		tree.initialize();

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

		tree.initialize();

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
		// create a new injector with large pagesize and string-serialization for 1000 bytes
		Injector newInjector = Guice.createInjector(Modules.override(new BTreeModule
				()).with(new AbstractModule() {
			@Override protected void configure() {
				bind(Integer.class).annotatedWith(PageSize.class).toInstance(PageSize.DEFAULT_PAGE_SIZE);
			}
		}));

		// initialize new btree
		getFile().delete();
		BTree<String, String> newTree =
				newInjector.getInstance(BTreeFactory.class).get(getFile(), FixedStringSerializer.INSTANCE_1000,
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

		FileResourceManager pm = new FileResourceManager(file);
		pm.open();

		BTree<Integer, String> btree =
				new BTree<Integer, String>(pm, IntegerSerializer.INSTANCE, FixedStringSerializer.INSTANCE,
						IntegerComparator.INSTANCE);

		btree.initialize();
		btree.sync();

		assertTrue(file.exists());
	}

	@Test
	public void factoryConstructor() throws IOException {
		File file = new File("/tmp/defaultBTreeModule");
		file.delete();


		Injector i = Guice.createInjector(new BTreeModule());
		BTreeFactory factory = i.getInstance(BTreeFactory.class);
		BTree<Integer, String> btree = factory.get(file, IntegerSerializer.INSTANCE, FixedStringSerializer.INSTANCE,
				IntegerComparator.INSTANCE);
		btree.initialize();
		btree.sync();

		assertTrue(file.exists());
	}

	@Test
	public void staticMethodConstructor() throws IOException {
		File file = new File("/tmp/btree-test");
		file.delete();

		BTree<Integer, String> btree = BTree.create(file, IntegerSerializer.INSTANCE, FixedStringSerializer.INSTANCE,
				IntegerComparator.INSTANCE);
		btree.initialize();
		btree.sync();

		assertTrue(file.exists());
	}

	@Test
	public void integerStringTree() throws IOException {
		File file = new File("/tmp/btree-test");
		file.delete();

		BTree<Integer, String> btree =
				BTree.create(file, IntegerSerializer.INSTANCE, FixedStringSerializer.INSTANCE_1000,
						IntegerComparator.INSTANCE);

		btree.initialize();

		int count = 50;

		for (int i = 0; i < count; i++) {
			LOG.debug("ROUND: " + i);

			btree.add(i, "" + i);
			btree.sync();

			BTree<Integer, String> btree2 =
					BTree.create(file, IntegerSerializer.INSTANCE, FixedStringSerializer.INSTANCE_1000,
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

		btree = BTree.create(file, IntegerSerializer.INSTANCE, FixedStringSerializer.INSTANCE_1000,
				IntegerComparator.INSTANCE);
		btree.load();

		Iterator<String> iterator = btree.getIterator();

		for (int i = 0; i < count; i++) {
			assertTrue(iterator.hasNext());
			LOG.debug("next value:" + iterator.next());
		}

		assertFalse(iterator.hasNext());

	}

	@Test
	public void iteratorsWithoutParameters() throws IOException {
		tree.initialize();
		fillTree(tree, 1000);
		Iterator<Integer> iterator = tree.getIterator();
		for (int i = 0; i < 1000; i++)
			assertEquals(i, (int) iterator.next());

		assertFalse(iterator.hasNext());
	}

	@Test
	public void ranges() throws IOException {
		tree.initialize();
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
	public void iteratorsWithStartEndGiven() throws IOException {
		tree.initialize();
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
		tree.initialize();
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


}
