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

	public Range(T from, T to){
		this.from = from;
		this.to = to;
	}

	public T getTo() {
		return to;
	}

	public void setTo(T to) {
		this.to = to;
	}

	public T getFrom() {
		return from;
	}

	public void setFrom(T from) {
		this.from = from;
	}
}

