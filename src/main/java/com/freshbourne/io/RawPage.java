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
 * This class is a byte array, eventually from a resource manager and always with an id != null and != 0L
 */
public class RawPage {

    private final ByteBuffer buffer;
    private final Long id;
    
    /**
     * buffer has been modified since RawPage was created?
     */
    private boolean modified = false;


    public RawPage(ByteBuffer buffer, Long pageId){
        if(pageId == null || pageId == 0L)
        	throw new IllegalArgumentException("A RawPage id must not be null or 0L");
    	
    	this.buffer = buffer;
        this.id = pageId;
    }

    public ByteBuffer buffer(){return buffer;}
    public Long id(){return id;}
    
    public static Long generateId(){
		long result;
		do{
			result = (new Random()).nextLong();
		} while (result == 0L);
		return result;
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
    
    
}
