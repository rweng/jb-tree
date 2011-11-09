/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 *
 * http://creativecommons.org/licenses/by-nc/3.0/
 *
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2011 "Robin Wenglewski <robin@wenglewski.de>"
 */

package com.freshbourne.io.rm;

import org.apache.log4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static org.testng.Assert.assertEquals;

public class ResourceManagerTest {
	private ResourceManager rm;
	private static Logger LOG = Logger.getLogger(ResourceManagerTest.class);

	/**
	 * NOTE: ensure that the provided ResourceManager uses a different file than the implementation tests
	 *
	 * @param rm
	 */
	ResourceManagerTest(final ResourceManager rm) throws IOException {
		checkNotNull(rm);
		this.rm = rm;
	}

	@BeforeMethod
	public void setUp() throws IOException {
		if (!rm.isOpen())
			rm.open();

		rm.clear();
	}

	@Test
	public void shouldBeEmptyAtFirst() throws IOException {
		assertThat(rm.numberOfPages()).isZero();
		assertThat(rm.getPageSize()).isEqualTo(PageSize.DEFAULT_PAGE_SIZE);
	}


	@Test(groups = "performance")
	public void performance() {
		LOG.info("ResourceManager: " + rm);
		final int count = 10000;
		final int[] ids = new int[count];

		final Random rand = new Random();

		final long createStart = System.currentTimeMillis();
		// create ids
		for (int i = 0; i < count; i++) {
			ids[i] = rm.createPage().id();
		}
		final long createEnd = System.currentTimeMillis();
		LOG.info("Time for creating " + count + " ids (in ms): " + (createEnd - createStart));

		// randomly write to ids
		final long writeStart = System.currentTimeMillis();
		for (int i = 0; i < count; i++) {
			final int index = rand.nextInt(count);
			RawPage page = rm.getPage(ids[index]);
			page.bufferForWriting(0).putInt(i);
			page.sync();
		}
		final long writeEnd = System.currentTimeMillis();
		LOG.info("Time for reading and writing randomly " + count + " ids (in ms): " + (writeEnd - writeStart));
		LOG.info("ResourceManager: " + rm);
	}

	@Test(groups = "slow")
	public void shouldBeAbleToCreateAMassiveNumberOfPages() {
		final List<Integer> ids = new ArrayList<Integer>();

		final RawPage p1 = rm.createPage();
		p1.bufferForWriting(0).putInt(111);
		p1.sync();

		final int size = 10000;
		for (int i = 0; i < size; i++) {
			ids.add(rm.createPage().id());
		}

		final RawPage p2 = rm.createPage();
		p2.bufferForWriting(0).putInt(222);
		p2.sync();

		assertEquals(111, rm.getPage(p1.id()).bufferForReading(0).getInt());
		assertEquals(222, rm.getPage(p2.id()).bufferForReading(0).getInt());

		assertEquals(size + 2, rm.numberOfPages());
		for (int i = 0; i < size; i++) {
			final Integer id = ids.get(0);
			assertEquals(id, rm.getPage(id).id());
		}
	}

	@Test
	public void toStringShouldAlwaysWork() throws IOException {
		if (!rm.isOpen())
			rm.open();

		assertThat(rm.toString()).isNotNull();
		rm.close();
		assertThat(rm.toString()).isNotNull();
	}

	@Test
	public void openAndClose() throws IOException, InterruptedException {
		assertThat(rm.isOpen()).isTrue();
		rm.close();
		assertThat(rm.isOpen()).isFalse();
		rm.open();
		assertThat(rm.isOpen()).isTrue();
	}

	@Test
	public void shouldBeAbleToRemovePages() throws Exception {
		final RawPage p1 = rm.createPage();
		final int i = rm.numberOfPages();
		final Integer p1Id = p1.id();

		rm.removePage(p1Id);
		assertThat(rm.numberOfPages()).isEqualTo(i - 1);
		try {
			rm.getPage(p1Id);
			fail("reading a non-existent page should throw an exeption");
		} catch (Exception expected) {
		}

		final RawPage p3 = rm.createPage();
		assertThat( rm.numberOfPages()).isEqualTo(i);
		rm.removePage(p3.id());
		assertThat(rm.numberOfPages()).isEqualTo(i - 1);
	}
}