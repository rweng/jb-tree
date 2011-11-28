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

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class StringCutSerializerTest {
	private static final StringCutSerializer serializer;
	private static final String testStr = "test123";

	static {
		serializer = StringCutSerializer.get(10);
	}

	@Test
	public void shouldBeSingleton() {
		assertThat( StringCutSerializer.get(10)).isSameAs(serializer);
		assertThat( StringCutSerializer.get(11)).isNotSameAs(serializer);
		
	}

	@Test
	public void serializeAcceptableStringsShouldWork() {
		assertThat( serializer.deserialize(serializer.serialize(testStr))).isEqualTo(testStr);
	}

	@Test
	public void fillCompletely() {
		final String filled = "12345678";
		assertThat( serializer.deserialize(serializer.serialize(filled))).isEqualTo(filled);
	}

	@Test
	public void tooLongIsCut() {
		final String tooLong = "123456789000";
		assertThat( serializer.deserialize(serializer.serialize(tooLong))).isEqualTo("12345678");
	}

	@Test
	public void emptyString() {
		final String empty = "";
		assertThat( serializer.deserialize(serializer.serialize(empty))).isEqualTo(empty);
	}

}
