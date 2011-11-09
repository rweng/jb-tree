/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 *
 * http://creativecommons.org/licenses/by-nc/3.0/
 *
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2011 "Robin Wenglewski <robin@wenglewski.de>"
 */
package de.rwhq.btree;

import de.rwhq.io.rm.AbstractPageManager;
import de.rwhq.io.rm.DataPageManager;
import de.rwhq.io.rm.PageManager;
import de.rwhq.io.rm.RawPage;
import de.rwhq.serializer.FixLengthSerializer;

import java.util.Comparator;

class InnerNodeManager<K, V> extends AbstractPageManager<InnerNode<K, V>> {

	private final FixLengthSerializer<K, byte[]> keySerializer;
	
	private final DataPageManager<K> keyPageManager;
	
	private final Comparator<K> comparator;
	private final PageManager<LeafNode<K, V>> leafPageManager;
	
	public InnerNodeManager(
			final PageManager<RawPage> bpm,
			final DataPageManager<K> keyPageManager,
			final DataPageManager<V> valuePageManager,
			final LeafPageManager<K, V> leafPageManager,
			final FixLengthSerializer<K, byte[]> keySerializer,
			final Comparator<K> comparator) {
		super(bpm);
		this.keySerializer = keySerializer;
        this.keyPageManager = keyPageManager;
        this.leafPageManager = leafPageManager;
        this.comparator = comparator;
	}

	/* (non-Javadoc)
	 * @see AbstractPageManager#createObjectPage()
	 */
	@Override
	protected InnerNode<K, V> createObjectPage(final RawPage page) {
		return new InnerNode<K, V>(page, keySerializer, comparator, keyPageManager, leafPageManager, this);
	}
}
