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

import de.rwhq.io.rm.PagePointer;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class PagePointerSerializerTest {
	
	private PagePointer p1, p2;
	private FixLengthSerializer<PagePointer, byte[]> serializer;
	
	@Before
	public void setUp(){
		p1 = new PagePointer(5, 10);
		p2 = new PagePointer(50, 100);
		serializer = PagePointSerializer.INSTANCE;
	}
	
	@Test
	public void testSerializer(){
		final byte[] b1 = serializer.serialize(p1);
		final byte[] b2 = serializer.serialize(p2);

		assert b1 != b2;
		assertThat( b1.length).isEqualTo(serializer.getSerializedLength());
		assertThat( b2.length).isEqualTo(serializer.getSerializedLength());
		assertThat( serializer.deserialize(b1)).isEqualTo(p1);
		assertThat( serializer.deserialize(b2)).isEqualTo(p2);
	}
}
