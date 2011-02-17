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

import com.freshbourne.io.AbstractPageManager;
import com.freshbourne.io.DataPageManager;
import com.freshbourne.io.PageManager;
import com.freshbourne.io.PagePointer;
import com.freshbourne.io.RawPage;
import com.freshbourne.serializer.FixLengthSerializer;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class InnerNodeManager<K, V> extends AbstractPageManager<InnerNode<K, V>> {

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
		super(bpm);
		this.ppSerializer = ppSerializer;
        this.keyPageManager = keyPageManager;
        this.leafPageManager = leafPageManager;
        this.comparator = comparator;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.AbstractPageManager#createObjectPage()
	 */
	@Override
	protected InnerNode<K, V> createObjectPage(RawPage page) {
		return new InnerNode<K, V>(page, ppSerializer, comparator, keyPageManager, leafPageManager, this);
	}
}
