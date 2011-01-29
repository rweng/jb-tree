/*
 * Copyright (c) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 */

package com.freshbourne.multimap.btree;

import com.freshbourne.io.*;
import com.freshbourne.serializer.FixLengthSerializer;
import com.google.inject.Inject;

import java.util.Comparator;

public class LeafPageManager<K,V> implements PageManager<LeafNode<K,V>> {

	private final PageManager<RawPage> bpm;
	private final FixLengthSerializer<PagePointer, byte[]> ppSerializer;
	
	private final DataPageManager<K> keyPageManager;
	private final DataPageManager<V> valuePageManager;
	
	private final Comparator<K> comparator;
	
	@Inject
	public LeafPageManager(
			PageManager<RawPage> bpm, 
			DataPageManager<K> keyPageManager,
			DataPageManager<V> valuePageManager,
			FixLengthSerializer<PagePointer, byte[]> ppSerializer,
			Comparator<K> comparator) {
		this.bpm = bpm;
		this.ppSerializer = ppSerializer;
        this.keyPageManager = keyPageManager;
        this.valuePageManager = valuePageManager;
        this.comparator = comparator;
	}
	
	/* (non-Javadoc)
	 * @see com.freshbourne.io.PageManager#createPage()
	 */
	@Override
	public LeafNode<K, V> createPage() {
		LeafNode<K, V> l = new LeafNode<K, V>(bpm.createPage(), keyPageManager, valuePageManager, ppSerializer, comparator, this);
		l.initialize();
		return l;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.PageManager#getPage(int)
	 */
	@Override
	public LeafNode<K, V> getPage(long id) {
		LeafNode<K, V> l = new LeafNode<K, V>(bpm.getPage(id), keyPageManager, valuePageManager, ppSerializer, comparator, this);
		l.load();
		return l;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.PageManager#removePage(int)
	 */
	@Override
	public void removePage(long id) {
		bpm.removePage(id);
	}

}
