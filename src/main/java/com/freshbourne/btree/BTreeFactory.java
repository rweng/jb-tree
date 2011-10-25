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

import com.freshbourne.io.DataPageManager;
import com.freshbourne.io.FileResourceManager;
import com.freshbourne.io.FileResourceManagerFactory;
import com.freshbourne.io.PagePointer;
import com.freshbourne.serializer.FixLengthSerializer;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;


@Singleton
public class BTreeFactory {

	private FileResourceManagerFactory frmFactory;
	private HashMap<File, BTree> map = new HashMap<File, BTree>();
	private FixLengthSerializer<PagePointer, byte[]> pagePointSerializer;

	@Inject BTreeFactory(FileResourceManagerFactory frmFactory, FixLengthSerializer<PagePointer, byte[]> pps) {
		this.frmFactory = frmFactory;
		this.pagePointSerializer = pps;
	}


	/**
	 * returns a loaded or initialized BTree
	 *
	 * @param file
	 * @param ks
	 * @param vs
	 * @param comparator
	 * @param <K>
	 * @param <V>
	 * @return
	 *
	 * @throws IOException
	 */
	public <K, V> BTree<K, V> get(File file, FixLengthSerializer<K, byte[]> ks, FixLengthSerializer<V, byte[]> vs,
	                              Comparator<K> comparator, boolean lockFile) throws IOException {
		if (map.containsKey(file))
			return map.get(file);

		FileResourceManager frm = frmFactory.get(file, lockFile);

		BTree<K, V> tree = new BTree<K, V>(frm, ks, vs, comparator);

		map.put(file, tree);

		return tree;
	}

	public <K, V> BTree<K, V> get(File file, FixLengthSerializer<K, byte[]> ks, FixLengthSerializer<V, byte[]> vs,
	                              Comparator<K> comparator) throws IOException {
		return get(file, ks, vs, comparator, true);
	}
}
