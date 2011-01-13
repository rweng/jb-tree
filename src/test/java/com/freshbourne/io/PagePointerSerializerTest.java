/**
 * Copyright (C) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.io;

import com.freshbourne.serializer.FixLengthSerializer;
import com.freshbourne.serializer.PagePointSerializer;
import junit.framework.TestCase;

public class PagePointerSerializerTest extends TestCase {
	
	private PagePointer p1, p2;
	private FixLengthSerializer<PagePointer, byte[]> serializer;
	
	public void setUp(){
		p1 = new PagePointer(5, 10);
		p2 = new PagePointer(50, 100);
		serializer = new PagePointSerializer();
	}
	
	public void testSerializer(){
		byte[] b1 = serializer.serialize(p1);
		byte[] b2 = serializer.serialize(p2);
		
		assertTrue(b1 != b2);
		assertEquals(serializer.serializedLength(PagePointer.class), b1.length);
		assertEquals(serializer.serializedLength(PagePointer.class), b2.length);
		assertEquals(p1, serializer.deserialize(b1));
		assertEquals(p2, serializer.deserialize(b2));
	}
}
