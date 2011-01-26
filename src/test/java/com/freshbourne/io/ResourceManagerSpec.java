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
		assertEquals(0, rm.numberOfPages());
		page = rm.createPage();
		assertEquals(1, rm.numberOfPages());
		rm.createPage();
		assertEquals(2, rm.numberOfPages());
		
		long longToCompare = 12345L;
		ByteBuffer buf = page.bufferAtZero();
		buf.putLong(longToCompare);
		rm.writePage(page);
		
		assertEquals(2, rm.numberOfPages());
		assertEquals(longToCompare, rm.readPage(page.id()).bufferAtZero().getLong());
		
		rm.close();
		
		// throw away all local variables
		rm = createOpenResourceManager();
		
		assertEquals(2, rm.numberOfPages());
		assertEquals(longToCompare, rm.readPage(page.id()).bufferAtZero().getLong());
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
	
}