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

import com.freshbourne.io.rm.FileResourceManager;
import com.freshbourne.io.rm.ResourceManager;
import com.freshbourne.io.rm.ResourceManagerBuilder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class ResourceManagerBuilderTest {
	private ResourceManagerBuilder builder;

	@BeforeMethod
	public void setUp(){
		builder = new ResourceManagerBuilder();
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void buildShouldRequireAFile(){
		builder.build();
	}

	@Test
	public void buildShouldOnlyRequireAFile(){
		final ResourceManager rm = setFile().build();
		assertNotNull(rm);
	}

	@Test
	public void withoutCache(){
		assertTrue(setFile().useCache(false).build() instanceof FileResourceManager);
	}

	private ResourceManagerBuilder setFile(){
		return builder.file(new File("/tmp/ResourceManagerBuilderTest"));
	}
}
