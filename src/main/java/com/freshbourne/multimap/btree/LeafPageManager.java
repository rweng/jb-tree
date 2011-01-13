/*
 * Copyright (c) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 */

package com.freshbourne.multimap.btree;

import com.freshbourne.io.*;
import com.freshbourne.serializer.PagePointSerializer;
import com.google.inject.Inject;

import java.io.IOException;

public class LeafPageManager<K,V> implements PageManager<LeafPage<K,V>> {

	private final BufferPoolManager bpm;
	private final PagePointSerializer ppSerializer;
	
	private DataPageManager<K> keyPageManager;
	private DataPageManager<V> valuePageManager;
	
	@Inject
	public LeafPageManager(
			BufferPoolManager bpm, 
			DataPageManager<K> keyPageManager,
			DataPageManager<V> valuePageManager,
			PagePointSerializer ppSerializer) {
		this.bpm = bpm;
		this.ppSerializer = ppSerializer;
	}
	
	/* (non-Javadoc)
	 * @see com.freshbourne.io.PageManager#createPage()
	 */
	@Override
	public LeafPage<K, V> createPage() throws IOException {
		RawPage p = bpm.createPage();
		return new LeafPage<K, V>(p, keyPageManager, valuePageManager, ppSerializer);
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.PageManager#getPage(int)
	 */
	@Override
	public LeafPage<K, V> getPage(int id) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.PageManager#removePage(int)
	 */
	@Override
	public void removePage(int id) {
		// TODO Auto-generated method stub
		
	}

}
