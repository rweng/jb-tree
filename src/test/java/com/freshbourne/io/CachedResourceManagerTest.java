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

import com.google.common.io.Files;
import com.google.inject.Provider;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

public class CachedResourceManagerTest {

	private ResourceManager rm;

	@BeforeMethod
	public void setUp() throws IOException {
		File file = new File("/tmp/CachedResourceManagerTest");
		file.delete();
		rm = new ResourceManagerBuilder().file(file).useCache(true).build();
	}

	@Test
	public void emtpy() {
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
}
