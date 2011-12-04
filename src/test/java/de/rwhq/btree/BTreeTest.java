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
import de.rwhq.comparator.StringComparator;
import de.rwhq.io.rm.ResourceManager;
import de.rwhq.io.rm.ResourceManagerBuilder;
import de.rwhq.serializer.FixedStringSerializer;
import de.rwhq.serializer.IntegerSerializer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.*;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;


@RunWith(Enclosed.class)
public class BTreeTest {
	private static final Log LOG  = LogFactory.getLog(BTreeTest.class);
	private static final File   file = new File("/tmp/btree-small-test");


	private static final Integer key1 = 1;
	private static final Integer key2 = 2;

	private static final Integer value1 = 55;
	private static final Integer value2 = 99;

	// 3 keys, 4 values
	private static final int PAGE_SIZE = InnerNode.Header.size() + 3 * (2 * Integer.SIZE / 8) + Integer.SIZE / 8;
	private static ResourceManager rm;

	private static BTree<Integer, Integer> tree;

	public static BTree<Integer, Integer> createNewTree(int pageSize) throws IOException {
		file.delete();
		rm = new ResourceManagerBuilder().useLock(true).pageSize(pageSize).file(file).cacheSize(0).open().build();

		return createNewTree(rm);
	}

	static BTree<Integer, Integer> createNewTree(ResourceManager rm) throws IOException {
		BTree<Integer, Integer> tree = BTree.create(rm, IntegerSerializer.INSTANCE, IntegerSerializer.INSTANCE,
				IntegerComparator.INSTANCE);
		tree.initialize();
		return tree;
	}

	private static void simpleTests(BTree tree, final Integer keyToAdd) {
		final int numOfEntries = tree.getNumberOfEntries();

		tree.add(keyToAdd, value2);
		assertThat(tree.containsKey(keyToAdd)).isTrue();
		assertThat(tree.get(keyToAdd).get(0)).isEqualTo(value2);
		assertThat(tree.getNumberOfEntries()).isEqualTo(numOfEntries + 1);

		tree.remove(keyToAdd);
		assertThat(tree.containsKey(keyToAdd)).isFalse();

		assertThat(tree.get(keyToAdd).size()).isEqualTo(0);
		assertThat(tree.getNumberOfEntries()).isEqualTo(numOfEntries);
	}


	static void fillTreeWithStructureCheck(final BTree<Integer, Integer> tree,
	                                       final int count) throws InterruptedException {
		for (int i = 0; i < count; i++) {
			tree.add(i, i);
			tree.checkStructure();
		}
	}

	static void fillTree(final BTree<Integer, Integer> tree, final int count) throws InterruptedException {
		for (int i = 0; i < count; i++) {
			tree.add(i, i);
		}
	}


	public static class Base {
		@Before
		public void setUp() throws IOException {
			tree = createNewTree(PAGE_SIZE);
		}

		@Test
		public void removeWithValueArgumentShouldRemoveOnlyThisValue() throws IOException {
			final int k1 = Integer.MAX_VALUE;
			final int k2 = Integer.MIN_VALUE;
			removeWithValueArgumentShouldRemoveOnlyThisValue(k1, k2);
			tree.clear();
			removeWithValueArgumentShouldRemoveOnlyThisValue(k2, k1);
		}

		@Test
		public void testInitialize() throws IOException {
			rm.clear();
			tree = BTree.create(rm, IntegerSerializer.INSTANCE, IntegerSerializer.INSTANCE,
					IntegerComparator.INSTANCE);
			assertThat(tree.isValid()).isFalse();
			tree.initialize();
			assertThat(tree.isValid()).isTrue();
		}


		@Test
		public void testMultiLevelInsertForward() throws IOException {
			final int count = 100;

			for (int i = 0; i < count; i++) {

				assertThat(tree.isValid()).isTrue();
				tree.add(i, i);
				tree.checkStructure();
				final Iterator<Integer> iterator = tree.getIterator();

				int latest = iterator.next();
				for (int j = 0; j <= i - 1; j++) {
					final int next = iterator.next();
					assertThat(latest <= next).isTrue();
					latest = next;
				}

				assertThat(iterator.hasNext()).isFalse();
			}


			assertThat(tree.getNumberOfEntries()).isEqualTo(count);
			final Iterator<Integer> iterator = tree.getIterator();

			int latest = iterator.next();
			for (int i = 0; i < count - 1; i++) {
				final int next = iterator.next();
				assertThat(latest <= next).isTrue();
				latest = next;
			}
			assertThat(iterator.hasNext()).isFalse();
		}

