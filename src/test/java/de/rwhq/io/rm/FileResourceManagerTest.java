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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

public class FileResourceManagerTest {

	private final static String filePath = "/tmp/frm_test";
	private final static File   file     = new File(filePath);


	static FileResourceManager createNewOpenResourceManager() {
		if (file.exists()) {
			file.delete();
		}

		return createOpenResourceManager();
	}

	static FileResourceManager createOpenResourceManager() {
		FileResourceManager rm = (FileResourceManager) new ResourceManagerBuilder().file(file).useCache(false).build();

		try {
			rm.open();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return rm;
	}
	private FileResourceManager rm;

	@Before
	public void setUp() throws IOException {
		rm = createNewOpenResourceManager();
	}

	@After
	public void tearDown() throws IOException {
		rm.close();
	}

	@Test
	public void shouldWriteOutHeaderCorrectly() throws IOException {
		rm = createNewOpenResourceManager();
		rm.createPage();
		rm.close();

		final RandomAccessFile rFile = new RandomAccessFile(file, "rw");
		rFile.seek(ResourceHeader.Header.PAGE_SIZE.offset);
		assertThat(rFile.readInt()).isEqualTo(PageSize.DEFAULT_PAGE_SIZE);
		rFile.close();
	}

	@Test(expected = IOException.class)
	public void shouldThrowExceptionIfFileIsLocked() throws IOException {
		rm = (FileResourceManager) new ResourceManagerBuilder().file(file).useCache(false).build();
		rm.open();
		final FileResourceManager rm2 =
				(FileResourceManager) new ResourceManagerBuilder().file(file).useCache(false).build();
		rm2.open();
		fail("FileResourceManager should throw an IOException if the file is already locked");
	}

// ******** TESTS **********

	@Test(expected = IllegalStateException.class)
	public void shouldThrowExceptionIfResourceClosed() throws IOException {
		rm.close();
		rm.createPage();
	}

	@Test(expected = PageNotFoundException.class)
	public void shouldThrowExceptionIfPageToWriteDoesNotExist() throws IOException {
		final RawPage page = new RawPage(ByteBuffer.allocate(PageSize.DEFAULT_PAGE_SIZE), 3423);
		rm.writePage(page);
	}

	@Test
	public void shouldGenerateDifferentIdsForEachPage() throws IOException {
		assertThat(rm.createPage().id() != rm.createPage().id()).isTrue();
	}

	@Test
	public void shouldReadWrittenPages() throws IOException {
		final RawPage page = rm.createPage();
		page.bufferForWriting(0).putInt(1234);
		rm.writePage(page);

		assertThat(page.bufferForWriting(0)).isEqualTo(rm.getPage(page.id()).bufferForWriting(0));
	}

	@Test
	public void addingAPageShouldIncreaseNumberOfPages() throws IOException {
		final int num = rm.numberOfPages();
		rm.createPage();
		assertThat(rm.numberOfPages()).isEqualTo(num + 1);
	}

	@Test
	public void shouldBeAbleToReadPagesAfterReopen() throws IOException {
		assertThat(rm.numberOfPages()).isEqualTo(0);
		final RawPage page = rm.createPage();
		assertThat(rm.numberOfPages()).isEqualTo(1);
		rm.createPage();
		assertThat(rm.numberOfPages()).isEqualTo(2);

		final long longToCompare = 12345L;
		final ByteBuffer buf = page.bufferForWriting(0);
		buf.putLong(longToCompare);
		rm.writePage(page);

		assertThat(rm.numberOfPages()).isEqualTo(2);
		assertThat(rm.getPage(page.id()).bufferForWriting(0).getLong()).isEqualTo(longToCompare);

		rm.close();

		// throw away all local variables
		rm = createOpenResourceManager();

		assertThat(rm.numberOfPages()).isEqualTo(2);
		assertThat(rm.getPage(page.id()).bufferForWriting(0).getLong()).isEqualTo(longToCompare);
	}

	@Test
	public void ensureNoHeapOverflowExeptionIsThrown() throws IOException {
		final int count = 100000;
		for (int i = 0; i < count; i++) {
			rm.createPage();
		}
		rm.close();
		assertThat(rm.getFile().getTotalSpace() > count * PageSize.DEFAULT_PAGE_SIZE).isTrue();
	}

	@Test(expected = IllegalStateException.class)
	public void detectExternalFileDelete() {
		file.delete();
		rm.createPage();
	}

	@Test
	public void clear() {
		final RawPage page1 = rm.createPage();
		page1.bufferForWriting(0).putInt(0);
		page1.sync();
		assertThat(rm.getFile().length()).isEqualTo(2 * rm.getPageSize());

		rm.clear();
		assertThat(rm.getFile().length()).isEqualTo(rm.getPageSize());
	}

	public static class ResourceManagerTestImpl extends ResourceManagerTest {

		@Override
		protected ResourceManager resetResourceManager() {
			return createNewOpenResourceManager();
		}
	}
}
