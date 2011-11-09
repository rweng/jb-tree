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

public enum StringSerializer implements Serializer<String, byte[]> {
	INSTANCE;
	
	/* (non-Javadoc)
	 * @see com.rwhq.serializer.Serializer#serialize(java.lang.Object)
	 */
	@Override
	public byte[] serialize(final String o) {
		return o.getBytes();
	}

	/* (non-Javadoc)
	 * @see Serializer#deserialize(java.lang.Object)
	 */
	@Override
	public String deserialize(final byte[] o) {
		return new String(o);
	}
	
}
