/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 *
 * http://creativecommons.org/licenses/by-nc/3.0/
 *
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2011 "Robin Wenglewski <robin@wenglewski.de>"
 */

package com.freshbourne.io.rm;

public class PagePointer {
	
	private int offset;
	private int id;
	
	
	
	/**
	 * @param offset
	 * @param id
	 */
	public PagePointer(final int id, final int offset) {
		super();
		this.offset = offset;
		this.id = id;
	}
	
	/**
	 * @param offset the offset to set
	 */
	public void setOffset(final int offset) {
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
	public void setId(final int pageId) {
		this.id = pageId;
	}
	/**
	 * @return the pageId
	 */
	public int getId() {
		return id;
	}
	
	@Override
	public boolean equals(final Object o){
		return o instanceof PagePointer && ((PagePointer)o).getId() == getId() && 
			((PagePointer)o).getOffset() == getOffset();
	}
	
	@Override
	public String toString(){
		return "PagePointer {id: " + getId() + ", offset: " + getOffset() + "}";
		
	}

}