		@Test
		public void testMultiLevelInsertBackward() throws IOException {
			final int count = 100;

			for (int i = 0; i < count; i++) {
				assertThat(tree.isValid()).isTrue();
				tree.add(count - i, count - i);
				tree.checkStructure();
				final Iterator<Integer> iterator = tree.getIterator();

				int latest = iterator.next();
				for (int j = 0; j <= i - 1; j++) {
					final int next = iterator.next();
					assertThat(latest <= next).isTrue();
					latest = next;
				}

				assertThat(iterator.hasNext()).isFalse();
			}


			assertThat(tree.getNumberOfEntries()).isEqualTo(count);
			final Iterator<Integer> iterator = tree.getIterator();

			int latest = iterator.next();
			for (int i = 0; i < count - 1; i++) {
				final int next = iterator.next();
				assertThat(latest <= next).isTrue();
				latest = next;
			}
			assertThat(iterator.hasNext()).isFalse();
		}

		@Test
		public void testLargeKeyValues() throws IOException, InterruptedException {
			// initialize new btree
			file.delete();
			final ResourceManager newRm = new ResourceManagerBuilder().file(file).build();
			final BTree<String, String> newTree = BTree.create(newRm, FixedStringSerializer.INSTANCE_1000,
					FixedStringSerializer.INSTANCE_1000,
					StringComparator.INSTANCE);

			assertThat(newTree.getKeySerializer().getSerializedLength()).isEqualTo(1000);
			assertThat(newTree.getValueSerializer().getSerializedLength()).isEqualTo(1000);

			newTree.initialize();

			// do the actual test
			final int count = 100;
			for (int i = 0; i < count; i++) {
				if (i == 24)
					LOG.debug("DEBUG");

				// LOG.info("i=" + i);
				newTree.add("" + i, "" + i);
				newTree.checkStructure();
			}

			final Iterator<String> iterator = newTree.getIterator();
			for (int i = 0; i < count; i++) {
				assertThat(iterator.hasNext()).isTrue();
				final String next = iterator.next();
				assertThat(next).isNotNull();
				// LOG.debug("got value: " + next);
			}
			assertThat(iterator.hasNext()).isFalse();
		}

		@Test
		public void manualConstructor() throws IOException {
			final String filePath = "/tmp/manualConstructorTestBtree";
			final File file = new File(filePath);
			file.delete();

			final ResourceManager pm = new ResourceManagerBuilder().file(file).build();
			pm.open();

			final BTree<Integer, String> btree =
					BTree.create(pm, IntegerSerializer.INSTANCE, FixedStringSerializer.INSTANCE,
							IntegerComparator.INSTANCE);

			btree.initialize();

			assertThat(file.exists()).isTrue();
		}

		@Test
		public void staticMethodConstructor() throws IOException {
			final BTree<Integer, String> btree =
					BTree.create(createResourceManager(true), IntegerSerializer.INSTANCE,
							FixedStringSerializer.INSTANCE,
							IntegerComparator.INSTANCE);
			btree.initialize();
		}

		@Test
		public void integerStringTree() throws IOException {
			final ResourceManager rm = createResourceManager(true);
			BTree<Integer, String> btree =
					BTree.create(rm, IntegerSerializer.INSTANCE, FixedStringSerializer.INSTANCE_1000,
							IntegerComparator.INSTANCE);
			btree.initialize();

			final int count = 50;

			for (int i = 0; i < count; i++) {
				LOG.debug("ROUND: " + i);

				btree.add(i, "" + i);

				final BTree<Integer, String> btree2 =
						BTree.create(rm, IntegerSerializer.INSTANCE, FixedStringSerializer.INSTANCE_1000,
								IntegerComparator.INSTANCE);
				btree2.load();


				final Iterator<String> iterator = btree2.getIterator();

				for (int j = 0; j <= i; j++) {
					assertThat(iterator.hasNext()).isTrue();
					LOG.debug("next value:" + iterator.next());
				}

				assertThat(iterator.hasNext()).isFalse();


			}

			btree = BTree.create(rm, IntegerSerializer.INSTANCE,
					FixedStringSerializer.INSTANCE_1000,
					IntegerComparator.INSTANCE);
			btree.load();

			final Iterator<String> iterator = btree.getIterator();

			for (int i = 0; i < count; i++) {
				assertThat(iterator.hasNext()).isTrue();
				LOG.debug("next value:" + iterator.next());
			}

			assertThat(iterator.hasNext()).isFalse();

		}

