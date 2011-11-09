/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 *
 * http://creativecommons.org/licenses/by-nc/3.0/
 *
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2011 "Robin Wenglewski <robin@wenglewski.de>"
 */

package de.rwhq.serializer;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class StringCutSerializerTest {
	private static final StringCutSerializer serializer;
	private static final String testStr = "test123";

	static {
		serializer = StringCutSerializer.get(10);
	}

	@Test
	public void shouldBeSingleton() {
		assertSame(serializer, StringCutSerializer.get(10));
		assertNotSame(serializer, StringCutSerializer.get(11));
	}

	@Test
	public void serializeAcceptableStringsShouldWork() {
		assertEquals(testStr, serializer.deserialize(serializer.serialize(testStr)));
	}

	@Test
	public void fillCompletely() {
		final String filled = "12345678";
		assertEquals(filled, serializer.deserialize(serializer.serialize(filled)));
	}

	@Test
	public void tooLongIsCut() {
		final String tooLong = "123456789000";
		assertEquals("12345678", serializer.deserialize(serializer.serialize(tooLong)));
	}

	@Test
	public void emptyString() {
		final String empty = "";
		assertEquals(empty, serializer.deserialize(serializer.serialize(empty)));
	}

}
