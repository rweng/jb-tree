/**
 * Copyright (C) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.multimap.btree;

import com.freshbourne.io.PagePointer;

public class AdjustmentAction<K,V> {
	public enum ACTION {INSERT_NEW_NODE, UPDATE_KEY}
	
	private ACTION action;
	private PagePointer keyPointer;
	private Long pageId;
	
	protected AdjustmentAction(ACTION action, PagePointer keyPointer, Long pageId){
		this.setAction(action);
		this.setKey(keyPointer);
		this.setPageId(pageId);
	}

	/**
	 * @param action the action to set
	 */
	public void setAction(ACTION action) {
		this.action = action;
	}

	/**
	 * @return the action
	 */
	public ACTION getAction() {
		return action;
	}

	/**
	 * @param key the key to set
	 */
	public void setKey(PagePointer key) {
		this.keyPointer = key;
	}

	/**
	 * @return the key
	 */
	public PagePointer getKeyPointer() {
		return keyPointer;
	}

	/**
	 * @param pageId the node to set
	 */
	public void setPageId(Long pageId) {
		this.pageId = pageId;
	}

	/**
	 * @return the pageId
	 */
	public Long getPageId() {
		return pageId;
	}

}
