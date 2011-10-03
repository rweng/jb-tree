/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */
package com.freshbourne.btree;

import java.util.Comparator;

import com.freshbourne.io.AbstractPageManager;
import com.freshbourne.io.DataPageManager;
import com.freshbourne.io.PageManager;
import com.freshbourne.io.RawPage;
import com.freshbourne.serializer.FixLengthSerializer;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class InnerNodeManager<K, V> extends AbstractPageManager<InnerNode<K, V>> {

	private final FixLengthSerializer<K, byte[]> keySerializer;
	
	private final DataPageManager<K> keyPageManager;
	
	private final Comparator<K> comparator;
	private final PageManager<LeafNode<K, V>> leafPageManager;
	
	@Inject
	public InnerNodeManager(
			PageManager<RawPage> bpm, 
			DataPageManager<K> keyPageManager,
			DataPageManager<V> valuePageManager,
			LeafPageManager<K, V> leafPageManager,
			FixLengthSerializer<K, byte[]> keySerializer,
			Comparator<K> comparator) {
		super(bpm);
		this.keySerializer = keySerializer;
        this.keyPageManager = keyPageManager;
        this.leafPageManager = leafPageManager;
        this.comparator = comparator;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.AbstractPageManager#createObjectPage()
	 */
	@Override
	protected InnerNode<K, V> createObjectPage(RawPage page) {
		return new InnerNode<K, V>(page, keySerializer, comparator, keyPageManager, leafPageManager, this);
	}
}
