/**
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 * 
 * (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */
package com.freshbourne.io;

import java.io.File;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class BufferPoolManagerTest {
	private ResourceManager rm;
	private final File file;
	private final Injector injector;
	
	public BufferPoolManagerTest(){
		super();
		file = new File("/tmp/frm_test");
		injector = Guice.createInjector(new FileResourceManagerModule(file));
	}
	
	public void setUp(){
		
	}
	
	
}
