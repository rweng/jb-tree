/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 *
 * http://creativecommons.org/licenses/by-nc/3.0/
 *
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2011 "Robin Wenglewski <robin@wenglewski.de>"
 */
package com.freshbourne.serializer;

import static org.testng.Assert.*;


public class FixedStringSerializerTest {
	@org.testng.annotations.Test
	public void result(){
		String s = "bla";
		byte[] bytes = FixedStringSerializer.INSTANCE_1000.serialize(s);
		assertEquals(FixedStringSerializer.INSTANCE_1000.getSerializedLength(), bytes.length);
		assertEquals(s, FixedStringSerializer.INSTANCE_1000.deserialize(bytes));
	}
}
