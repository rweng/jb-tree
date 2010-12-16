/**
 * Copyright (C) 2010 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.multimap.btree;

import com.freshbourne.io.FixLengthSerializer;
import com.freshbourne.io.PagePointSerializer;
import com.freshbourne.io.PagePointer;

import junit.framework.TestCase;

public class DynamicDataPageTest extends TestCase {
	
	private DataPage page;
	private final int size = 1000;
	private final byte[] bytes = new byte[size];
	FixLengthSerializer<PagePointer, byte[]> serializer = new PagePointSerializer();
	
	public void setUp(){
		page = new DynamicDataPage(bytes, serializer);
	}
	
	public void testCreation() throws Exception{
		assertFalse(page.valid());
		assertEquals(1000, page.buffer().length);
		assertEquals(1000 - 4, page.body().length);
		
		page.initialize();
		assertTrue(page.valid());
		assertEquals(1000, page.buffer().length);
		assertEquals(1000 - 8, page.body().length);
		
		Float f = new Float(1.4);
		byte[] fbytes = {f.byteValue()};
		int fbytesId = page.add(fbytes);
		
		final String s = "blast";
		int sId = page.add(s.getBytes());
		
		final String s2 = "blastsdfds";
		int s2Id = page.add(s.getBytes());
		
		assertEquals(fbytes, page.get(fbytesId) );
		assertEquals(s2, page.get(s2Id) );
		assertEquals(s, page.get(sId) );
		
		page.remove(sId);
		
		assertEquals(fbytes, page.get(fbytesId) );
		assertEquals(s2, page.get(s2Id) );
		
		try{
			page.get(s2Id);
			fail("getting data with a removed id should fail");
		} catch (Exception expected) {
		}
		
		try{

			page.get(34);
			fail("getting data with a non-existent id should fail");
		} catch (Exception expected) {
		}
		
		page = new DynamicDataPage(bytes, serializer);
		assertTrue(page.valid());
		assertEquals(fbytes, page.get(fbytesId) );
		assertEquals(s2, page.get(s2Id) );
		
		try{
			page.get(s2Id);
			fail("getting data with a removed id should fail");
		} catch (Exception expected) {
		}		
	}
	
}