		@Test(expected = IllegalStateException.class)
		public void exceptionIfValidTreeIsLoaded() throws IOException {
			tree.load();
		}


		@Test(expected = IllegalStateException.class)
		public void exceptionIfValidTreeIsInitialized() throws IOException {
			tree.initialize();
		}

		@Test(expected = IllegalStateException.class)
		public void exceptionIfValidTreeIsBulkInitialized() throws IOException {
			final AbstractMap.SimpleEntry[] content = {new AbstractMap.SimpleEntry(1, 2)};
			tree.bulkInitialize(content, true);
		}

		@Test
		public void ranges() throws IOException, InterruptedException {
			fillTreeWithStructureCheck(tree, 100);
			final List<Range<Integer>> rangeList = new ArrayList<Range<Integer>>();
			rangeList.add(new Range(-5, 5));

			// 49 - 56
			rangeList.add(new Range(50, 55));
			rangeList.add(new Range(52, 53));
			rangeList.add(new Range(49, 53));
			rangeList.add(new Range(52, 56));

			rangeList.add(new Range(95, 1000));

			Iterator<Integer> iterator = tree.getIterator(rangeList);

			for (int i = 0; i <= 5; i++) {
				assertThat((int) iterator.next()).isEqualTo(i);
			}

			for (int i = 49; i <= 56; i++)
				assertThat((int) iterator.next()).isEqualTo(i);

			for (int i = 95; i < 100; i++) {
				assertThat((int) iterator.next()).isEqualTo(i);
			}
			assertThat(iterator.hasNext()).isFalse();


			rangeList.clear();
			iterator = tree.getIterator(rangeList);
			for (int i = 0; i < 100; i++) {
				assertThat(iterator.next()).isNotNull();
			}
			assertThat(iterator.next()).isNull();

			rangeList.add(new Range(null, 50));
			iterator = tree.getIterator(rangeList);
			for (int i = 0; i <= 50; i++) {
				assertThat(iterator.next()).isNotNull();
			}
			assertThat(iterator.next()).isNull();

			rangeList.add(new Range<Integer>(0, 75));
			iterator = tree.getIterator(rangeList);
			for (int i = 0; i <= 75; i++) {
				assertThat(iterator.next()).isNotNull();
			}
			assertThat(iterator.next()).isNull();

			rangeList.clear();
			rangeList.add(new Range(50, null));
			iterator = tree.getIterator(rangeList);
			for (int i = 50; i <= 99; i++) {
				assertThat(iterator.next()).isNotNull();
			}
			assertThat(iterator.next()).isNull();

			rangeList.add(new Range(25, 99));
			iterator = tree.getIterator(rangeList);
			for (int i = 25; i <= 99; i++) {
				assertThat(iterator.next()).isNotNull();
			}
			assertThat(iterator.next()).isNull();


			rangeList.add(new Range(null, null));
			rangeList.add(new Range(null, 23));
			iterator = tree.getIterator(rangeList);
			for (int i = 0; i <= 99; i++) {
				assertThat(iterator.next()).isNotNull();
			}
			assertThat(iterator.next()).isNull();


			// with null as search range
			try {
				iterator = tree.getIterator(null);
				fail("tree.getIterator(null) should throw an exception");
			} catch (NullPointerException ignored) {
			}

			// with an empty list as search range
			rangeList.clear();
			iterator = tree.getIterator(rangeList);
			for (int i = 0; i <= 99; i++) {
				assertThat(iterator.next()).isNotNull();
			}
			assertThat(iterator.next()).isNull();
		}

		@Test
		public void shouldBeEmptyAfterCreation() {
			assertThat(tree.getNumberOfEntries()).isEqualTo(0);
		}

		@Test
		public void getIteratorShouldEnsureValid() throws IOException {
			tree.close();
			try {
				tree.getIterator();
				fail("getIterator should ensure valid");
			} catch (IllegalStateException e) {
			}

			try {
				tree.getIterator(Integer.MIN_VALUE, Integer.MAX_VALUE);
				fail("getIterator should ensure valid");
			} catch (IllegalStateException e) {
			}
		}

