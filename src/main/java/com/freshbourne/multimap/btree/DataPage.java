/**
 * Copyright (C) 2010 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.multimap.btree;

import java.nio.ByteBuffer;

import com.freshbourne.io.Page;

/**
 * Wraps around a <code>byte[]</code> and can hold values of type T.
 * 
 * @author Robin Wenglewski <robin@wenglewski.de>
 *
 * @param <T>
 */
public class DataPage<T> implements Page{
	
	private final ByteBuffer p;
	
	DataPage(byte[] p){
		this.p = ByteBuffer.wrap(p);
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.Page#initialize()
	 */
	@Override
	public void initialize() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.Page#buffer()
	 */
	@Override
	public byte[] buffer() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.Page#body()
	 */
	@Override
	public byte[] body() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.Page#valid()
	 */
	@Override
	public boolean valid() {
		// TODO Auto-generated method stub
		return false;
	}
	
	// add(value), remove_value, 

}
