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

import java.nio.ByteBuffer;

public enum IntegerSerializer implements FixLengthSerializer<Integer, byte[]> {
	INSTANCE;
	
	/* (non-Javadoc)
	 * @see com.freshbourne.serializer.Serializer#serialize(java.lang.Object)
	 */
	@Override
	public byte[] serialize(final Integer o) {
		return ByteBuffer.allocate(4).putInt(o).array();
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.serializer.Serializer#deserialize(java.lang.Object)
	 */
	@Override
	public Integer deserialize(final byte[] o) {
		return ByteBuffer.wrap(o).getInt();
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.serializer.FixLengthSerializer#getSerializedLength()
	 */
	@Override
	public int getSerializedLength() {
		// TODO Auto-generated method stub
		return 4;
	}

}
