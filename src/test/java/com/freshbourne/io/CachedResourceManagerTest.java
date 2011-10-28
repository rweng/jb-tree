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

import com.google.common.cache.Cache;
import com.google.common.collect.MapMaker;
import com.google.common.io.Files;
import com.google.inject.Provider;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentMap;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;


public class CachedResourceManagerTest {

	private ResourceManager rm;
	private static final Logger LOG = Logger.getLogger(CachedResourceManagerTest.class);
	private static final File file = new File("/tmp/CachedResourceManagerTest");

	@BeforeMethod
	public void setUp() throws IOException {
		file.delete();
		rm = new ResourceManagerBuilder().file(file).useCache(true).open().build();
		LOG.info("setup");
	}

	@Test
	public void open() throws IOException {
		rm.close();
		file.delete();
		rm = new ResourceManagerBuilder().file(file).useCache(true).build();
		assertThat(rm.isOpen()).isFalse();
		rm.open();
		assertThat(rm.isOpen()).isTrue();
	}

	@Factory
	public Object[] resourceManagerInterface() throws IOException {
		setUp();
		return new Object[]{new ResourceManagerTest(rm)};
	}

	@Test
	public void cache() throws IOException {
		assertThat(rm.isOpen()).isTrue();
		int count = 10;
		RawPage[] pages = new RawPage[count];

		for (int i = 0; i < count; i++) {
			pages[i] = rm.createPage();
		}

		CachedResourceManager crm = (CachedResourceManager) rm;
		Cache<Integer, RawPage> cache = crm.getCache();

		Random random = new Random();
		for (int i = 0; i < count * 10; i++) {
			rm.getPage(pages[random.nextInt(10)].id());
		}

		assertThat(cache.stats().hitCount()).isEqualTo(count * 10);
	}


	@Test(dependsOnMethods = "open")
	public void testAutoSave() throws IOException {
		CachedResourceManager crm = (CachedResourceManager) rm;
		RawPage p = rm.createPage();
		int testInt = 5343;
		p.bufferForWriting(0).putInt(testInt);

		for (int i = 0; i < crm.getCacheSize() * 2; i++) {
			rm.createPage();
		}

		assertThat(crm.getCache().asMap().containsKey(p)).isFalse();
		assertThat(rm.getPage(p.id()).bufferForReading(0).getInt()).isEqualTo(testInt);
	}

	@Test
	public void closeShouldInvalidateCache() throws IOException {
		RawPage page = rm.createPage();
		rm.close();
		rm.open();
		assertThat(rm.getPage(page.id())).isNotSameAs(page);
	}
}