		@Test
		public void shouldContainAddedEntries() {
			tree.add(key1, value1);
			assertThat(tree.containsKey(key1)).isTrue();
			assertThat(tree.get(key1).size()).isEqualTo(1);
			assertThat(tree.get(key1).get(0)).isEqualTo(value1);
			assertThat(tree.getNumberOfEntries()).isEqualTo(1);

			tree.add(key1, value2);
			assertThat(tree.containsKey(key1)).isTrue();
			assertThat(tree.get(key1).size()).isEqualTo(2);
			assertThat(tree.get(key1).contains(value1)).isTrue();
			assertThat(tree.get(key1).contains(value2)).isTrue();
			assertThat(tree.getNumberOfEntries()).isEqualTo(2);

			tree.add(key2, value2);
			assertThat(tree.containsKey(key2)).isTrue();
			assertThat(tree.get(key2).size()).isEqualTo(1);
			assertThat(tree.get(key1).contains(value2)).isTrue();
			assertThat(tree.get(key1).contains(value1)).isTrue();
			assertThat(tree.get(key1).size() == 2).isTrue();
			assertThat(tree.getNumberOfEntries()).isEqualTo(3);
		}

		@Test
		public void shouldReturnEmptyArrayIfKeyNotFound() {
			assertThat(tree.get(key1).size()).isEqualTo(0);
		}

		@Test
		public void shouldBeAbleToRemoveInsertedEntries() {
			tree.add(key1, value1);
			assertThat(tree.containsKey(key1)).isTrue();
			tree.remove(key1);
			assertThat(tree.containsKey(key1)).isFalse();
			assertThat(tree.getNumberOfEntries()).isEqualTo(0);
		}

		@Test
		public void clearShouldRemoveAllElements() throws IOException {
			tree.add(key1, value1);
			tree.add(key2, value2);
			assertThat(tree.getNumberOfEntries()).isEqualTo(2);
			tree.clear();
			assertThat(tree.getNumberOfEntries()).isEqualTo(0);
		}

		@Test
		public void removeWithOnlyKeyArgumentShouldRemoveAllValues() {
			tree.add(key1, value1);
			tree.add(key1, value2);
			tree.add(key2, value2);

			assertThat(tree.getNumberOfEntries()).isEqualTo(3);
			tree.remove(key1);
			assertThat(tree.getNumberOfEntries()).isEqualTo(1);
			assertThat(tree.get(key1).size()).isEqualTo(0);
		}

