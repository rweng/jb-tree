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
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import static org.fest.assertions.Assertions.assertThat;
import static org.testng.Assert.*;

public class FileResourceManagerTest {

	private final static String filePath = "/tmp/frm_test";
	private final        File   file     = new File(filePath);
	private FileResourceManager rm;
	private static final Log LOG = LogFactory.getLog(FileResourceManagerTest.class);


	@BeforeMethod(alwaysRun = true)
	public void setUp() throws IOException {
		rm = createNewOpenResourceManager();
	}

	@AfterMethod(alwaysRun = true)
	public void tearDown() throws IOException {
		rm.close();
	}


	protected FileResourceManager createNewOpenResourceManager() {
		if (file.exists()) {
			file.delete();
		}

		return createOpenResourceManager();
	}

	protected FileResourceManager createOpenResourceManager() {
		rm = (FileResourceManager) new ResourceManagerBuilder().file(file).useCache(false).build();

		try {
			rm.open();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return rm;
	}

	@Test
	public void shouldWriteOutHeaderCorrectly() throws IOException {
		rm = createNewOpenResourceManager();
		rm.createPage();
		rm.close();

		final RandomAccessFile rFile = new RandomAccessFile(file, "rw");
		assertEquals(PageSize.DEFAULT_PAGE_SIZE, rFile.readInt());
		assertEquals(1, rFile.readInt());
		rFile.close();
	}

	@Test(expectedExceptions = IOException.class)
	public void shouldThrowExceptionIfFileIsLocked() throws IOException {
		rm = (FileResourceManager) new ResourceManagerBuilder().file(file).useCache(false).build();
		rm.open();
		final FileResourceManager rm2 = (FileResourceManager) new ResourceManagerBuilder().file(file).useCache(false).build();
		rm2.open();
		fail("FileResourceManager should throw an IOException if the file is already locked");
	}


// ******** TESTS **********

	@Test(expectedExceptions = IllegalStateException.class)
	public void shouldThrowExceptionIfResourceClosed() throws IOException {
		rm.close();
		rm.createPage();
	}

	@Test(expectedExceptions = PageNotFoundException.class)
	public void shouldThrowExceptionIfPageToWriteDoesNotExist() throws IOException {
		final RawPage page = new RawPage(ByteBuffer.allocate(PageSize.DEFAULT_PAGE_SIZE), 3423);
		rm.writePage(page);
	}

	@Test
	public void shouldGenerateDifferentIdsForEachPage() throws IOException {
		assertTrue(rm.createPage().id() != rm.createPage().id());
	}

	@Test
	public void shouldReadWrittenPages() throws IOException {
		final RawPage page = rm.createPage();
		page.bufferForWriting(0).putInt(1234);
		rm.writePage(page);

		assertEquals(rm.getPage(page.id()).bufferForWriting(0), page.bufferForWriting(0));
	}

	@Test
	public void addingAPageShouldIncreaseNumberOfPages() throws IOException {
		final int num = rm.numberOfPages();
		rm.createPage();
		assertEquals(num + 1, rm.numberOfPages());
	}

	@Factory
	public Object[] createInstances() {
		return new Object[]{new ResourceManagerTest(new ResourceManagerBuilder().file("/tmp/ResourceManagerTest").open().useCache(false).build())};
	}


	@Test
	public void shouldBeAbleToReadPagesAfterReopen() throws IOException {
		assertEquals(0, rm.numberOfPages());
		final RawPage page = rm.createPage();
		assertEquals(1, rm.numberOfPages());
		rm.createPage();
		assertEquals(2, rm.numberOfPages());

		final long longToCompare = 12345L;
		final ByteBuffer buf = page.bufferForWriting(0);
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

	@Test(enabled = false)
	public void shouldBeAbleToRemovePages() throws Exception {
		final RawPage p1 = rm.createPage();
		final int i = rm.numberOfPages();
		final Integer p1Id = p1.id();

		rm.removePage(p1Id);
		assertEquals(i - 1, rm.numberOfPages());
		try {
			rm.getPage(p1Id);
			fail("reading a non-existent page should throw an exeption");
		} catch (Exception expected) {
		}

		final RawPage p3 = rm.createPage();
		assertEquals(i, rm.numberOfPages());
		rm.removePage(p3.id());
		assertEquals(i - 1, rm.numberOfPages());
	}

	@Test(groups = "slow")
	public void ensureNoHeapOverflowExeptionIsThrown() throws IOException {
		final int count = 100000;
		for (int i = 0; i < count; i++) {
			rm.createPage();
		}
		rm.close();
		assertTrue(rm.getFile().getTotalSpace() > count * PageSize.DEFAULT_PAGE_SIZE);
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void detectExternalFileDelete() {
		file.delete();
		rm.createPage();
	}

	@Test
	public void clear(){
		final RawPage page1 = rm.createPage();
		page1.bufferForWriting(0).putInt(0);
		page1.sync();
		assertThat(rm.getFile().length()).isEqualTo(2 * rm.getPageSize());

		rm.clear();
		assertThat(rm.getFile().length()).isEqualTo(rm.getPageSize());
	}
}
