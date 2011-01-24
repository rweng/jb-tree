/*
 * Copyright (c) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 */
package com.freshbourne.serializer;

import com.freshbourne.io.PagePointer;

import java.nio.ByteBuffer;

/**
 * serializes a PagePoint to <code>byte[8]</code>
 * 
 * @author Robin Wenglewski <robin@wenglewski.de>
 *
 */
public enum PagePointSerializer implements FixLengthSerializer<PagePointer, byte[]> {
	INSTANCE;
	
	/* (non-Javadoc)
	 * @see com.freshbourne.serializer.Serializer#serialize(java.lang.Object)
	 */
	@Override
	public byte[] serialize(PagePointer o) {
		ByteBuffer b = ByteBuffer.allocate(12);
		b.putLong(o.getId());
		b.putInt(o.getOffset());
		return b.array();
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.serializer.Serializer#deserialize(java.lang.Object)
	 */
	@Override
	public PagePointer deserialize(byte[] o) {
		ByteBuffer b = ByteBuffer.wrap(o);
		long id = b.getLong();
		int offset = b.getInt();
		return new PagePointer(id, offset);
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.serializer.FixLengthSerializer#serializedLength(java.lang.Class)
	 */
	@Override
	public int serializedLength(Class<PagePointer> c) {
		return 12;
	}

}
