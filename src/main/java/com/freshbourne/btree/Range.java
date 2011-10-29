/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 *
 * http://creativecommons.org/licenses/by-nc/3.0/
 *
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2011 "Robin Wenglewski <robin@wenglewski.de>"
 */

package com.freshbourne.btree;

/**
 * Very generic Range object for getting values form the BTree
 *
 * @param <T>
 */
public class Range<T> {
	private T from;
	private T to;

	public Range(){}

	public Range(final T from, final T to){
		this.from = from;
		this.to = to;
	}

	public T getTo() {
		return to;
	}

	public void setTo(final T to) {
		this.to = to;
	}

	public T getFrom() {
		return from;
	}

	public void setFrom(final T from) {
		this.from = from;
	}
}

