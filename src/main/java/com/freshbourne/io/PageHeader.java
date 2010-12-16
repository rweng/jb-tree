/**
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 * 
 * (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */
package com.freshbourne.io;

import java.nio.ByteBuffer;

public class PageHeader {
	private final RawPage page;
	private final ByteBuffer buffer;
	
	PageHeader(RawPage page, ByteBuffer buffer){
		this.page = page;
		this.buffer = buffer;
	}
	
	
	
	void bodyHash(int hash){
		buffer.position(0);
		buffer.putInt(hash);
	}
	
	static int bufferSize(){ return 4; }
	
	public int bodyHash(){
		buffer.position(0);
		return buffer.getInt();
	}
	
	
	
}
