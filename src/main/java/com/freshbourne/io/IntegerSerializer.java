/**
 * Copyright (C) 2010 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.io;

import java.nio.ByteBuffer;

public class IntegerSerializer implements Serializer<Integer, byte[]> {

	/* (non-Javadoc)
	 * @see com.freshbourne.io.Serializer#serialize(java.lang.Object)
	 */
	@Override
	public byte[] serialize(Integer o) {
		return ByteBuffer.allocate(4).putInt(o).array();
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.Serializer#deserialize(java.lang.Object)
	 */
	@Override
	public Integer deserialize(byte[] o) {
		return ByteBuffer.wrap(o).getInt();
	}

}
