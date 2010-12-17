/**
 * Copyright (C) 2010 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.io;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.freshbourne.multimap.btree.BTreeModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.sun.tools.corba.se.idl.constExpr.GreaterThan;
import com.sun.tools.corba.se.idl.constExpr.LessThan;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;


public class DynamicDataPageSpec {
	
	private DynamicDataPage<String> page;
	
	@Before
	public void setUp(){
		page = new DynamicDataPage<String>(
				new byte[1000], 
				new PagePointSerializer(), 
				new StringSerializer());
	}
	
	@Test
	public void shouldHaveToInitialize(){
		assertFalse(page.valid());
		page.initialize();
		assertTrue(page.valid());
	}
	
	@Test(expected= Exception.class)
	public void shouldThrowAnExceptionIfInvalidEntryId() throws Exception{
		page.get(432);
	}
	
	@Test
	public void shouldGetSmallerWhenInsertingSomething() throws NoSpaceException{
		int rest = page.remaining();
		page.add("bla");
		//assertThat(is(rest), gre(page.remaining()));
	}
	
}
