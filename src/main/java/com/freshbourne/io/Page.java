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
 * A Page is a <code>byte[]</code> from a {@link ResourceManager}.
 * 
 * If the 
 * 
 * 
 * 
 * @author "Robin Wenglewski <robin@wenglewski.de>"
 *
 */
public class Page {
	
	private ByteBuffer buffer;
	private int id;
	private ResourceManager resourceManager;
		
	public Page(ByteBuffer buffer, int id, ResourceManager rm){
		this.buffer = buffer;
		this.id = id;
		this.resourceManager = rm;
	}
	
	public ByteBuffer getBuffer(){
		return buffer;
	}
	
	public int getSize(){
		return buffer.capacity();
	}
	
	public int getId(){return id;}
	
	public ResourceManager getResourceManager(){return resourceManager;}
	public void save() throws IOException{
		resourceManager.writePage(this);
	}
}
