/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 *
 * http://creativecommons.org/licenses/by-nc/3.0/
 *
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2011 "Robin Wenglewski <robin@wenglewski.de>"
 */
package com.freshbourne.io;

import com.freshbourne.serializer.FixLengthSerializer;
import com.freshbourne.serializer.Serializer;


public interface DataPage<T> extends ComplexPage {
	
	/**
	 * adds some bytes to the underlying body. It is possible that the header
	 * also grows through this process.
	 * 
	 * @param value array to be written
	 * @return id of the entry/byte[] within this page, or null if the page could not be added (e.g. page is full)
	 */
	public Integer add(T value);
	
	
	/**
	 * @param id within this page
	 * @return byte array with this id
	 */
	public T get(int id);
	
	
	/**
	 * removes the byte array with the given id and truncates the page
	 * @param id of the byte array to be removed
	 * @throws ElementNotFoundException 
	 */
	public void remove(int id);
	
	/**
	 * @return the remaining number Of bytes that can be used by the body or header
	 */
	public int remaining();
	
	/**
	 * @return number of entries stored in the DataPage
	 */
	public int numberOfEntries();
	
	/**
	 * @return the serializer object used to serialize PagePoints
	 */
	public FixLengthSerializer<PagePointer, byte[]> pagePointSerializer();
	
	/**
	 * @return the serializer object used to serialize the data
	 */
	public Serializer<T, byte[]> dataSerializer();
}
