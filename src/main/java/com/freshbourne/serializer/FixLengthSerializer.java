/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */

package com.freshbourne.serializer;

import com.freshbourne.serializer.Serializer;

/**
 * A Serializer that serializes always to the same String/Buffer length
 * 
 * @author "Robin Wenglewski <robin@wenglewski.de>"
 *
 */
public interface FixLengthSerializer<InputType, ResultType> extends Serializer<InputType, ResultType> {
	
	/**
	 * @param c class in question
	 * @return length of the object returned by {@link #serialize(Object)}
	 */
	public int getSerializedLength();
}
