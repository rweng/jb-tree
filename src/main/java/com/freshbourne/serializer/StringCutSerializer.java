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

import com.freshbourne.io.SoftHashMap;

import java.nio.ByteBuffer;

public class StringCutSerializer implements FixLengthSerializer<String, byte[]> {
	private static SoftHashMap<Integer, StringCutSerializer> cache = new SoftHashMap<Integer, StringCutSerializer>();

	public static StringCutSerializer get(Integer size) {
		if (cache.containsKey(size))
			return cache.get(size);

		StringCutSerializer s = new StringCutSerializer(size);
		cache.put(size, s);
		return s;
	}

	private int size;

	private StringCutSerializer(int size) {
		this.size = size;
	}

	@Override public int getSerializedLength() {
		return size;
	}

	@Override public byte[] serialize(String o) {
		ByteBuffer buf = ByteBuffer.allocate(size);
		buf.putShort((short) 0);

		byte[] bytes = o.getBytes();
		short toWrite = (short) (bytes.length > buf.remaining() ? buf.remaining() : bytes.length);

		buf.put(bytes, 0, toWrite);
		buf.position(0);
		buf.putShort(toWrite);

		return buf.array();
	}

	@Override public String deserialize(byte[] o) {
		ByteBuffer buf = ByteBuffer.wrap(o);
		short length = buf.getShort();
		byte[] bytes = new byte[length];
		buf.get(bytes);
		return new String(bytes);
	}
}
