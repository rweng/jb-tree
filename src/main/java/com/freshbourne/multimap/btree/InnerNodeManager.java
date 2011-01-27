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

import com.freshbourne.io.BufferPoolManager;
import com.freshbourne.io.DataPageManager;
import com.freshbourne.io.PageManager;
import com.freshbourne.io.PagePointer;
import com.freshbourne.io.RawPage;
import com.freshbourne.serializer.FixLengthSerializer;
import com.google.inject.Inject;

public class InnerNodeManager<K, V> implements PageManager<BTreeInnerNode<K, V>> {

	private final BufferPoolManager bpm;
	private final FixLengthSerializer<PagePointer, byte[]> ppSerializer;
	
	private final DataPageManager<K> keyPageManager;
	private final DataPageManager<V> valuePageManager;
	
	private final Comparator<K> comparator;
	
	@Inject
	public InnerNodeManager(
			BufferPoolManager bpm, 
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
	public BTreeInnerNode<K, V> createPage() {
		RawPage p = bpm.createPage();
		BTreeInnerNode<K, V> l = new BTreeInnerNode<K, V>(bpm.createPage(), ppSerializer, comparator);
		try {
			l.initialize();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return l;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.PageManager#getPage(int)
	 */
	@Override
	public BTreeInnerNode<K, V> getPage(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.PageManager#removePage(int)
	 */
	@Override
	public void removePage(long id) {
		// TODO Auto-generated method stub
		
	}

}
