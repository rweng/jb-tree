package com.freshbourne.serializer;

import org.testng.annotations.BeforeMethod;

import static org.testng.Assert.*;

public class StringCutSerializerTest {
	private static final StringCutSerializer serializer;
	private static final String testStr = "test123";

	static {
		serializer = StringCutSerializer.get(10);
	}

	@org.testng.annotations.Test
	public void shouldBeSingleton() {
		assertSame(serializer, StringCutSerializer.get(10));
		assertNotSame(serializer, StringCutSerializer.get(11));
	}

	@org.testng.annotations.Test
	public void serializeAcceptableStringsShouldWork() {
		assertEquals(testStr, serializer.deserialize(serializer.serialize(testStr)));
	}

	@org.testng.annotations.Test
	public void fillCompletely() {
		final String filled = "12345678";
		assertEquals(filled, serializer.deserialize(serializer.serialize(filled)));
	}

	@org.testng.annotations.Test
	public void tooLongIsCut() {
		final String tooLong = "123456789000";
		assertEquals("12345678", serializer.deserialize(serializer.serialize(tooLong)));
	}

	@org.testng.annotations.Test
	public void emptyString() {
		final String empty = "";
		assertEquals(empty, serializer.deserialize(serializer.serialize(empty)));
	}

}
