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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;

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
	private final int cacheSize;
	
	private ArrayBlockingQueue<RawPage> cacheQueue;
	private HashMap<Long, RawPage> cache;
	
	@Inject
	public BufferPoolManagerImpl(ResourceManager rm,@Named("cacheSize") int cacheSize) throws IOException {
		if(!rm.isOpen())
            rm.open();

        this.rm = rm;
        this.cacheSize = cacheSize;
		this.cache = new HashMap<Long, RawPage>();
		
		cacheQueue = new ArrayBlockingQueue<RawPage>(cacheSize);
	}

	
	private RawPage newPage() {
		RawPage p = new RawPage(ByteBuffer.allocate(rm.pageSize()), rm, null);
		return p;
	}

	
	@Override
	public RawPage getPage(long pageId) throws IOException {
		if(cache.get(pageId) != null)
			return cache.get(pageId);
		
		return addToCache(rm.readPage(pageId));
	}


	private RawPage addToCache(RawPage p) throws IOException{
		if(cacheQueue.remainingCapacity() == 0){
			RawPage toRemove = cacheQueue.poll();
			rm.writePage(toRemove);
			cache.remove(toRemove);
		}
		
		cacheQueue.add(p);
		cache.put(p.id(), p);
		return p;
	}

	@Override
	public RawPage createPage() throws IOException {
		RawPage p = newPage();
		p = rm.addPage(p);
		return addToCache(p);
	}

	@Override
	public void removePage(long id) {
		// TODO Auto-generated method stub
		
	}
}
