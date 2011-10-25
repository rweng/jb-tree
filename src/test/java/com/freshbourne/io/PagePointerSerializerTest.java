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

import com.freshbourne.serializer.FixLengthSerializer;
import com.freshbourne.serializer.PagePointSerializer;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PagePointerSerializerTest {
	
	private PagePointer p1, p2;
	private FixLengthSerializer<PagePointer, byte[]> serializer;
	
	@BeforeMethod
	public void setUp(){
		p1 = new PagePointer(5, 10);
		p2 = new PagePointer(50, 100);
		serializer = PagePointSerializer.INSTANCE;
	}
	
	@Test public void testSerializer(){
		byte[] b1 = serializer.serialize(p1);
		byte[] b2 = serializer.serialize(p2);

		assert b1 != b2;
		Assert.assertEquals(serializer.getSerializedLength(), b1.length);
		Assert.assertEquals(serializer.getSerializedLength(), b2.length);
		Assert.assertEquals(p1, serializer.deserialize(b1));
		Assert.assertEquals(p2, serializer.deserialize(b2));
	}
}
