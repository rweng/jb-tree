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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class ResourceManagerTest {
	private ResourceManager rm;

	ResourceManagerTest(ResourceManager rm){
		checkNotNull(rm);
		this.rm = rm;
	}

	@BeforeMethod
	public void setUp() throws IOException {
		if(!rm.isOpen())
			rm.open();
	}

	@Test
	public void shouldBeEmptyAtFirst() throws IOException {
		assertTrue(rm != null);
		assertEquals(PageSize.DEFAULT_PAGE_SIZE, rm.getPageSize());
		assertEquals(0, rm.numberOfPages()); // 0 pages
	}

}