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

public interface Serializer<InputType, ResultType> {
	
	/**
	 * serializes the object of type InputType
	 * 
	 * @param o input object
	 * @return input object serialized as ResultType
	 */
	public ResultType serialize(InputType o);
	
	/**
	 * deserializes the object of type ResultType
	 * 
	 * @param o serialized object
	 * @return deserialized object
	 */
	public InputType deserialize(ResultType o);

}
