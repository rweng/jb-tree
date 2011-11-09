/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 *
 * http://creativecommons.org/licenses/by-nc/3.0/
 *
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2011 "Robin Wenglewski <robin@wenglewski.de>"
 */

package de.rwhq.io.rm;

import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * A byte array, usually with an id and a ResourceManager it comes from.
 */
public class RawPage implements Serializable {

    private ByteBuffer buffer;
    private int id;
    private ResourceManager resourceManager;
    
    // private static Log LOG = LogFactory.getLog(RawPage.class);

    /**
     * buffer has been modified since RawPage was created?
     */
    private boolean modified = false;


    public RawPage(final ByteBuffer buffer, final int pageId){this(buffer, pageId, null);}
    public RawPage(final ByteBuffer buffer, final int pageId, final ResourceManager rm){
        this.buffer = buffer;
        this.id = pageId;
        this.resourceManager = rm;
    }

    /**
     * @param pos
     * @return ByteBuffer backing this RawPage
     */
    public ByteBuffer bufferForWriting(final int pos){setModified(true); buffer.position(pos); return buffer;}
    public ByteBuffer bufferForReading(final int pos){buffer.position(pos); return buffer.asReadOnlyBuffer();}
    public Integer id(){return id;}
    
    	/**
	 * @param modified the modified to set
	 */
	public void setModified(final boolean modified) {
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
	
	/**
	 * if this RawPage was modified, this method syncs the RawPage to the ResourceManager it is from.
	 */
	public void sync() {
		if (isModified() && resourceManager != null)
			getResourceManager().writePage(this);
	}
}
