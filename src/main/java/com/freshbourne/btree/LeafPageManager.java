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

import com.freshbourne.io.AbstractPageManager;
import com.freshbourne.io.PageManager;
import com.freshbourne.io.RawPage;
import com.freshbourne.serializer.FixLengthSerializer;

import java.util.Comparator;

class LeafPageManager<K,V> extends AbstractPageManager<LeafNode<K,V>> {

	private final FixLengthSerializer<V, byte[]> valueSerializer;
	private final FixLengthSerializer<K, byte[]> keySerializer;
	
	private final Comparator<K> comparator;
	
	public LeafPageManager(
			final PageManager<RawPage> bpm,
			final FixLengthSerializer<V, byte[]> valueSerializer,
			final FixLengthSerializer<K, byte[]> keySerializer,
			final Comparator<K> comparator) {
		super(bpm);
		this.valueSerializer = valueSerializer;
		this.keySerializer = keySerializer;
        this.comparator = comparator;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.AbstractPageManager#createObjectPage()
	 */
	@Override
	protected LeafNode<K, V> createObjectPage(final RawPage page) {
		return new LeafNode<K, V>(page, keySerializer, valueSerializer, comparator, this, 1);
	}

}
