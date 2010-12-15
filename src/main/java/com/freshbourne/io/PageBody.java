/**
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 * 
 * (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */
package com.freshbourne.io;

import java.nio.ByteBuffer;

public class PageBody {
	private final ByteBuffer buffer;
	private final Page page;
	
	PageBody(Page page, ByteBuffer buffer){
		this.page = page;
		this.buffer = buffer;
	}
	
	public int hashCode(){
		buffer.position(0);
		return buffer.hashCode();
	}
	
	int size(){
		return buffer.limit();
	}
}
