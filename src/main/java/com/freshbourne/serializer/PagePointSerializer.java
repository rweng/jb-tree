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

import com.freshbourne.io.rm.PagePointer;

import java.nio.ByteBuffer;

public enum PagePointSerializer implements FixLengthSerializer<PagePointer, byte[]> {
	INSTANCE;
	
	/* (non-Javadoc)
	 * @see com.freshbourne.serializer.Serializer#serialize(java.lang.Object)
	 */
	@Override
	public byte[] serialize(final PagePointer o) {
		final ByteBuffer b = ByteBuffer.allocate(getSerializedLength());
		b.putInt(o.getId());
		b.putInt(o.getOffset());
		return b.array();
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.serializer.Serializer#deserialize(java.lang.Object)
	 */
	@Override
	public PagePointer deserialize(final byte[] o) {
		final ByteBuffer b = ByteBuffer.wrap(o);
		final Integer id = b.getInt();
		final Integer offset = b.getInt();
		return new PagePointer(id, offset);
	}
	
	/* (non-Javadoc)
	 * @see com.freshbourne.serializer.FixLengthSerializer#getSerializedLength()
	 */
	@Override
	public int getSerializedLength() {
		// TODO Auto-generated method stub
		return 8;
	}

}
