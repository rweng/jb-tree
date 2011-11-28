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

public class FixedStringSerializerTest {
	@Test
	public void result(){
		final String s = "bla";
		final byte[] bytes = FixedStringSerializer.INSTANCE_1000.serialize(s);
		assertThat( bytes.length).isEqualTo(FixedStringSerializer.INSTANCE_1000.getSerializedLength());
		assertThat( FixedStringSerializer.INSTANCE_1000.deserialize(bytes)).isEqualTo(s);
	}
}
