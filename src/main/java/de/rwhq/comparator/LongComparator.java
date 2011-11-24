/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 *
 * http://creativecommons.org/licenses/by-nc/3.0/
 *
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2011 "Robin Wenglewski <robin@wenglewski.de>"
 */

package de.rwhq.comparator;

import java.util.Comparator;

public enum LongComparator implements Comparator<Long> {
	INSTANCE;
	
	@Override
	public int compare(Long l1, Long l2) {
		return l1.compareTo(l2);
	}
}
