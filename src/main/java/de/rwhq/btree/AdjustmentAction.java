/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 *
 * http://creativecommons.org/licenses/by-nc/3.0/
 *
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2011 "Robin Wenglewski <robin@wenglewski.de>"
 */
package de.rwhq.btree;

class AdjustmentAction<K,V> {
	public enum ACTION {INSERT_NEW_NODE, UPDATE_KEY}
	
	private ACTION action;
	private byte[] serializedKey;
	private Integer pageId;
	
	protected AdjustmentAction(final ACTION action, final byte[] serializedKey, final Integer pageId){
		this.setAction(action);
		this.setSerializedKey(serializedKey);
		this.setPageId(pageId);
	}

	/**
	 * @param action the action to set
	 */
	public void setAction(final ACTION action) {
		this.action = action;
	}

	/**
	 * @return the action
	 */
	public ACTION getAction() {
		return action;
	}
	
	/**
	 * @param pageId the node to set
	 */
	public void setPageId(final Integer pageId) {
		this.pageId = pageId;
	}

	/**
	 * @return the pageId
	 */
	public Integer getPageId() {
		return pageId;
	}

	/**
	 * @param serializedKey the serializedKey to set
	 */
	public void setSerializedKey(final byte[] serializedKey) {
		this.serializedKey = serializedKey;
	}

	/**
	 * @return the serializedKey
	 */
	public byte[] getSerializedKey() {
		return serializedKey;
	}

    public String toString(){
        return "AdjustmentAction(type: " + action + ", pageId: " + pageId + ")";
    }

}
