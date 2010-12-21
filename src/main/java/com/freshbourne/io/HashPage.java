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
 * A HashPage is the most basic for of a Page. It makes sure that the content
 * of the page is valid by hashing the body. Therefore, update() should be called
 * when the content changes.
 * 
 * @author Robin Wenglewski <robin@wenglewski.de>
 *
 */
public class HashPage extends Observable implements Page {
	private final ResourceManager source;
	private final int id;
	
	private ByteBuffer buffer;
	private final ByteBuffer header;
	private final ByteBuffer body;
	private boolean valid = false;
	
	private static final int HASH_SIZE = 4;
	
	
	protected HashPage(ByteBuffer buffer, ResourceManager source, int id){
		this.source = source;
		this.id = id;
		
		this.buffer = buffer;
		
		// create the header
		buffer.position(0);
		buffer.limit(HASH_SIZE);
		this.header = buffer.slice();
		
		// slice header away from the body;
		buffer.limit(buffer.capacity());
		buffer.position(HASH_SIZE);
		this.body = buffer.slice();
		
		// reset the limit
		buffer.limit(buffer.capacity());
	}
	
	/**
	 * writes the header to the <code>byte[]</code> and makes the page valid
	 */
	public void initialize(){
		header.position(0);
		header.putInt(body.hashCode());
		valid = true;
	}
	
	/**
	 * @return a readOnly ByteBuffer over the complete <code>byte[]</code> underneath this page
	 */
	public ByteBuffer buffer(){
		return buffer.asReadOnlyBuffer();
	}
	
	/**
	 * @return a ByteBuffer over the<code>byte[]</code> underneath this page without the header.
	 */
	public ByteBuffer body(){
		return body;
	}
	
	/**
	 * @return true if the <code>byte[]</code> is valid (e.g. header is written).
	 */
	public boolean valid(){
		return valid || bodyHash() == body.hashCode();
	}
	/**
	 * returns the ResourceManger in which the page was created. All pages must be created in
	 * a Resource Manager
	 * @return source resource manager or null
	 */
	public ResourceManager resourceManager(){return source;}
	
	/**
	 * 
	 * @return page id in the context of the ResourceManager
	 */
	public int id(){return id;}
	
	private int bodyHash(){
		buffer.position(0);
		return buffer.getInt();
	}
}
