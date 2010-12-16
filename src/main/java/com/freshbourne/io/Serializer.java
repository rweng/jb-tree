/**
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 * 
 * (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */
package com.freshbourne.io;

/**
 * Classes implementing this interface can encode and decoded the InputType to and from the ResultType
 * 
 * @author "Robin Wenglewski <robin@wenglewski.de>"
 *
 */
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
