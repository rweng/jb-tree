/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
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
	private ByteBuffer buffer;

	private static Logger LOG = Logger.getLogger(FixedStringSerializer.class);
	
	
	private FixedStringSerializer(int length) {
		this.length = length;
	}

	private ByteBuffer getBuffer(){
		if(buffer == null)
			buffer = ByteBuffer.allocate(length);

		return buffer;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.serializer.Serializer#serialize(java.lang.Object)
	 */
	@Override
	public byte[] serialize(String o) {
		byte[] bytes = o.getBytes();
		
		if(bytes.length > (length - 1)){
			throw new IllegalArgumentException("String is too long to be serialized");
		}


		getBuffer().position(0);
		getBuffer().putShort((short) bytes.length);
		getBuffer().put(bytes);
		return getBuffer().array();
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.serializer.Serializer#deserialize(java.lang.Object)
	 */
	@Override
	public String deserialize(byte[] o) {
		getBuffer().position(0);
		short length = getBuffer().getShort();
		
		LOG.debug("deserialing string with arraysize " + o.length + " and str-length " + length );

		byte[] bytes = new byte[length];
		getBuffer().get(bytes);
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
