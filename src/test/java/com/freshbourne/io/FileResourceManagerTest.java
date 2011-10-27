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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.testng.Assert.*;

public class FileResourceManagerTest {
	
	private final static String filePath = "/tmp/frm_test";
	private final File file = new File(filePath);
	private FileResourceManager rm;
	private RawPage page;
	private static final Log LOG = LogFactory.getLog(FileResourceManagerTest.class);

	
	protected FileResourceManager createNewOpenResourceManager() {
		if(file.exists()){
			file.delete();
		}
		
		return createOpenResourceManager();
	}
	
	protected FileResourceManager createOpenResourceManager(){
		rm = new FileResourceManager(file, PageSize.DEFAULT_PAGE_SIZE, false);
		
		try {
			rm.open();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		return rm;
	}
	
	@Test
	public void shouldWriteOutHeaderCorrectly() throws IOException{
		rm = (FileResourceManager) createNewOpenResourceManager();
		rm.createPage();
		rm.close();
		
		RandomAccessFile rFile = new RandomAccessFile(file, "rw");
		assertEquals(PageSize.DEFAULT_PAGE_SIZE, rFile.readInt());
		assertEquals(1, rFile.readInt());
		rFile.close();
	}

	@Test(enabled = false)
	public void testSync() {
		rm = (FileResourceManager) createNewOpenResourceManager();
		RawPage p = rm.createPage();
		int testInt = 5343;
		p.bufferForWriting(0).putInt(testInt);

		RandomAccessFile handle = rm.getHandle();
		try {
			handle.seek(rm.getPageSize());

			assertFalse(testInt == handle.readInt());
		} catch (IOException ignored) { // ignore, we dont care whether the empty page has been written or not
		}


		rm.sync();

		try {
			handle.seek(rm.getPageSize());
			assertEquals(testInt, handle.readInt());
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
			fail();
		}
	}

	@Test(expectedExceptions = IOException.class)
	public void shouldThrowExceptionIfFileIsLocked() throws IOException {
		rm = new FileResourceManager(file, PageSize.DEFAULT_PAGE_SIZE, true);
		rm.open();
		FileResourceManager rm2 = new FileResourceManager(file, PageSize.DEFAULT_PAGE_SIZE, true);
		rm2.open();
		fail("FileResourceManager should throw an IOException if the file is already locked");
	}


	@BeforeMethod(alwaysRun = true)
	public void setUp() throws IOException {
		rm = createNewOpenResourceManager();
	}

	@AfterMethod(alwaysRun = true)
	public void tearDown() throws IOException{
		rm.close();
	}



// ******** TESTS **********

	@Test
	public void shouldBeEmptyAtFirst() throws IOException{
		assertTrue(rm != null);
		assertEquals(PageSize.DEFAULT_PAGE_SIZE, rm.getPageSize());
		assertEquals(0, rm.numberOfPages()); // 0 pages
	}


	@Test(expectedExceptions = IllegalStateException.class)
	public void shouldThrowExceptionIfResourceClosed() throws IOException{
		rm.close();
		rm.createPage();
	}

	@Test(expectedExceptions = PageNotFoundException.class)
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

	@Test(expectedExceptions = WrongPageSizeException.class)
	public void shouldThrowExceptionIfWrongPageSize() throws IOException{
		page = new RawPage(ByteBuffer.allocate(PageSize.DEFAULT_PAGE_SIZE + 1), 1);
        rm.addPage(page);
	}

	@Test(enabled = false)
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

	@Test(groups = "slow")
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

	@Test(groups = "slow")
	public void ensureNoHeapOverflowExeptionIsThrown() throws IOException {
		int count = 100000;
		for(int i = 0;i<count;i++){
				rm.createPage();
		}
		rm.close();
		assertTrue(rm.getFile().getTotalSpace() > count * PageSize.DEFAULT_PAGE_SIZE);
	}
}
