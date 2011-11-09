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

import com.google.common.collect.MapMaker;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentMap;

public class StringCutSerializer implements FixLengthSerializer<String, byte[]> {
	private static ConcurrentMap<Integer, StringCutSerializer> cache = null;

	public static StringCutSerializer get(final Integer size) {
		if (cache == null) {
			cache = new MapMaker().weakValues().makeMap();
		} else if (cache.containsKey(size)) {
			return cache.get(size);
		}

		final StringCutSerializer s = new StringCutSerializer(size);
		cache.put(size, s);
		return s;
	}

	private int size;

	private StringCutSerializer(final int size) {
		this.size = size;
	}

	@Override public int getSerializedLength() {
		return size;
	}

	@Override public byte[] serialize(final String o) {
		final ByteBuffer buf = ByteBuffer.allocate(size);
		buf.putShort((short) 0);

		final byte[] bytes = o.getBytes();
		final short toWrite = (short) (bytes.length > buf.remaining() ? buf.remaining() : bytes.length);

		buf.put(bytes, 0, toWrite);
		buf.position(0);
		buf.putShort(toWrite);

		return buf.array();
	}

	@Override public String deserialize(final byte[] o) {
		final ByteBuffer buf = ByteBuffer.wrap(o);
		final short length = buf.getShort();
		final byte[] bytes = new byte[length];
		buf.get(bytes);
		return new String(bytes);
	}
}
