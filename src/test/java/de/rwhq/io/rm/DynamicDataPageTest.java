/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 *
 * http://creativecommons.org/licenses/by-nc/3.0/
 *
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2011 "Robin Wenglewski <robin@wenglewski.de>"
 */
package de.rwhq.io.rm;

import de.rwhq.io.NoSpaceException;
import de.rwhq.serializer.PagePointSerializer;
import de.rwhq.serializer.StringSerializer;
import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.fest.assertions.Assertions.assertThat;


public class DynamicDataPageTest {
	
	private DynamicDataPage<String> page;
	
	// some test strings to insert
	private String s1 = "blubla";
	private String s2 = "blast";
	private String s3 = "ups";
	
	@Before
	public void setUp(){
		page = new DynamicDataPage<String>(new RawPage(ByteBuffer.allocate(PageSize.DEFAULT_PAGE_SIZE), 1), PagePointSerializer.INSTANCE, StringSerializer.INSTANCE);
	}
	
	@Test
	public void shouldHaveToInitialize(){
		assertThat(page.isValid()).isFalse();
		page.initialize();
		assertThat(page.isValid()).isTrue();
		checkAndSetModified(page);
	}
	
	@Test
	public void shouldBeEmptyAfterInitialize() throws InvalidPageException{
		page.initialize();
		assertThat( page.numberOfEntries()).isEqualTo(0);
	}
	
	private void checkAndSetModified(final DynamicDataPage<String> page){
		assertThat(page.rawPage().isModified()).isTrue();
		page.rawPage().setModified(false);
	}
	
	
	@Test(expected = InvalidPageException.class)
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
		assertThat(rest > page.remaining()).isTrue();
	}
	
	@Test(expected = InvalidPageException.class)
	public void shouldThrowAnExceptionOnAddIfNotValid() throws NoSpaceException, InvalidPageException{
		page.add(s1);
		checkAndSetModified(page);
	}
	
	@Test(expected = InvalidPageException.class)
	public void shouldThrowAnExceptionOnGetIfNotValid() throws Exception{
		page.get(0);
	}
	
	@Test(expected = InvalidPageException.class)
	public void shouldThrowAnExceptionOnNumberOfEntriesIfNotValid() throws Exception{
		page.numberOfEntries();
	}
	
	@Test
	public void shouldBeAbleToReturnInsertedItems() throws Exception{
		
		page.initialize();
		
		assertThat( page.rawPage().bufferForReading(0).getInt()).isEqualTo(DynamicDataPage.NO_ENTRIES_INT);
		assertThat( page.numberOfEntries()).isEqualTo(0);
		
		final int id1 = page.add(s1);
		assertThat( page.rawPage().bufferForReading(0).getInt()).isEqualTo(1);
		assertThat( page.numberOfEntries()).isEqualTo(1);
		final int id2 = page.add(s2);
		assertThat( page.rawPage().bufferForReading(0).getInt()).isEqualTo(2);
		assertThat( page.numberOfEntries()).isEqualTo(2);
		final int id3 = page.add(s3);
		assertThat( page.rawPage().bufferForReading(0).getInt()).isEqualTo(3);
		assertThat( page.numberOfEntries()).isEqualTo(3);
		checkAndSetModified(page);
		
		assertThat( page.get(id1)).isEqualTo(s1);
		assertThat( page.get(id3)).isEqualTo(s3);
		
		page.remove(id1);
		assertThat( page.rawPage().bufferForReading(0).getInt()).isEqualTo(2);
		assertThat( page.numberOfEntries()).isEqualTo(2);
		checkAndSetModified(page);
		assertThat( page.get(id2)).isEqualTo(s2);
		assertThat( page.get(id3)).isEqualTo(s3);
	}
	
	@Test
	public void shouldReturnNullIfEntryWasRemoved() throws Exception {
		
		page.initialize();
		
		final int id = page.add(s3);
		page.remove(id);
		checkAndSetModified(page);
		
		assertThat( page.get(id)).isEqualTo(null);
		assertThat( page.get(id + 5)).isEqualTo(null);
		
	}
	
	@Test
	public void remainingMethodShouldAdjustWhenInsertingOrRemovingEntries() throws NoSpaceException, InvalidPageException {
		page.initialize();
		
		final int r1 = page.remaining();
		final int id1 = page.add(s1);
		final int r2 = page.remaining();
		assertThat(r1 > r2).isTrue();
		final int id2 = page.add(s2);
		final int r3 = page.remaining();
		assertThat(r2 > r3).isTrue();
		page.remove(id1);
		assertThat( page.remaining()).isEqualTo(r1 - (r2 - r3));
		page.remove(id2);
		assertThat( page.remaining()).isEqualTo(r1);
	}
	
	@Test public void remainingValueShouldBeCorrectAfterReload() throws NoSpaceException, InvalidPageException{
		page.initialize();
		
		page.add(s1);
		page.add(s2);
		final int r = page.remaining();
		page = new DynamicDataPage<String>(page.rawPage(), page.pagePointSerializer(), page.dataSerializer());
		page.load();
		assertThat( page.remaining()).isEqualTo(r);
	}
	
	@Test
	public void shouldHaveSameSizeAfterInsertAndRemove() throws NoSpaceException, InvalidPageException {
		page.initialize();
		
		final int remaining = page.remaining();
		final int id = page.add("blast");
		assertThat(remaining > page.remaining()).isTrue();
		page.remove(id);
		assertThat( page.remaining()).isEqualTo(remaining);
	}
	
	@Test
	public void shouldLoadCorrectly() throws Exception{
		assertThat(page.isValid()).isFalse();
		page.initialize();
		assertThat(page.isValid()).isTrue();
		final int id = page.add(s1);
		assertThat( page.numberOfEntries()).isEqualTo(1);
		page = new DynamicDataPage<String>(page.rawPage(), page.pagePointSerializer(), page.dataSerializer());
		assertThat(page.isValid()).isFalse();
		page.load();
		assertThat(page.isValid()).isTrue();
		assertThat( page.numberOfEntries()).isEqualTo(1);
		assertThat( page.get(id)).isEqualTo(s1);
	}
}
