/**
 * Copyright (C) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.multimap.btree;

public class AdjustmentAction<K,V> {
	public enum ACTION {INSERT_NEW_NODE, UPDATE_KEY}
	
	private ACTION action;
	private K key;
	private Long node;
	
	protected AdjustmentAction(ACTION action, K key, Long pageId){
		this.setAction(action);
		this.setKey(key);
		this.setNode(node);
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
	public void setKey(K key) {
		this.key = key;
	}

	/**
	 * @return the key
	 */
	public K getKey() {
		return key;
	}

	/**
	 * @param node the node to set
	 */
	public void setNode(Long node) {
		this.node = node;
	}

	/**
	 * @return the node
	 */
	public Long getNode() {
		return node;
	}

}
