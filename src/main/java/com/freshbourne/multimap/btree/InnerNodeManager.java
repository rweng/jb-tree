/**
 * Copyright (C) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.multimap.btree;

import java.io.IOException;
import java.util.Comparator;

import com.freshbourne.io.DataPageManager;
import com.freshbourne.io.PageManager;
import com.freshbourne.io.PagePointer;
import com.freshbourne.io.RawPage;
import com.freshbourne.serializer.FixLengthSerializer;
import com.google.inject.Inject;

public class InnerNodeManager<K, V> implements PageManager<InnerNode<K, V>> {

	private final PageManager<RawPage> bpm;
	private final FixLengthSerializer<PagePointer, byte[]> ppSerializer;
	
	private final DataPageManager<K> keyPageManager;
	
	private final Comparator<K> comparator;
	private final PageManager<LeafNode<K, V>> leafPageManager;
	
	@Inject
	public InnerNodeManager(
			PageManager<RawPage> bpm, 
			DataPageManager<K> keyPageManager,
			DataPageManager<V> valuePageManager,
			LeafPageManager<K, V> leafPageManager,
			FixLengthSerializer<PagePointer, byte[]> ppSerializer,
			Comparator<K> comparator) {
		this.bpm = bpm;
		this.ppSerializer = ppSerializer;
        this.keyPageManager = keyPageManager;
        this.leafPageManager = leafPageManager;
        this.comparator = comparator;
	}
	
	/* (non-Javadoc)
	 * @see com.freshbourne.io.PageManager#createPage()
	 */
	@Override
	public InnerNode<K, V> createPage() {
		InnerNode<K, V> l = new InnerNode<K, V>(bpm.createPage(), ppSerializer, comparator, keyPageManager, leafPageManager, this);
		l.initialize();
		return l;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.PageManager#getPage(int)
	 */
	@Override
	public InnerNode<K, V> getPage(long id) {
		InnerNode<K, V> l = new InnerNode<K, V>(bpm.getPage(id), ppSerializer, comparator, keyPageManager, leafPageManager, this);
		try {
			l.load();
		} catch (IOException e) {
			return null;
		}
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
