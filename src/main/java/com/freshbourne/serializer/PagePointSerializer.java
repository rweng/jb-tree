/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */
package com.freshbourne.serializer;

import com.freshbourne.io.PagePointer;

import java.nio.ByteBuffer;

public enum PagePointSerializer implements FixLengthSerializer<PagePointer, byte[]> {
	INSTANCE;
	
	/* (non-Javadoc)
	 * @see com.freshbourne.serializer.Serializer#serialize(java.lang.Object)
	 */
	@Override
	public byte[] serialize(PagePointer o) {
		ByteBuffer b = ByteBuffer.allocate(getSerializedLength());
		b.putInt(o.getId());
		b.putInt(o.getOffset());
		return b.array();
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.serializer.Serializer#deserialize(java.lang.Object)
	 */
	@Override
	public PagePointer deserialize(byte[] o) {
		ByteBuffer b = ByteBuffer.wrap(o);
		Integer id = b.getInt();
		Integer offset = b.getInt();
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
