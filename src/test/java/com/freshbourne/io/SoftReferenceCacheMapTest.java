/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */
package com.freshbourne.io;

import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class SoftReferenceCacheMapTest {

	private Map<Integer, Integer> map;

	private Integer k1, k2, v1, v2, v3;


	@Before
	public void setUp() {
		map = new SoftHashMap<Integer, Integer>();

		k1 = 1;
		k2 = 2;
		v1 = 11;
		v2 = 22;
		v3 = 33;
	}

	@Test
	public void basicOperations() {
		assertEquals(0, map.size());
		map.put(k1, v1);
		assertEquals(1, map.size());
		assertTrue(map.containsKey(k1));
		assertTrue(map.containsValue(v1));

		map.put(k2, v2);
		assertEquals(2, map.size());
		assertTrue(map.containsKey(k2));
		assertTrue(map.containsValue(v2));

		map.put(k1, v3);
		assertEquals(2, map.size());
		assertTrue(map.containsKey(k1));
		assertTrue(map.containsValue(v3));
		assertEquals(v3, map.get(k1));

		map.remove(k1);
		assertEquals(1, map.size());
		assertFalse(map.containsKey(k1));
		assertFalse(map.containsValue(v3));
	}

	// dosnt work somehow, wonder how we could test this
	public void caching() {
		map.put(k1, v1);
		assertEquals(1, map.size());
		k1 = null;
		v1 = null;

		try {
			byte[][] buf = new byte[1024][];
			System.out.println("Allocating until DOOME...");
			for (int i = 0; i < buf.length; i++) {
				buf[i] = new byte[1024 * 1024];
			}
		} catch (
				Throwable e
				)

		{
			System.out.println("catched");
		}

		for (
				int i = 0;
				i < 10; i++)

		{
			System.gc();
		}

		assertEquals(0, map.size()

		);
	}

}
