package com.freshbourne.serializer;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class StringCutSerializerTest {
	private static final StringCutSerializer serializer;
	private static final String testStr = "test123";

	static {
		serializer = StringCutSerializer.get(10);
	}

	@Before
	public void setUp(){
	}

	@Test
	public void shouldBeSingleton(){
		assertSame(serializer, StringCutSerializer.get(10));
		assertNotSame(serializer, StringCutSerializer.get(11));
	}

	@Test
	public void serializeAcceptableStringsShouldWork(){
		assertEquals(testStr, serializer.deserialize(serializer.serialize(testStr)));
	}

	@Test
	public void fillCompletely(){
		String filled = "12345678";
		assertEquals(filled, serializer.deserialize(serializer.serialize(filled)));
	}

	@Test
	public void tooLongIsCut(){
		String tooLong = "123456789000";
		assertEquals("12345678", serializer.deserialize(serializer.serialize(tooLong)));
	}

	@Test
	public void emptyString(){
		String empty = "";
		assertEquals(empty, serializer.deserialize(serializer.serialize(empty)));
	}

}
