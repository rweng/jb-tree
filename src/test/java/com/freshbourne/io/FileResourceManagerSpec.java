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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FileResourceManagerSpec {
	
	private ResourceManager rm;
	private final File file = new File("/tmp/frm_test");
	private RawPage page;

	@Before
	public void setUp() throws IOException {
		if(file.exists()){
			file.delete();
		}
		
		rm = new FileResourceManager(file, PageSize.DEFAULT_PAGE_SIZE);
		rm.open();
		page = new RawPage(ByteBuffer.allocate(PageSize.DEFAULT_PAGE_SIZE));
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
	}
	
	@Test
	public void firstPageShouldBeWrittenAfterOpen(){
		rm.close();
		assertEquals(PageSize.DEFAULT_PAGE_SIZE, file.length());
	}
	
	@Test(expected= ResourceNotOpenException.class)
	public void shouldThrowExceptionIfResourceClosed() throws IOException{
		rm.close();
		rm.addPage(page);
	}

    @Test(expected = WrongResourceManagerException.class)
    public void shouldThrowExceptionOnWriteIfResourceManagerNotSet() throws IOException{
        rm.writePage(page);
    }
	
	@Test(expected= PageNotFoundException.class)
	public void shouldThrowExceptionIfPageToWriteDoesNotExist() throws IOException{
        page = new RawPage(ByteBuffer.allocate(PageSize.DEFAULT_PAGE_SIZE), rm, null);
        rm.writePage(page);
	}
	
	@Test
	public void shouldGenerateDifferentIdsForEachPage() throws IOException{
		assertTrue(rm.addPage(page).id() != rm.addPage(page).id());
	}
	
	@Test
	public void shouldReadWrittenPages() throws IOException{
		RawPage newPage = rm.addPage(page);
		assertEquals(rm.readPage(newPage.id()).buffer(), page.buffer());
	}
	
	@Test
	public void shouldBeAbleToReadPagesAfterReopen() throws IOException{
		RawPage newPage = rm.addPage(new RawPage(ByteBuffer.allocate(PageSize.DEFAULT_PAGE_SIZE)));
		RawPage newPage2 = rm.addPage(page);
		
		rm.close();
		
		// throw away all local variables
		rm = new FileResourceManager(file, PageSize.DEFAULT_PAGE_SIZE);
		rm.open();
		
		assertEquals(rm.readPage(newPage2.id()).buffer(), page.buffer());
	}
	
	@Test(expected= WrongPageSizeException.class)
	public void shouldThrowExceptionIfWrongPageSize() throws IOException{
		page = new RawPage(ByteBuffer.allocate(PageSize.DEFAULT_PAGE_SIZE + 1), null, null);
        rm.addPage(page);
	}
}
