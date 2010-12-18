/**
 * Copyright (C) 2010 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.io;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DynamicDataPageSpec {
	
	private DynamicDataPage<String> page;
	
	// some test strings to insert
	private String s1 = "blubla";
	private String s2 = "blast";
	private String s3 = "ups";
	
	
	@Before
	public void setUp(){
		page = new DynamicDataPage<String>(
				new byte[1000], 
				new PagePointSerializer(), 
				new StringSerializer());
	}
	
	@Test
	public void shouldHaveToInitialize(){
		assertFalse(page.valid());
		page.initialize();
		assertTrue(page.valid());
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
	
}
