/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */

package com.freshbourne.btree;

import com.freshbourne.io.*;
import com.freshbourne.serializer.FixLengthSerializer;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.Comparator;

@Singleton
public class LeafPageManager<K,V> extends AbstractPageManager<LeafNode<K,V>> {

	private final FixLengthSerializer<V, byte[]> valueSerializer;
	private final FixLengthSerializer<K, byte[]> keySerializer;
	
	private final Comparator<K> comparator;
	
	@Inject
	public LeafPageManager(
			PageManager<RawPage> bpm, 
			FixLengthSerializer<V, byte[]> valueSerializer,
			FixLengthSerializer<K, byte[]> keySerializer,
			Comparator<K> comparator) {
		super(bpm);
		this.valueSerializer = valueSerializer;
		this.keySerializer = keySerializer;
        this.comparator = comparator;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.AbstractPageManager#createObjectPage()
	 */
	@Override
	protected LeafNode<K, V> createObjectPage(RawPage page) {
		return new LeafNode<K, V>(page, keySerializer, valueSerializer, comparator, this, 1);
	}

}
