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
	
	
	@Before
	public void setUp() throws IOException{
		bpm = createBufferPoolManager();
		cacheSize = getCacheSize();
	}
	
	protected abstract BufferPoolManager createBufferPoolManager();
	protected abstract int getCacheSize();

	@Test
	public void shouldReturnCorrectPagesEvenIfExceededCache() throws IOException {
		RawPage page = bpm.createPage();
		long id = page.id();
		long valueToCompare = 123345L;
		page.bufferAtZero().putLong(valueToCompare);
		page.setModified(true);
		
		for(int i = 0; i < cacheSize * 3; i++) // make sure all cache is replaced
			bpm.createPage();
		
		assertEquals(valueToCompare, bpm.getPage(id).bufferAtZero().getLong());
	}
	
}
