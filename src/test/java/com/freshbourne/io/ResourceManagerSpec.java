/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */
package com.freshbourne.io;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public abstract class ResourceManagerSpec {
	private ResourceManager rm;
	private RawPage page;
	
	@Before
	public void setUp() throws IOException {
		rm = createNewOpenResourceManager();
	}
	
	@After
	public void tearDown() throws IOException{
		rm.close();
	}

	
	
// ******** TESTS **********
	
	@Test
	public void shouldBeEmptyAtFirst() throws IOException{
		assertTrue(rm != null);
		assertEquals(PageSize.DEFAULT_PAGE_SIZE, rm.pageSize());
		assertEquals(0, rm.numberOfPages()); // 0 pages
	}
	
	
	@Test(expected= IllegalStateException.class)
	public void shouldThrowExceptionIfResourceClosed() throws IOException{
		rm.close();
		rm.createPage();
	}
	
	@Test(expected= PageNotFoundException.class)
	public void shouldThrowExceptionIfPageToWriteDoesNotExist() throws IOException{
        page = new RawPage(ByteBuffer.allocate(PageSize.DEFAULT_PAGE_SIZE), 3423);
        rm.writePage(page);
	}
	
	@Test
	public void shouldGenerateDifferentIdsForEachPage() throws IOException{
		assertTrue(rm.createPage().id() != rm.createPage().id());
	}
	
	@Test
	public void shouldReadWrittenPages() throws IOException{
		page = rm.createPage();
		page.bufferForWriting(0).putInt(1234);
		rm.writePage(page);
		
		assertEquals(rm.getPage(page.id()).bufferForWriting(0), page.bufferForWriting(0));
	}
	
	@Test
	public void addingAPageShouldIncreaseNumberOfPages() throws IOException{
		int num = rm.numberOfPages();
		rm.createPage();
		assertEquals(num + 1, rm.numberOfPages());
	}
	
	@Test
	public void shouldBeAbleToReadPagesAfterReopen() throws IOException{
		assertEquals(0, rm.numberOfPages());
		page = rm.createPage();
		assertEquals(1, rm.numberOfPages());
		rm.createPage();
		assertEquals(2, rm.numberOfPages());
		
		long longToCompare = 12345L;
		ByteBuffer buf = page.bufferForWriting(0);
		buf.putLong(longToCompare);
		rm.writePage(page);
		
		assertEquals(2, rm.numberOfPages());
		assertEquals(longToCompare, rm.getPage(page.id()).bufferForWriting(0).getLong());
		
		rm.close();
		
		// throw away all local variables
		rm = createOpenResourceManager();
		
		assertEquals(2, rm.numberOfPages());
		assertEquals(longToCompare, rm.getPage(page.id()).bufferForWriting(0).getLong());
	}
	
	@Test(expected= WrongPageSizeException.class)
	public void shouldThrowExceptionIfWrongPageSize() throws IOException{
		page = new RawPage(ByteBuffer.allocate(PageSize.DEFAULT_PAGE_SIZE + 1), 1);
        rm.addPage(page);
	}
	
	@Test @Ignore("does not work yet")
	public void shouldBeAbleToRemovePages() throws Exception{
		RawPage p1 = rm.createPage();
		RawPage p2 = rm.createPage();
		int i = rm.numberOfPages();
		Integer p1Id = p1.id();
		
		rm.removePage(p1Id);
		assertEquals(i - 1, rm.numberOfPages());
		try{
			rm.getPage(p1Id);
			fail("reading a non-existent page should throw an exeption");
		} catch( Exception expected){
		}
		
		RawPage p3 = rm.createPage();
		assertEquals(i, rm.numberOfPages());
		rm.removePage(p3.id());
		assertEquals(i - 1, rm.numberOfPages());
	}
	
	@Test
	public void shouldBeAbleToCreateAMassiveNumberOfPages(){
		List<Integer> ids = new ArrayList<Integer>();
		RawPage p1 = rm.createPage();
		p1.bufferForWriting(0).putInt(111);
		int size = 10000;
		for(int i = 0; i < size; i++){
			ids.add(rm.createPage().id());
		}
		RawPage p2 = rm.createPage();
		p2.bufferForWriting(0).putInt(222);
		
		assertEquals(111, rm.getPage(p1.id()).bufferForReading(0).getInt());
		assertEquals(222, rm.getPage(p2.id()).bufferForReading(0).getInt());

		assertEquals(size + 2, rm.numberOfPages());
		for(int i = 0; i < size; i++){
			Integer id = ids.get(0);
			assertEquals(id, rm.getPage(id).id());
		}
		
		
		
		
	}

	
	/**
	 * should reset the ResourceManager and create a completely new ResourceManager
	 * @return
	 */
	protected abstract ResourceManager createNewOpenResourceManager();
	
	/**
	 * should just create a new ResourceManager, without resetting the resource
	 * @return
	 */
	protected abstract ResourceManager createOpenResourceManager();
	
	/**
	 * implement the test for Sync
	 */
	public abstract void testSync();
	
}
