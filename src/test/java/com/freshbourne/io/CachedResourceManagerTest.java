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
import com.google.common.io.Files;
import com.google.inject.Provider;
import org.apache.log4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;


public class CachedResourceManagerTest {

	private ResourceManager rm;
	private static final Logger LOG = Logger.getLogger(CachedResourceManagerTest.class);

	@BeforeMethod
	public void setUp() throws IOException {
		File file = new File("/tmp/CachedResourceManagerTest");
		file.delete();
		rm = new ResourceManagerBuilder().file(file).useCache(true).build();
		LOG.info("setup");
	}

	@Test
	public void open() throws IOException {
		rm.open();
		assertThat(rm.isOpen()).isTrue();
	}

	@Factory
	public ResourceManagerTest[] resourceManagerInterface() {
		ResourceManagerTest[] test = {new ResourceManagerTest(new Provider<ResourceManager>() {
			@Override public ResourceManager get() {
				try {
					setUp();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				return rm;
			}
		})};

		return test;
	}


	@Test(dependsOnMethods = "open")
	public void cache() throws IOException {
		open();
		
		assertThat(rm.isOpen()).isTrue();
		int count = 10;
		RawPage[] pages = new RawPage[count];
		
		for(int i = 0;i<count;i++){
			pages[i] = rm.createPage();
		}

		CachedResourceManager crm = (CachedResourceManager) rm;
		Cache<Integer,RawPage> cache = crm.getCache();

		Random random = new Random();
		for(int i = 0;i<count*10;i++){
			rm.getPage(pages[random.nextInt(10)].id());
		}

		// createPage does unfortunately not add to the cache, so the first time misses in the loop above
		assertThat(cache.stats().hitCount()).isEqualTo(count * 9);
	}
}
