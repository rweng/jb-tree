/**
 * Copyright (C) 2010 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.io;

import java.io.IOException;
import java.nio.ByteBuffer;


/**
 * A page is wrapped around a {@link ByteBuffer}. 
 * 
 * The page is provided by the {@link ResourceManager} and has a for this manager unique pageId.
 * 
 * @author Robin Wenglewski <robin@wenglewski.de>
 *
 */
public class RawPage implements Page {
	
	private ByteBuffer buffer;
	private int id;
	private ResourceManager resourceManager;
	private final PageHeader header;
	private final ByteBuffer body;
	private boolean valid = false;
	
	
	RawPage(ByteBuffer buffer, int id, ResourceManager rm){
		this.buffer = buffer;
		this.id = id;
		this.resourceManager = rm;
		
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
	
	int getId(){return id;}
	
	ResourceManager getResourceManager(){return resourceManager;}
	void save() throws IOException{
		resourceManager.writePage(this);
	}

	/**
	 * @return complete buffer under the page, with random position and limit
	 */
	public byte[] buffer() {
		return buffer.array();
	}

	/**
	 * @return buffer without the header with random position and limit
	 */
	public byte[] body() {
		return body.array();
	}
}
