/**
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 * 
 * (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */
package com.freshbourne.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
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
	
	private ArrayBlockingQueue<HashPage> cacheQueue;
	private HashMap<Integer, HashPage> cache;
	
	
	public BufferPoolManagerImpl(ResourceManager rm, int cacheSize) {
		this.rm = rm;
		this.cacheSize = cacheSize;
		
		cacheQueue = new ArrayBlockingQueue<HashPage>(cacheSize);
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.ResourceManager#newPage()
	 */
	@Override
	public HashPage newPage() {
		HashPage p = new HashPage(ByteBuffer.allocate(rm.pageSize()), rm, 0);
		p.initialize();
		return p;
	}

	
	@Override
	public HashPage getPage(int pageId) throws IOException {
		if(cache.get(pageId) != null)
			return cache.get(pageId);
		
		return addToCache(rm.readPage(pageId));
	}


	private HashPage addToCache(HashPage p) throws IOException{
		if(cacheQueue.remainingCapacity() == 0){
			HashPage toRemove = cacheQueue.poll();
			rm.writePage(toRemove);
			cache.remove(toRemove);
		}
		
		cacheQueue.add(p);
		cache.put(p.id(), p);
		return p;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.BufferPoolManager#createPage()
	 */
	@Override
	public HashPage createPage() throws IOException {
		HashPage p = newPage();
		p = rm.addPage(p);
		return addToCache(p);
	}
}
