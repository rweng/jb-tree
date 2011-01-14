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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static org.mockito.Mockito.*;

public class BufferPoolManagerSpec {
	private BufferPoolManager bpm;
	
	private final int cacheSize = 30;
	
	@Mock private ResourceManager mockRM;
	@Mock private RawPage mockPageWithoutId;
	@Mock private RawPage mockPageWithId;
	
	@Before
	public void setUp() throws IOException{
		MockitoAnnotations.initMocks(this);
		
		when(mockRM.pageSize()).thenReturn(PageSize.DEFAULT_PAGE_SIZE);
		
		when(mockPageWithId.id()).thenReturn(1);
		when(mockRM.addPage(any(RawPage.class))).thenReturn(mockPageWithId);
		
		//bpm = new BufferPoolManagerImpl(mockRM, cacheSize);
	}
	
	@Test
	public void shouldCachePages() throws IOException {
		// RawPage p = bpm.createPage();
		// bpm.getPage(p.id());
		verify(mockRM, times(0)).readPage(anyInt());
	}
	
//	@Test
//	public void shouldCreateInitializedHashPages() throws IOException{
//		HashPage p = bpm.newPage();
//		assertNotNull(p);
//		assertTrue(p.valid());
//		assertEquals(PageSize.DEFAULT_PAGE_SIZE, p.buffer().capacity());
//	}
//	
//	@Test
//	public void shouldNotPersistNewPages() throws IOException {
//		bpm.newPage();
//		verify(mockRM, times(0)).writePage(any(HashPage.class));
//	}
	
	@Test
	public void shouldPersistCreatedPages() throws IOException{
		// bpm.createPage();
		// verify(mockRM).writePage(any(RawPage.class));
	}
	
	
}
