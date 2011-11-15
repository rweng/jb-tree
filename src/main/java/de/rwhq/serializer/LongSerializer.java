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

import java.nio.ByteBuffer;

public enum LongSerializer implements FixLengthSerializer<Long, byte[]> {
	INSTANCE;

	@Override
	public byte[] serialize(final Long o) {
		final ByteBuffer buffer = ByteBuffer.allocate(Long.SIZE / 8);
		buffer.position(0);
		buffer.putLong(o);
		return buffer.array();
	}

	@Override
	public Long deserialize(final byte[] o) {
		return ByteBuffer.wrap(o).getLong();
	}

	@Override
	public int getSerializedLength() {
		return Long.SIZE / 8;
	}
}
