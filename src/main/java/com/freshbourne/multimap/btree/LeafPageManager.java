/**
 * Copyright (C) 2010 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.multimap.btree;

import java.io.IOException;

import com.freshbourne.io.BufferPoolManager;
import com.freshbourne.io.HashPage;
import com.freshbourne.io.PageManager;
import com.google.inject.Inject;

public class LeafPageManager<K,V> implements PageManager<LeafPage<K,V>> {

	private final BufferPoolManager bpm;
	
	@Inject
	public LeafPageManager(BufferPoolManager bpm) {
		this.bpm = bpm;
	}
	
	/* (non-Javadoc)
	 * @see com.freshbourne.io.PageManager#createPage()
	 */
	@Override
	public LeafPage<K, V> createPage() throws IOException {
		HashPage p = bpm.createPage();
		LeafPage<K, V> result = new LeafPage<K, V>();
		result.addObserver(p);
		return result;
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
