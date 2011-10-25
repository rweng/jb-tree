/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 *
 * http://creativecommons.org/licenses/by-nc/3.0/
 *
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2011 "Robin Wenglewski <robin@wenglewski.de>"
 */

package com.freshbourne.io;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.nio.ByteBuffer;


public class RawPage {

    private ByteBuffer buffer;
    private int id;
    private ResourceManager resourceManager;
    
    private static Log LOG = LogFactory.getLog(RawPage.class);
	
    /**
     * buffer has been modified since RawPage was created?
     */
    private boolean modified = false;


    public RawPage(ByteBuffer buffer, int pageId){this(buffer, pageId, null);}
    public RawPage(ByteBuffer buffer, int pageId, ResourceManager rm){
        this.buffer = buffer;
        this.id = pageId;
        this.resourceManager = rm;
    }

    /**
     * @return ByteBuffer backing this RawPage
     */
    public ByteBuffer bufferForWriting(int pos){setModified(true); buffer.position(pos); return buffer;}
    public ByteBuffer bufferForReading(int pos){buffer.position(pos); return buffer.asReadOnlyBuffer();}
    public Integer id(){return id;}
    
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
	
	@Override
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
