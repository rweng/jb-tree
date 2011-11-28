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

import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;

public class ResourceManagerBuilderTest {
	private ResourceManagerBuilder builder;

	@Before
	public void setUp(){
		builder = new ResourceManagerBuilder();
	}

	@Test(expected = NullPointerException.class)
	public void buildShouldRequireAFile(){
		builder.build();
	}

	@Test
	public void buildShouldOnlyRequireAFile(){
		final ResourceManager rm = setFile().build();
		assertThat(rm).isNotNull();
	}

	@Test
	public void withoutCache(){
		assertThat(setFile().useCache(false).build() instanceof FileResourceManager).isTrue();
	}

	private ResourceManagerBuilder setFile(){
		return builder.file(new File("/tmp/ResourceManagerBuilderTest"));
	}
}
