/*
 * Copyright (c) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 */
package com.freshbourne.io;

import java.nio.ByteBuffer;


/**
 * Pages implementing this interface use the provided buffer to store data.
 * add, get and remove use byte arrays instead of ByteBuffer since they
 * usually come from serializations which usually create a new byte array.
 * 
 * 
 * @author Robin Wenglewski <robin@wenglewski.de>
 *
 */
public abstract class DataPage<T> extends RawPage {

    public DataPage(ByteBuffer buffer, ResourceManager rm, Integer pageId){
        super(buffer, rm, pageId);
    }

	/**
	 * adds some bytes to the underlying body. It is possible that the header
	 * also grows through this process.
	 * 
	 * @param value array to be written
	 * @return id of the entry/byte[] within this page
	 */
	public abstract int add(T value) throws Exception;
	
	
	/**
	 * @param id within this page
	 * @return byte array with this id
	 */
	public abstract T get(int id) throws Exception;
	
	
	/**
	 * removes the byte array with the given id and truncates the page
	 * @param id of the byte array to be removed
	 * @throws ElementNotFoundException 
	 */
	public abstract void remove(int id) throws ElementNotFoundException;
	
	/**
	 * @return the part of the body which is actually used up with data
	 */
	public abstract ByteBuffer bodyUsed();
	
	/**
	 * @return the remaining number Of bytes that can be used by the body or header
	 */
	public abstract int remaining();
}