		@Test
		public void iterator() {
			Integer val;

			final int k1 = Integer.MIN_VALUE;
			final int k2 = Integer.MAX_VALUE;

			tree.add(k1, value1);
			tree.add(k1, value2);
			tree.add(k2, value2);

			final Iterator<Integer> i = tree.getIterator();
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

		@Test
		public void iteratorsWithStartEndGiven() throws IOException, InterruptedException {
			fillTreeWithStructureCheck(tree, 100);
			Iterator<Integer> iterator = tree.getIterator();
			for (int i = 0; i < 100; i++)
				assertThat((int) iterator.next()).isEqualTo(i);
			assertThat(iterator.hasNext()).isFalse();

			iterator = tree.getIterator(-50, 50);
			for (int i = 0; i <= 50; i++)
				assertThat((int) iterator.next()).isEqualTo(i);
			assertThat(iterator.hasNext()).isFalse();

			iterator = tree.getIterator(50, 150);
			for (int i = 50; i < 100; i++)
				assertThat((int) iterator.next()).isEqualTo(i);
			assertThat(iterator.hasNext()).isFalse();
		}

		@Test
		public void close() throws IOException, InterruptedException {
			fillTreeWithStructureCheck(tree, 100);
			tree.close();
			assertThat(tree.isValid()).isFalse();
			assertThat(tree.getResourceManager().isOpen()).isFalse();
		}

		@Test
		public void bulkInsert1Layer() throws IOException {
			tree.close();
			file.delete();
			bulkInsert(tree.getMaxLeafKeys()); // exactly one leaf
		}

		@Test
		public void shouldWorkOnTheEdgeToCreateAnInnerNode() throws InterruptedException {
			final int size = tree.getMaxLeafKeys();
			fillTreeWithStructureCheck(tree, size);
			tree.checkStructure();
			assertThat(tree.getNumberOfEntries()).isEqualTo(size);
			simpleTests(tree, size + 1);
		}

		@Test
		public void bulkInsert2Layers() throws IOException {
			bulkInsert((tree.getMaxInnerKeys() + 1) * tree.getMaxLeafKeys());

			// make sure the test is checking 2 layers
			assertThat(tree.getDepth()).isEqualTo(2);
		}

		/**
		 * causes java.lang.IllegalArgumentException: for bulkinsert, you must have at least 2 page ids and keys.size() ==
		 * (pageIds.size() - 1)
		 *
		 * @throws java.io.IOException
		 */
		@Test
		public void bulkInsertWithOnlyOnePageForNextInnerNode() throws IOException {
			bulkInsert((tree.getMaxInnerKeys() + 1) * tree.getMaxLeafKeys() + 1);
		}

		@Test
		public void bulkInsert3Layers() throws IOException {
			bulkInsert((tree.getMaxInnerKeys() + 1) * (tree.getMaxInnerKeys() + 1) * tree.getMaxLeafKeys());

			// just to make sure that the test is really checking 3 layers
			assertThat(tree.getDepth() >= 3).isTrue();
		}


		@Test
		public void bulkInsertWithSortAndCloseAndRange() throws IOException {
			final int count = 50;
			final int from = 10;
			final int to = 39;
			final int realCount = to - from + 1;

			final AbstractMap.SimpleEntry<Integer, Integer>[] kvs = new AbstractMap.SimpleEntry[count];
			final SecureRandom srand = new SecureRandom();

			final List<Integer> keys = new LinkedList<Integer>();

			for (int i = from; i <= to; i++) {
				int newKey = srand.nextInt();

				while (keys.contains(newKey)) {
					newKey = srand.nextInt();
				}

				keys.add(newKey);
				kvs[i] = new AbstractMap.SimpleEntry<Integer, Integer>(newKey, newKey);
			}

			tree.close();
			file.delete();

			tree.bulkInitialize(kvs, from, to, false);

			assertThat(tree.getNumberOfEntries()).isEqualTo(to - from + 1);
			tree.checkStructure();

			Collections.sort(keys, IntegerComparator.INSTANCE);
			for (int i = 0; i < realCount; i++) {
				assertThat(tree.get(keys.get(i)).size()).as("size problem with key " + i).isEqualTo(1);
				assertThat(tree.get(keys.get(i)).get(0)).isEqualTo(keys.get(i));
			}

			tree.close();
			tree.load();

			// test everything again
			assertThat(tree.getNumberOfEntries()).isEqualTo(realCount);
			tree.checkStructure();

			for (int i = 0; i < realCount; i++) {
				assertThat(tree.get(keys.get(i)).size()).as("size problem with key " + i).isEqualTo(1);
				assertThat(tree.get(keys.get(i)).get(0)).isEqualTo(keys.get(i));
			}
		}

		@Test(expected = NullPointerException.class)
		public void bulkInsertWithNullValues() throws IOException {
			int count = 1;
			final AbstractMap.SimpleEntry<Integer, Integer>[] kvs = new AbstractMap.SimpleEntry[2];

			for (int i = 0; i < count; i++) {
				kvs[i] = new AbstractMap.SimpleEntry<Integer, Integer>(i, i);
			}

			tree.close();
			file.delete();
			tree.bulkInitialize(kvs, true);
		}


		public void shouldBeAbleToOpenAndLoad(final Integer key1, final Integer key2) throws IOException {
			final Integer value1 = 1;
			final Integer value2 = 2;

			tree.close();
			file.delete();


			tree.initialize();
			tree.add(key1, value1);
			tree.add(key2, value2);


			assertThat(tree.getNumberOfEntries()).isEqualTo(2);
			assertThat(tree.get(key1).get(0)).isEqualTo(value1);
			assertThat(tree.get(key2).get(0)).isEqualTo(value2);

			tree.close();

			tree = BTree.create(rm, IntegerSerializer.INSTANCE, IntegerSerializer.INSTANCE, IntegerComparator.INSTANCE);
			tree.load();
			assertThat(tree.getNumberOfEntries()).isEqualTo(2);
			assertThat(tree.get(key1).get(0)).isEqualTo(value1);
			assertThat(tree.get(key2).get(0)).isEqualTo(value2);
		}

		@Test
		public void shouldBeAbleToOpenAndLoad() throws IOException {
			shouldBeAbleToOpenAndLoad(20, 10);
			shouldBeAbleToOpenAndLoad(10, 20);
		}

		@Test
		public void toStringShouldAlwaysWork() throws IOException {
			assertThat(tree.toString()).isNotNull();
			tree.close();
			assertThat(tree.toString()).isNotNull();
		}

		private void removeWithValueArgumentShouldRemoveOnlyThisValue(final Integer key1, final Integer key2) {
			tree.add(key1, value1);
			tree.add(key1, value2);
			tree.add(key2, value2);

			assertThat(tree.getNumberOfEntries()).isEqualTo(3);
			assertThat(tree.get(key1).size()).isEqualTo(2);
			assertThat(tree.get(key2).size()).isEqualTo(1);

			tree.remove(key1, value2);
			assertThat(tree.getNumberOfEntries()).isEqualTo(2);
			assertThat(tree.get(key1).size()).isEqualTo(1);
			assertThat(tree.get(key1).get(0)).isEqualTo(value1);
			assertThat(tree.get(key2).get(0)).isEqualTo(value2);
		}

		private ResourceManager createResourceManager(final boolean reset) {
			if (reset)
				file.delete();
			return new ResourceManagerBuilder().file(file).build();
		}

		private void bulkInsert(final int count) throws IOException {
			final AbstractMap.SimpleEntry<Integer, Integer>[] kvs = new AbstractMap.SimpleEntry[count];

			for (int i = 0; i < count; i++) {
				kvs[i] = new AbstractMap.SimpleEntry<Integer, Integer>(i, i);
			}

			tree.close();
			file.delete();
			tree.bulkInitialize(kvs, true);

			// check if its correct
			LOG.debug("checking bulkinsert results...");
			assertThat(tree.getNumberOfEntries()).isEqualTo(count);

			tree.checkStructure();

			for (int i = 0; i < count; i++) {
				assertThat(tree.get(kvs[i].getKey()).size() > 0).isTrue();
				assertThat(tree.get(kvs[i].getKey()).size()).isEqualTo(1);
				assertThat(tree.get(kvs[i].getKey()).get(0)).isEqualTo(kvs[i].getValue());
			}
		}
	}

