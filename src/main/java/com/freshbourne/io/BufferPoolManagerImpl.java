/**
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 * 
 * (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */
package com.freshbourne.io;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * 
 * provides access to pages
 * 
 * @author "Robin Wenglewski <robin@wenglewski.de>"
 *
 */
public class BufferPoolManagerImpl implements BufferPoolManager {
	
	private final ResourceManager rm;
	private final int cacheSize;
	
	private ArrayBlockingQueue<Page> cache;
	
	
	public BufferPoolManagerImpl(ResourceManager rm, int cacheSize) {
		this.rm = rm;
		this.cacheSize = cacheSize;
		
		cache = new ArrayBlockingQueue<Page>(cacheSize);
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.ResourceManager#open()
	 */
	@Override
	public void open() throws IOException {
		rm.open();
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.ResourceManager#newPage()
	 */
	@Override
	public Page newPage() throws IOException {
		return addToCache(rm.newPage());
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.ResourceManager#writePage(com.freshbourne.io.Page)
	 */
	@Override
	public void writePage(Page page) throws IOException {
		rm.writePage(page);
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.ResourceManager#readPage(int)
	 */
	@Override
	public Page readPage(int pageId) throws IOException {
		return addToCache(rm.readPage(pageId));
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.ResourceManager#close()
	 */
	@Override
	public void close() {
		rm.close();
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.ResourceManager#getPageSize()
	 */
	@Override
	public int getPageSize() {
		return rm.getPageSize();
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.ResourceManager#getNumberOfPages()
	 */
	@Override
	public int getNumberOfPages() throws IOException {
		return rm.getNumberOfPages();
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.ResourceManager#isOpen()
	 */
	@Override
	public boolean isOpen() {
		return rm.isOpen();
	}
	
	
	private Page addToCache(Page p) throws IOException{
		if(cache.remainingCapacity() == 0){
			rm.writePage(cache.poll());
		}
		
		cache.add(p);
		return p;
	}
}