/**
 * Copyright (C) 2010 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.io;

import java.nio.ByteBuffer;
import java.util.Observable;


/**
 * @author Robin Wenglewski <robin@wenglewski.de>
 */
public class HashPageImpl extends Observable implements HashPage {
	
	private ByteBuffer buffer;
	private final PageHeader header;
	private final ByteBuffer body;
	private boolean valid = false;
	
	
	HashPageImpl(ByteBuffer buffer){
		this.buffer = buffer;
		
		// create the header
		buffer.position(0);
		buffer.limit(PageHeader.bufferSize());
		this.header = new PageHeader(this, buffer.slice());
		
		// slice header away from the body;
		buffer.limit(buffer.capacity());
		buffer.position(PageHeader.bufferSize());
		this.body = buffer.slice();
		
		// reset the limit
		buffer.limit(buffer.capacity());
	}
	
	/**
	 * writes the header to the buffer.
	 * Requires the buffer to be uninitialized.
	 */
	public void initialize(){
		header.bodyHash(body.hashCode());
		valid = true;
	}
	
	public boolean valid(){
		return valid || header.bodyHash() == body.hashCode();
	}
	
	/**
	 * @return complete buffer under the page, with random position and limit
	 */
	public ByteBuffer buffer() {
		return buffer.asReadOnlyBuffer();
	}

	/**
	 * @return buffer without the header with random position and limit
	 */
	public ByteBuffer body() {
		return body;
	}
}
