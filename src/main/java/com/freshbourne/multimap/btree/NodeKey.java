/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */
package com.freshbourne.multimap.btree;

public class NodeKey<T> {
	
	public short getSerializedSize(){
		return 1;
	}
	
	public byte[] getBytes(){
		return null;
	}
	
	public T getKey(){
		return null;
	}
}
