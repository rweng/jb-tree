/**
 * Copyright (C) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.serializer;

import org.junit.*;
import static org.junit.Assert.*;


public class FixedStringSerializerTest {
	@Test
	public void result(){
		String s = "bla";
		byte[] bytes = FixedStringSerializer.INSTANCE.serialize(s);
		assertEquals(FixedStringSerializer.INSTANCE.getSerializedLength(), bytes.length);
		assertEquals(s, FixedStringSerializer.INSTANCE.deserialize(bytes));
	}
}
