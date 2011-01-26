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

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public abstract class BufferPoolManagerSpec {
	private BufferPoolManager bpm;
	private int cacheSize;
	private final long valueToCompare = 12345L;
	
	
	@Before
	public void setUp() throws IOException{
		bpm = createBufferPoolManager();
		cacheSize = getCacheSize();
	}
	
	protected abstract BufferPoolManager createBufferPoolManager();
	protected abstract int getCacheSize();
	
	private Long createPageWithCompareValueAndLoop(){
		RawPage page = bpm.createPage();
		long id = page.id();
		page.bufferAtZero().putLong(valueToCompare);
		page.setModified(true);
		
		for(int i = 0; i < cacheSize * 3; i++) // make sure all cache is replaced
			bpm.createPage();
		
		return id;
	}

	@Test
	public void shouldReturnCorrectPagesEvenIfExceededCache() throws IOException {
		Long pageId = createPageWithCompareValueAndLoop();
		
		assertEquals(valueToCompare, bpm.getPage(pageId).bufferAtZero().getLong());
	}
	
	@Test public void flush(){
		Long pageId = createPageWithCompareValueAndLoop();
		bpm.flush();
		assertEquals(valueToCompare, bpm.getPage(pageId).bufferAtZero().getLong());
	}
	
	@Test public void clearCache(){
		Long pageId = createPageWithCompareValueAndLoop();
		bpm.clearCache();
		assertEquals(valueToCompare, bpm.getPage(pageId).bufferAtZero().getLong());		
	}
	
}
