/**
 * Copyright (C) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.multimap;

import org.junit.Test;

import com.freshbourne.multimap.btree.BTreeProvider;

public class BTreePTest extends MultiMapPTest<Integer, String>  {
	
	private static BTreeProvider provider = new BTreeProvider("/tmp/btree_performance_test");
	
	public BTreePTest() {
		super(provider);
	}
	
	
	@Test
	public void shouldNotHaveTooMuchOverhead(){
		int key = getProvider().createRandomKey();
		String val = getProvider().createRandomValue();
		
		
		// insert 10.000 K/V pairs
		int size = 10000;
		for(int i = 0; i<size;i++){
			getMultiMap().add(key, val);
		}
		
		//TODO: getMultiMap().sync();
		
		
	}
	
	@Test
	public void shouldNotTakeTooLong(){}
	

}
