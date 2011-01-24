/*
 * Copyright (c) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 */

package com.freshbourne.io;

import java.nio.ByteBuffer;


/**
 * This class is a byte array, eventually from a resource manager and always with an id != null and != 0L
 */
public class RawPage {

    private final ByteBuffer buffer;
    private final ResourceManager rm;
    private final Long id;

    public RawPage(ByteBuffer buffer){
        this(buffer, null, null);
    }

    public RawPage(ByteBuffer buffer, ResourceManager rm, Long pageId){
        if(pageId == null || pageId == 0L)
        	throw new IllegalArgumentException("A RawPage id must not be null or 0L");
    	
    	this.buffer = buffer;
        this.rm = rm;
        this.id = pageId;
    }

    public ByteBuffer buffer(){return buffer;}
    public Long id(){return id;}
    public ResourceManager resourceManager(){return rm;}
    public void writeToResource() {
    	if(rm == null){
    		throw new IllegalStateException("Resource Manager not set");
    	}
    	
    	rm.writePage(this);
    }
}
