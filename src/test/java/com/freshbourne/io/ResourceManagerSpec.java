/**
 * Copyright (C) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public abstract class ResourceManagerSpec {
	private ResourceManager rm;
	private RawPage page;
	
	@Before
	public void setUp() throws IOException {
		rm = createOpenResourceManager();
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
		assertEquals(1, rm.numberOfPages()); // header page
	}
	
	
	
	@Test(expected= ResourceNotOpenException.class)
	public void shouldThrowExceptionIfResourceClosed() throws IOException{
		rm.close();
		rm.createPage();
	}
	
	@Test(expected= PageNotFoundException.class)
	public void shouldThrowExceptionIfPageToWriteDoesNotExist() throws IOException{
        page = new RawPage(ByteBuffer.allocate(PageSize.DEFAULT_PAGE_SIZE), 3423L);
        rm.writePage(page);
	}
	
	@Test
	public void shouldGenerateDifferentIdsForEachPage() throws IOException{
		assertTrue(rm.createPage().id() != rm.createPage().id());
	}
	
	@Test
	public void shouldReadWrittenPages() throws IOException{
		page = rm.createPage();
		page.buffer().position(0);
		page.buffer().putInt(1234);
		rm.writePage(page);
		
		assertEquals(rm.readPage(page.id()).buffer(), page.buffer());
	}
	
	@Test
	public void addingAPageShouldIncreaseNumberOfPages() throws IOException{
		int num = rm.numberOfPages();
		rm.createPage();
		assertEquals(num + 1, rm.numberOfPages());
	}
	
	@Test
	public void shouldBeAbleToReadPagesAfterReopen() throws IOException{
		page = rm.createPage();
		assertEquals(0 + 1, rm.numberOfPages());
		page = rm.createPage();
		assertEquals(1 + 1, rm.numberOfPages());
		RawPage newPage2 = rm.createPage();
		assertEquals(2 + 1, rm.numberOfPages());
		assertEquals(rm.readPage(newPage2.id()).buffer(), page.buffer());
		
		rm.close();
		
		// throw away all local variables
		rm = createOpenResourceManager();
		
		assertEquals(2 + 1, rm.numberOfPages());
		assertEquals(rm.readPage(newPage2.id()).buffer(), page.buffer());
	}
	
	@Test(expected= WrongPageSizeException.class)
	public void shouldThrowExceptionIfWrongPageSize() throws IOException{
		page = new RawPage(ByteBuffer.allocate(PageSize.DEFAULT_PAGE_SIZE + 1), 1L);
        rm.addPage(page);
	}
	
	@Test
	public void shouldBeAbleToRemovePages() throws Exception{
		RawPage p1 = rm.createPage();
		RawPage p2 = rm.createPage();
		int i = rm.numberOfPages();
		long p1Id = p1.id();
		
		rm.removePage(p1Id);
		assertEquals(i - 1, rm.numberOfPages());
		try{
			rm.readPage(p1Id);
			fail("reading a non-existent page should throw an exeption");
		} catch( Exception expected){
		}
		
		RawPage p3 = rm.createPage();
		assertEquals(i, rm.numberOfPages());
		rm.removePage(p3.id());
		assertEquals(i - 1, rm.numberOfPages());
	}

	
	protected abstract ResourceManager createOpenResourceManager();
}
