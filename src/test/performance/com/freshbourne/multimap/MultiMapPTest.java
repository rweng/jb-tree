/**
 * Copyright (C) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.multimap;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public abstract class MultiMapPTest<K, V> extends MultiMapTestBase<K,V> {
	
	protected MultiMapPTest(MultiMapProvider<K, V> provider) {
		super(provider);
	}

	@Test
	public void shouldWorkWithMassiveValues(){
		int size = 10000;

		fill(size);
		
		assertEquals(size, tree.getNumberOfEntries());
		key1 = getProvider().createMaxKey();
		simpleTests();
		key1 = getProvider().createMinKey();
		simpleTests();
	}
}
