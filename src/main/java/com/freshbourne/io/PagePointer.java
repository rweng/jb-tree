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
	private int pageId;
	
	
	
	/**
	 * @param offset
	 * @param pageId
	 */
	public PagePointer(int offset, int pageId) {
		super();
		this.offset = offset;
		this.pageId = pageId;
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
	public void setPageId(int pageId) {
		this.pageId = pageId;
	}
	/**
	 * @return the pageId
	 */
	public int getPageId() {
		return pageId;
	}

}