	public static class Slow {

		@Before
		public void setUp() throws IOException {
			tree = createNewTree(PAGE_SIZE);
		}

		@Ignore
		@Test
		public void iteratorsWithoutParameters() throws IOException, InterruptedException {
			fillTreeWithStructureCheck(tree, 1000);
			tree.checkStructure();

			final Iterator<Integer> iterator = tree.getIterator();
			for (int i = 0; i < 1000; i++) {
				assertThat(iterator.hasNext()).isTrue();
				assertThat(iterator.next()).isEqualTo(i);
			}
			assertThat(iterator.hasNext()).isFalse();
		}


		@Ignore
		@Test
		public void shouldWorkWithMassiveValues() throws InterruptedException {
			final int size = 100000;

			fillTree(tree, size);

			assertThat(tree.getNumberOfEntries()).isEqualTo(size);
			
			simpleTests(tree, Integer.MAX_VALUE);
			simpleTests(tree, Integer.MIN_VALUE);
		}
	}

	public static class Performance {

		@Before
		public void setUp() throws IOException {
			tree = createNewTree(4096);
		}

		@Ignore
		@Test
		public void randomReads() throws InterruptedException, IOException {
			int entries = 10 * 1000;
			int numOfReads = 1000 * 1000;

			assertThat(tree.getResourceManager().getPageSize()).isEqualTo(4096);
			long diff = testReads(entries, numOfReads);

			LOG.info("reading " + numOfReads + " random entries from a tree with " +
					entries + " entries took " + diff + " milliseconds");
		}

		@Ignore
		@Test
		private void randomReadsWithCache() throws IOException, InterruptedException {
			randomReadsWithCache(100);
			randomReadsWithCache(1000);
			
		}

