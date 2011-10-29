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

import org.apache.log4j.Logger;

import java.nio.ByteBuffer;

public enum FixedStringSerializer implements FixLengthSerializer<String, byte[]> {
	INSTANCE(100),
	INSTANCE_10(10),
	INSTANCE_100(100),
	INSTANCE_1000(1000);
	
	private int length;

	private static Logger LOG = Logger.getLogger(FixedStringSerializer.class);
	
	
	private FixedStringSerializer(final int length) {
		this.length = length;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.serializer.Serializer#serialize(java.lang.Object)
	 */
	@Override
	public byte[] serialize(final String o) {
		final byte[] bytes = o.getBytes();
		
		if(bytes.length > (length - 1)){
			throw new IllegalArgumentException("String is too long to be serialized");
		}


		final ByteBuffer buf = ByteBuffer.allocate(length);
		buf.putShort((short) bytes.length);
		buf.put(bytes);
		return buf.array();
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.serializer.Serializer#deserialize(java.lang.Object)
	 */
	@Override
	public String deserialize(final byte[] o) {
		final ByteBuffer buf = ByteBuffer.wrap(o);
		final short length = buf.getShort();

		final byte[] bytes = new byte[length];
		buf.get(bytes);
		return new String(bytes);
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.serializer.FixLengthSerializer#getSerializedLength()
	 */
	@Override
	public int getSerializedLength() {
		return length;
	}

}
