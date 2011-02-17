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
import com.google.inject.Singleton;

import java.util.Comparator;

@Singleton
public class LeafPageManager<K,V> extends AbstractPageManager<LeafNode<K,V>> {

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
		super(bpm);
		this.ppSerializer = ppSerializer;
        this.keyPageManager = keyPageManager;
        this.valuePageManager = valuePageManager;
        this.comparator = comparator;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.AbstractPageManager#createObjectPage()
	 */
	@Override
	protected LeafNode<K, V> createObjectPage(RawPage page) {
		return new LeafNode<K, V>(page, keyPageManager, valuePageManager, ppSerializer, comparator, this);
	}

}
