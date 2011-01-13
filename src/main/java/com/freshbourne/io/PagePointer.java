/*
 * Copyright (c) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 */

/**
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 * 
 * (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */
package com.freshbourne.io;

/** 
 * This object points to a byte in a page
 * 
 * @author "Robin Wenglewski <robin@wenglewski.de>"
 *
 */
public class PagePointer {
	
	private int offset;
	private int id;
	
	
	
	/**
	 * @param offset
	 * @param id
	 */
	public PagePointer(int id, int offset) {
		super();
		this.offset = offset;
		this.id = id;
	}
	
	/**
	 * @param offset the offset to set
	 */
	public void setOffset(int offset) {
		this.offset = offset;
	}
	/**
	 * @return the offset
	 */
	public int getOffset() {
		return offset;
	}
	/**
	 * @param pageId the pageId to set
	 */
	public void setId(int pageId) {
		this.id = pageId;
	}
	/**
	 * @return the pageId
	 */
	public int getId() {
		return id;
	}
	
	public boolean equals(Object o){
		return o instanceof PagePointer && ((PagePointer)o).getId() == getId() && 
			((PagePointer)o).getOffset() == getOffset();
	}

}
