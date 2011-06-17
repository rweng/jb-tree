/**
 * Copyright (C) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.serializer;

public enum FixedStringSerializer implements FixLengthSerializer<String, byte[]> {
	INSTANCE(100);
	
	private int length;
	
	private FixedStringSerializer(int length) {
		this.length = length;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.serializer.Serializer#serialize(java.lang.Object)
	 */
	@Override
	public byte[] serialize(String o) {
		byte[] result = new byte[length];
		byte[] bytes = o.getBytes();
		result[0] = new Integer(bytes.length).byteValue();
		System.arraycopy(bytes, 0, result, 1, bytes.length);
		return result;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.serializer.Serializer#deserialize(java.lang.Object)
	 */
	@Override
	public String deserialize(byte[] o) {
		int length = o[0];
		byte[] bytes = new byte[length];
		System.arraycopy(o, 1, bytes, 0, bytes.length);
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
