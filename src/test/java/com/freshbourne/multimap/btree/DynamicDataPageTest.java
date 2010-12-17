/**
 * Copyright (C) 2010 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.multimap.btree;

import java.nio.ByteBuffer;

import com.freshbourne.io.DataPage;
import com.freshbourne.io.DynamicDataPage;
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
		page = null;// new DynamicDataPage(bytes, serializer);
	}
	
	public void testCreation() throws Exception{
		assertFalse(page.valid());
		assertEquals(1000, page.buffer().capacity());
		int ex =(1000 - 4 - serializer.serializedLength(PagePointer.class));
		assertEquals(ex, page.body().capacity());
		assertEquals(ex, page.remaining());
		assertEquals(0, page.bodyUsed().capacity());
		
		page.initialize();
		assertTrue(page.valid());
		assertEquals(1000, page.buffer().capacity());
		assertEquals(ex, page.body().capacity());
		
		Integer f = new Integer(55);
		byte[] fbytes = new byte[4];
		ByteBuffer.wrap(fbytes).putInt(f);
		int fbytesId = page.add(fbytes);
		
		final String s = "blast";
		int sId = page.add(s.getBytes());
		
		final String s2 = "blastsdfds";
		int s2Id = page.add(s2.getBytes());
		
		//assertEquals(fbytes.length, page.get(fbytesId).length );
		//String s2result = new String(page.get(s2Id));
		//assertEquals(s2, s2result  );
		//assertEquals(s, new String(page.get(sId)) );
		
//		page.remove(sId);
//		assertEquals(f, (Integer) ByteBuffer.wrap(page.get(fbytesId)).getInt() );
//		assertEquals(s2, new String(page.get(s2Id)) );
//		
//		try{
//			page.get(sId);
//			fail("getting data with a removed id should fail");
//		} catch (Exception expected) {
//		}
//		
//		try{
//
//			page.get(34);
//			fail("getting data with a non-existent id should fail");
//		} catch (Exception expected) {
//		}
//		
//		page = null;//new DynamicDataPage(bytes, serializer);
//		assertTrue(page.valid());
//		assertEquals(f, (Integer)ByteBuffer.wrap(page.get(fbytesId)).getInt() );
//		assertEquals(s2, new String(page.get(s2Id)) );
//		
//		try{
//			page.get(sId);
//			fail("getting data with a removed id should fail");
//		} catch (Exception expected) {
//		}		
	}
	
}
