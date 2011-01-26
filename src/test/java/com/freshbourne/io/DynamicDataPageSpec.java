/*
 * Copyright (c) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 */
package com.freshbourne.io;

import java.nio.ByteBuffer;
import org.junit.Before;
import org.junit.Test;

import com.freshbourne.serializer.PagePointSerializer;
import com.freshbourne.serializer.StringSerializer;
import static org.junit.Assert.*;

public class DynamicDataPageSpec {
	
	private DynamicDataPage<String> page;
	
	// some test strings to insert
	private String s1 = "blubla";
	private String s2 = "blast";
	private String s3 = "ups";
	
	@Before
	public void setUp(){
		page = new DynamicDataPage<String>(new RawPage(ByteBuffer.allocate(PageSize.DEFAULT_PAGE_SIZE), 1L), PagePointSerializer.INSTANCE, StringSerializer.INSTANCE);
	}
	
	@Test
	public void shouldHaveToInitialize(){
		assertFalse(page.isValid());
		page.initialize();
		assertTrue(page.isValid());
	}
	
	@Test
	public void shouldBeEmptyAfterInitialize() throws InvalidPageException{
		page.initialize();
		assertEquals(0, page.numberOfEntries());
	}
	
	
	
	@Test(expected= Exception.class)
	public void shouldThrowAnExceptionIfInvalidEntryId() throws Exception{
		page.get(432);
	}
	
	@Test
	public void shouldGetSmallerWhenInsertingSomething() throws NoSpaceException, InvalidPageException{
		page.initialize();
		
		int rest = page.remaining();
		page.add("bla");
		assertTrue(rest > page.remaining());
	}
	
	@Test(expected= InvalidPageException.class)
	public void shouldThrowAnExceptionOnAddIfNotValid() throws NoSpaceException, InvalidPageException{
		page.add(s1);
	}
	
	@Test(expected= InvalidPageException.class)
	public void shouldThrowAnExceptionOnGetIfNotValid() throws Exception{
		page.get(0);
	}
	
	@Test(expected= InvalidPageException.class)
	public void shouldThrowAnExceptionOnNumberOfEntriesIfNotValid() throws Exception{
		page.numberOfEntries();
	}
	
	@Test
	public void shouldBeAbleToReturnInsertedItems() throws Exception{
		
		page.initialize();
		
		int id1 = page.add(s1);
		int id2 = page.add(s2);
		int id3 = page.add(s3);
		
		assertEquals(s1, page.get(id1));
		assertEquals(s3, page.get(id3));
		
		page.remove(id1);
		assertEquals(s2, page.get(id2));
		assertEquals(s3, page.get(id3));
	}
	
	@Test
	public void shouldReturnNullIfEntryWasRemoved() throws Exception {
		
		page.initialize();
		
		int id = page.add(s3);
		page.remove(id);
		assertEquals(null, page.get(id));
		assertEquals(null, page.get(id + 5));
		
	}
	
	@Test
	public void remainingMethodShouldAdjustWhenInsertingOrRemovingEntries() throws NoSpaceException, InvalidPageException{
		page.initialize();
		
		int r1 = page.remaining();
		int id1 = page.add(s1);
		int r2 = page.remaining();
		assertTrue(r1 > r2);
		int id2 = page.add(s2);
		int r3 = page.remaining();
		assertTrue(r2 > r3);
		page.remove(id1);
		assertEquals(r1 - (r2 - r3), page.remaining());
		page.remove(id2);
		assertEquals(r1, page.remaining());
	}
	
	@Test public void remainingValueShouldBeCorrectAfterReload() throws NoSpaceException, InvalidPageException{
		page.initialize();
		
		page.add(s1);
		page.add(s2);
		int r = page.remaining();
		page = new DynamicDataPage<String>(page.rawPage(), page.pagePointSerializer(), page.dataSerializer());
		page.load();
		assertEquals(r, page.remaining());
	}
	
	@Test
	public void shouldHaveSameSizeAfterInsertAndRemove() throws NoSpaceException, InvalidPageException{
		page.initialize();
		
		int remaining = page.remaining();
		int id = page.add("blast");
		assertTrue(remaining > page.remaining());
		page.remove(id);
		assertEquals(remaining, page.remaining());
	}
	
	@Test
	public void shouldLoadCorrectly() throws Exception{
		assertFalse(page.isValid());
		page.initialize();
		assertTrue(page.isValid());
		int id = page.add(s1);
		assertEquals(1, page.numberOfEntries());
		page = new DynamicDataPage<String>(page.rawPage(), page.pagePointSerializer(), page.dataSerializer());
		assertFalse(page.isValid());
		page.load();
		assertTrue(page.isValid());
		assertEquals(1, page.numberOfEntries());
		assertEquals(s1, page.get(id));
	}
}
