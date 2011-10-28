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

import com.google.inject.Provider;
import org.apache.log4j.Logger;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Random;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

public class ResourceManagerTest {
	private ResourceManager rm;
	private static Logger LOG = Logger.getLogger(ResourceManagerTest.class);
	private Provider<ResourceManager> provider;

	ResourceManagerTest(Provider<ResourceManager> provider){
		checkNotNull(provider);
		this.provider = provider;
	}

	@BeforeMethod
	public void setUp() throws IOException {
		rm = provider.get();
		if(!rm.isOpen())
			rm.open();
	}

	@Test
	public void shouldBeEmptyAtFirst() throws IOException {
		assertThat(rm.numberOfPages()).isZero();
		assertThat(rm.getPageSize()).isEqualTo(PageSize.DEFAULT_PAGE_SIZE);
	}


	@Test(groups = "bla")
	public void performance(){
		LOG.info(rm);
		int count = 10000;
		RawPage[] pages = new RawPage[count];

		Random rand = new Random();

		long createStart = System.currentTimeMillis();
		// create pages
		for(int i = 0;i<count;i++){
			pages[i] = rm.createPage();
		}
		long createEnd = System.currentTimeMillis();
		LOG.info("Time for creating "+count+" pages (in ms): " + (createEnd - createStart));

		// randomly write to pages
		long writeStart = System.currentTimeMillis();
		for(int i=0; i < count; i++){
			int page = rand.nextInt(count);
			pages[page].bufferForWriting(0).putInt(i);
			rm.writePage(pages[page]);
		}
		long writeEnd = System.currentTimeMillis();
		LOG.info("Time for writing randomly "+count+" pages (in ms): " + (writeEnd - writeStart));
	}

}