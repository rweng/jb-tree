/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 *
 * http://creativecommons.org/licenses/by-nc/3.0/
 *
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2011 "Robin Wenglewski <robin@wenglewski.de>"
 */
package com.freshbourne.io;

import com.freshbourne.serializer.PagePointSerializer;
import com.freshbourne.serializer.StringSerializer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.nio.ByteBuffer;

import static org.testng.Assert.*;

public class DynamicDataPageTest {
	
	private DynamicDataPage<String> page;
	
	// some test strings to insert
	private String s1 = "blubla";
	private String s2 = "blast";
	private String s3 = "ups";
	
	@BeforeMethod
	public void setUp(){
		page = new DynamicDataPage<String>(new RawPage(ByteBuffer.allocate(PageSize.DEFAULT_PAGE_SIZE), 1), PagePointSerializer.INSTANCE, StringSerializer.INSTANCE);
	}
	
	@Test
	public void shouldHaveToInitialize(){
		assertFalse(page.isValid());
		page.initialize();
		assertTrue(page.isValid());
		checkAndSetModified(page);
	}
	
	@Test
	public void shouldBeEmptyAfterInitialize() throws InvalidPageException{
		page.initialize();
		assertEquals(0, page.numberOfEntries());
	}
	
	private void checkAndSetModified(final DynamicDataPage<String> page){
		assertTrue(page.rawPage().isModified());
		page.rawPage().setModified(false);
	}
	
	
	@Test(expectedExceptions = InvalidPageException.class)
	public void shouldThrowAnExceptionIfInvalidEntryId() throws Exception{
		page.get(432);
	}
	
	@Test
	public void remainingShouldGetSmallerWhenInsertingSomething() throws NoSpaceException, InvalidPageException{
		page.initialize();
		checkAndSetModified(page);
		
		final int rest = page.remaining();
		page.add("bla");
		checkAndSetModified(page);
		assertTrue(rest > page.remaining());
	}
	
	@Test(expectedExceptions = InvalidPageException.class)
	public void shouldThrowAnExceptionOnAddIfNotValid() throws NoSpaceException, InvalidPageException{
		page.add(s1);
		checkAndSetModified(page);
	}
	
	@Test(expectedExceptions = InvalidPageException.class)
	public void shouldThrowAnExceptionOnGetIfNotValid() throws Exception{
		page.get(0);
	}
	
	@Test(expectedExceptions = InvalidPageException.class)
	public void shouldThrowAnExceptionOnNumberOfEntriesIfNotValid() throws Exception{
		page.numberOfEntries();
	}
	
	@Test
	public void shouldBeAbleToReturnInsertedItems() throws Exception{
		
		page.initialize();
		
		assertEquals(DynamicDataPage.NO_ENTRIES_INT, page.rawPage().bufferForReading(0).getInt());
		assertEquals(0, page.numberOfEntries());
		
		final int id1 = page.add(s1);
		assertEquals(1, page.rawPage().bufferForReading(0).getInt());
		assertEquals(1, page.numberOfEntries());
		final int id2 = page.add(s2);
		assertEquals(2, page.rawPage().bufferForReading(0).getInt());
		assertEquals(2, page.numberOfEntries());
		final int id3 = page.add(s3);
		assertEquals(3, page.rawPage().bufferForReading(0).getInt());
		assertEquals(3, page.numberOfEntries());
		checkAndSetModified(page);
		
		assertEquals(s1, page.get(id1));
		assertEquals(s3, page.get(id3));
		
		page.remove(id1);
		assertEquals(2, page.rawPage().bufferForReading(0).getInt());
		assertEquals(2, page.numberOfEntries());
		checkAndSetModified(page);
		assertEquals(s2, page.get(id2));
		assertEquals(s3, page.get(id3));
	}
	
	@Test
	public void shouldReturnNullIfEntryWasRemoved() throws Exception {
		
		page.initialize();
		
		final int id = page.add(s3);
		page.remove(id);
		checkAndSetModified(page);
		
		assertEquals(null, page.get(id));
		assertEquals(null, page.get(id + 5));
		
	}
	
	@Test
	public void remainingMethodShouldAdjustWhenInsertingOrRemovingEntries() throws NoSpaceException, InvalidPageException{
		page.initialize();
		
		final int r1 = page.remaining();
		final int id1 = page.add(s1);
		final int r2 = page.remaining();
		assertTrue(r1 > r2);
		final int id2 = page.add(s2);
		final int r3 = page.remaining();
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
		final int r = page.remaining();
		page = new DynamicDataPage<String>(page.rawPage(), page.pagePointSerializer(), page.dataSerializer());
		page.load();
		assertEquals(r, page.remaining());
	}
	
	@Test
	public void shouldHaveSameSizeAfterInsertAndRemove() throws NoSpaceException, InvalidPageException{
		page.initialize();
		
		final int remaining = page.remaining();
		final int id = page.add("blast");
		assertTrue(remaining > page.remaining());
		page.remove(id);
		assertEquals(remaining, page.remaining());
	}
	
	@Test
	public void shouldLoadCorrectly() throws Exception{
		assertFalse(page.isValid());
		page.initialize();
		assertTrue(page.isValid());
		final int id = page.add(s1);
		assertEquals(1, page.numberOfEntries());
		page = new DynamicDataPage<String>(page.rawPage(), page.pagePointSerializer(), page.dataSerializer());
		assertFalse(page.isValid());
		page.load();
		assertTrue(page.isValid());
		assertEquals(1, page.numberOfEntries());
		assertEquals(s1, page.get(id));
	}
}
