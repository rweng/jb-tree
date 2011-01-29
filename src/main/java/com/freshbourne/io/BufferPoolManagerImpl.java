/*
 * Copyright (c) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 */

/**
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 * 
 * (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */
package com.freshbourne.io;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

/**
 * 
 * provides access to pages. Whereas the ResourceManager only reads and writes
 * Pages, the BufferPoolManager also caches them and makes sure old pages
 * are written to disk.
 * 
 * @author "Robin Wenglewski <robin@wenglewski.de>"
 *
 */
public class BufferPoolManagerImpl implements BufferPoolManager {
	
	private final ResourceManager rm;
	private int cacheSize;
	
	private Queue<RawPage> cacheQueue;
	private HashMap<Long, RawPage> cache;
	
	@Inject
	public BufferPoolManagerImpl(ResourceManager rm, @Named("cacheSize") int cacheSize) {
		this.rm = rm;
        this.cacheSize = cacheSize;
		this.cache = new HashMap<Long, RawPage>();
		cacheQueue = new LinkedList<RawPage>();
	}

	
		@Override
	public RawPage getPage(long pageId) {
		if(cache.get(pageId) != null)
			return cache.get(pageId);
		
		return addToCache(rm.readPage(pageId));
	}


	private RawPage addToCache(RawPage p) {
		if(cacheQueue.size() == cacheSize){
			RawPage toRemove = cacheQueue.poll();
			rm.writePage(toRemove);
			cache.remove(toRemove);
		}
		
		cacheQueue.add(p);
		cache.put(p.id(), p);
		return p;
	}

	@Override
	public RawPage createPage() {
		RawPage p = rm.createPage();
		return addToCache(p);
	}

	@Override
	public void removePage(long id) {
		if(cache.containsKey(id)){
			RawPage page = cache.get(id);
			cacheQueue.remove(page);
			cache.remove(id);
		}
		
		rm.removePage(id);
	}


	/* (non-Javadoc)
	 * @see com.freshbourne.io.BufferPoolManager#flush()
	 */
	@Override
	public void flush() {
		for(RawPage rawPage : cache.values()){
			if(rawPage.isModified()){
				rm.writePage(rawPage);
				rawPage.setModified(false);
			}
		}
	}


	/* (non-Javadoc)
	 * @see com.freshbourne.io.BufferPoolManager#clearCache()
	 */
	@Override
	public void clearCache() {
		cache.clear();
		cacheQueue.clear();
	}


	/* (non-Javadoc)
	 * @see com.freshbourne.io.BufferPoolManager#getResourceManager()
	 */
	@Override
	public ResourceManager getResourceManager() {
		return rm;
	}


	


}
