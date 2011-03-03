/*
 * Copyright (c) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 */

package com.freshbourne.io;

import java.nio.ByteBuffer;
import java.util.Random;


/**
 * This class wraps a byte array and usually has an id and a source
 * 
 * Long is chosen as id, although in most scenarios Integer should be sufficient. However, Long can be used in more cases.
 * Maybe this class gets refactored to enable also Integer ids.
 * 
 */
public class RawPage {

    private ByteBuffer buffer;
    private Long id;
    private ResourceManager resourceManager;
    
    /**
     * buffer has been modified since RawPage was created?
     */
    private boolean modified = false;


    public RawPage(ByteBuffer buffer, Long pageId){this(buffer, pageId, null);}
    public RawPage(ByteBuffer buffer, Long pageId, ResourceManager rm){
        this.buffer = buffer;
        this.id = pageId;
        this.resourceManager = rm;
    }

    /**
     * @return ByteBuffer backing this RawPage
     */
    public ByteBuffer bufferForWriting(int pos){setModified(true); buffer.position(pos); return buffer;}
    public ByteBuffer bufferForReading(int pos){buffer.position(pos); return buffer.asReadOnlyBuffer();}
    public Long id(){return id;}
    
    /**
     * @return a random Long but 0L
     */
    private static long lastid = 0;
    public static Long generateId(){
    	long result;
		do{
			result = (new Random()).nextLong();
		} while (result == 0L);
		return ++lastid;
    }

	/**
	 * @param modified the modified to set
	 */
	public void setModified(boolean modified) {
		this.modified = modified;
	}

	/**
	 * @return the modified
	 */
	public boolean isModified() {
		return modified;
	}

	/**
	 * @return the resourceManager
	 */
	public ResourceManager getResourceManager() {
		return resourceManager;
	}
	
	protected void finalize() throws Throwable {
		try {
			sync();
		} catch (Exception e) {
			super.finalize();
		}
	}
	
	/**
	 * syncs the RawPage with the ResourceManager its from.
	 */
	public void sync() {
		if (isModified())
			getResourceManager().writePage(this);
	}
    
    
}
