/**
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 * 
 * (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */
package com.freshbourne.io;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * A Page is wraped around a {@link ByteBuffer} from a {@link ResourceManager}.
 * 
 * @author "Robin Wenglewski <robin@wenglewski.de>"
 *
 */
public class Page {
	
	private ByteBuffer buffer;
	private int id;
	private ResourceManager resourceManager;
	private final PageHeader header;
	private final PageBody body;
	private boolean valid = false;
	
	
	Page(ByteBuffer buffer, int id, ResourceManager rm){
		this.buffer = buffer;
		this.id = id;
		this.resourceManager = rm;
		
		buffer.position(0);
		buffer.limit(PageHeader.bufferSize());
		this.header = new PageHeader(this, buffer.slice());
		buffer.limit(buffer.capacity());
		buffer.position(PageHeader.bufferSize());
		this.body = new PageBody(this, buffer.slice());
		buffer.limit(buffer.capacity());
	}
	
	/**
	 * writes the header to the buffer.
	 * Requires the buffer to be uninitialized.
	 */
	void initialize(){
		header.bodyHash(body.hashCode());
		valid = true;
	}
	
	PageHeader getHeader(){
		return header;
	}
	
	PageBody body(){
		return body;
	}
	
	boolean valid(){
		return valid || header.bodyHash() == body.hashCode();
	}
	
	ByteBuffer getBuffer(){
		buffer.position(0);
		return buffer.duplicate();
	}
	
	public int size(){
		return buffer.capacity();
	}
	
	int getId(){return id;}
	
	ResourceManager getResourceManager(){return resourceManager;}
	void save() throws IOException{
		resourceManager.writePage(this);
	}
}
