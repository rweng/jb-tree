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

import com.google.inject.Guice;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNull;

public class CachedResourceManagerTest {
	
	@BeforeMethod
	public void setUp(){
		// rm = new ResourceManagerBuilder().useCache();
	}
}
