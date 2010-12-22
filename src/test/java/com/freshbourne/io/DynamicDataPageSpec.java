/**
 * Copyright (C) 2010 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.io;

import java.nio.ByteBuffer;
import java.util.Observer;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DynamicDataPageSpec {
	
	private DynamicDataPage<String> page;
	
	// some test strings to insert
	private String s1 = "blubla";
	private String s2 = "blast";
	private String s3 = "ups";
	
	
	@Before
	public void setUp(){
		page = new DynamicDataPage<String>(
				ByteBuffer.allocate(PageSize.DEFAULT_PAGE_SIZE), 
				new PagePointSerializer(), 
				new StringSerializer());
	}
	
	@Test
	public void shouldHaveToInitialize(){
		Observer mockObserver = mock(Observer.class);
		page.addObserver(mockObserver);
		
		assertFalse(page.valid());
		page.initialize();
		assertTrue(page.valid());
		verify(mockObserver).update(page, null);
	}
	
	@Test(expected= Exception.class)
	public void shouldThrowAnExceptionIfInvalidEntryId() throws Exception{
		page.get(432);
	}
	
	@Test
	public void shouldGetSmallerWhenInsertingSomething() throws NoSpaceException{
		int rest = page.remaining();
		page.add("bla");
		assertTrue(rest > page.remaining());
	}
	
	@Test
	public void shouldBeAbleToReturnInsertedItems() throws Exception{
		
		int id1 = page.add(s1);
		int id2 = page.add(s2);
		int id3 = page.add(s3);
		
		assertEquals(s1, page.get(id1));
		assertEquals(s3, page.get(id3));
		
		page.remove(id1);
		assertEquals(s2, page.get(id2));
		assertEquals(s3, page.get(id3));
	}
	
	@Test(expected= Exception.class)
	public void shouldThrowAnExceptionWhenEntryWasRemoved() throws Exception {
		int id = page.add(s3);
		page.remove(id);
		page.get(id);
	}
	
	@Test
	public void shouldNotifyObservers() throws NoSpaceException, ElementNotFoundException{
		Observer mockObserver = mock(Observer.class);
		page.addObserver(mockObserver);
		
		int id = page.add("blast");
		verify(mockObserver).update(page, null);
		
		page.remove(id);
		verify(mockObserver, times(2)).update(page, null);
	}
	
	@Test
	public void shouldHaveSameSizeAfterInsertAndRemove() throws NoSpaceException, ElementNotFoundException{
		int size = page.remaining();
		int id = page.add("blast");
		page.remove(id);
		assertEquals(size, page.remaining());
	}
	
}