		private void randomReadsWithCache(int cacheSize) throws InterruptedException, IOException {
			int entries = 10 * 1000;
			int numOfReads = 1000 * 1000;

			file.delete();

			rm = new ResourceManagerBuilder().useLock(true).pageSize(4096).file(file).cacheSize(cacheSize).
					cacheSize(cacheSize).open().build();
			tree = createNewTree(rm);

			long time = testReads(entries, numOfReads);

			LOG.info("reading " + numOfReads + " random entries from a tree (cacheSize=" + cacheSize + ") with " +
					entries + " entries took " + time + " milliseconds. fileSize: " +
					(((double) (file.length())) / 1024) + "kb");
		}

		@Ignore
		@Test
		public void randomReadsWithReferenceCache() throws IOException, InterruptedException {
			int entries = 10 * 1000;
			int numOfReads = 1000 * 1000;

			file.delete();
			rm = new ResourceManagerBuilder().useLock(true).pageSize(4096).file(file).cacheSize(0).
					cacheSize(0).open().build();
			tree = createNewTree(rm);

			long time = testReads(entries, numOfReads);

			LOG.info("reading " + numOfReads + " random entries from a tree (with weakReferenceCache) with " +
					entries + " entries took " + time + " milliseconds");
		}

		@Ignore
		@Test
		public void continuousInserts() throws IOException, InterruptedException {
			continuousInserts(4 * 1024);
			continuousInserts(64 * 1024);
		}

		@Ignore
		@Test
		public void bulkInsert() throws IOException {
			bulkInsert(4 * 1024);
			bulkInsert(64 * 1024);
		}

		private long testReads(int entries, int reads) throws InterruptedException {
			assertThat(tree.getResourceManager().getPageSize()).isEqualTo(4096);
			fillTree(tree, entries);
			Random rand = new Random();

			long total = 0;
			for (int i = 0; i < reads; i++) {
				int next = rand.nextInt(entries);
				
				long start = System.currentTimeMillis();
				tree.get(next);
				total += System.currentTimeMillis() - start;
			}
			return total;
		}

		private void continuousInserts(int pageSize) throws InterruptedException, IOException {
			int count = 1000 * 1000;

			file.delete();
			rm = new ResourceManagerBuilder().useLock(true).pageSize(pageSize).file(file).cacheSize(0).open().build();
			tree = createNewTree(rm);

			assertThat(tree.getResourceManager().getPageSize()).isEqualTo(pageSize);

			long start = System.currentTimeMillis();
			fillTree(tree, count);
			long diff = System.currentTimeMillis() - start;

			tree.close();
			tree.load();

			assertThat(tree.getNumberOfEntries()).isEqualTo(count);
			for (int i = 0; i < count; i++)
				assertThat(tree.get(i)).hasSize(1).contains(i);
			tree.checkStructure();

			log(count, diff, pageSize);
		}

		private void bulkInsert(int pageSize) throws IOException {
			int count = 1000 * 1000;

			file.delete();
			rm = new ResourceManagerBuilder().useLock(true).pageSize(pageSize).file(file).cacheSize(0).open().build();
			tree = BTree.create(rm, IntegerSerializer.INSTANCE, IntegerSerializer.INSTANCE,
					IntegerComparator.INSTANCE);

			assertThat(tree.getResourceManager().getPageSize()).isEqualTo(pageSize);

			AbstractMap.SimpleEntry[] buffer = new AbstractMap.SimpleEntry[count];
			for (int i = 0; i < count; i++) {
				buffer[i] = new AbstractMap.SimpleEntry(new Integer(i), new Integer(i));
			}

			long start = System.currentTimeMillis();
			tree.bulkInitialize(buffer, true);
			long diff = System.currentTimeMillis() - start;

			tree.close();
			tree.load();

			assertThat(tree.getNumberOfEntries()).isEqualTo(count);
			for (int i = 0; i < count; i++)
				assertThat(tree.get(i)).hasSize(1).contains(i);

			tree.checkStructure();

			log(count, diff, pageSize);
		}

		private void log(int count, long diff, int pageSize){
			// time information
			LOG.info("inserting " + count + " Integers with pageSize " + pageSize + " took " + diff + " milliseconds");

			// file size information
			LOG.info("the tree file has a size of " + (file.length() / 1024) + "kb");
			LOG.info("filled with " + count + " Integers (4byte) gives a total usage of " + (count * 4 / 1024) + "kb");
			LOG.info("that is an overhead of " + (100d * (file.length() / (count * 4)) - 100) + "%");
		}


	}
}
