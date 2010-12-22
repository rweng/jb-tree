/**
 * Copyright (C) 2010 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.multimap.btree;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import com.freshbourne.io.DynamicDataPage;
import com.freshbourne.io.FileResourceManagerModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

import static org.junit.Assert.*;

public class LeafPageSpec {
	
	LeafPage<Integer, String> leaf;
	DynamicDataPage<String> stringData;
	DynamicDataPage<Integer> intData;
	
	Injector injector;
	
	@Before
	public void setUp(){
		this.injector = Guice.createInjector(
				new FileResourceManagerModule(new File("/tmp/leafnodespec")),
				new BTreeModule()
				);
		
		stringData = injector.getInstance(DynamicDataPage.class);
		
	}
	
	
	@Test
	public void setUpShouldHaveWorked(){
		assertNotNull(leaf);
	}

}
