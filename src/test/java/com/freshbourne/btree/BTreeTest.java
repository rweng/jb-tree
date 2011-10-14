/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */
package com.freshbourne.btree;

import com.freshbourne.multimap.MultiMapSpec;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;

import static org.junit.Assert.assertEquals;

public class BTreeTest extends MultiMapSpec<Integer, Integer> {

	private static       String        path     = "/tmp/btree_spec";
	private static       BTreeProvider provider = new BTreeProvider(path);
	private static final Log           LOG      = LogFactory.getLog(BTreeTest.class);

	public BTreeTest() {
		super(provider);
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

		BTree<Integer, Integer> tree = (BTree<Integer, Integer>) getMultiMap();

		tree.initialize();
		tree.add(key1, value1);
		tree.add(key2, value2);
		tree.sync();

		tree = provider.getInstance();
		tree.load();
		assertEquals(2, tree.getNumberOfEntries());
		assertEquals(value1, tree.get(key1).get(0));
		assertEquals(value2, tree.get(key2).get(0));
	}

	@Test
	public void shouldWorkWithMassiveValues() {
		int size = 100000;

		fill(size);

		assertEquals(size, getMultiMap().getNumberOfEntries());
		key1 = getProvider().createMaxKey();
		simpleTests();
		key1 = getProvider().createMinKey();
		simpleTests();
	}

	@Test @Ignore("does not work yet")
	public void shouldNotHaveTooMuchOverhead() {
		int key = getProvider().createRandomKey();
		int val = getProvider().createRandomValue();

		int sizeForKey = Integer.SIZE / 8;
		int sizeForVal = Integer.SIZE / 8;

		// insert 10.000 K/V pairs
		int size = 100000;
		long start = System.currentTimeMillis();
		for (int i = 0; i < size; i++) {
			getMultiMap().add(key, val);
		}

		getTree().sync();
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

	private BTree<Integer, Integer> getTree() {
		return (BTree<Integer, Integer>) getMultiMap();
	}

	@Test
	public void bulkInsert() throws IOException {
		int testSize = 1000 * 1000;

		@SuppressWarnings("unchecked")
		AbstractMap.SimpleEntry<Integer, Integer>[] kvs = new AbstractMap.SimpleEntry[testSize];

		for (int i = 0; i < testSize; i++) {
			kvs[i] = new AbstractMap.SimpleEntry<Integer, Integer>(i, i + 10000);
		}

		getTree().bulkInitialize(kvs, true);

		// check if its correct
		LOG.debug("checking bulkinsert results...");
		assertEquals(testSize, getTree().getNumberOfEntries());
		for (int i = 0; i < testSize; i++) {

			if (getTree().get(kvs[i].getKey()).size() == 0) {
				LOG.error("tree doesn't have key " + i);
			}
			assertEquals("size problem with key " + i, 1, getTree().get(kvs[i].getKey()).size());
			assertEquals(kvs[i].getValue(), getTree().get(kvs[i].getKey()).get(0));
		}

		LOG.info("Checking tree structure. This could take a while");
		getTree().checkStructure();
	}

}
