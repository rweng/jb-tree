/**
 * Copyright (C) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.multimap;

import java.io.File;

import org.junit.Test;

import com.freshbourne.multimap.btree.BTree;
import com.freshbourne.multimap.btree.BTreeProvider;

public class BTreePTest extends MultiMapPTest<Integer, String>  {
	
	private static String path = "/tmp/btree_performance_test";
	private static BTreeProvider provider = new BTreeProvider(path);
	
	public BTreePTest() {
		super(provider);
	}
	
	
	@Test
	public void shouldNotHaveTooMuchOverhead(){
		int key = getProvider().createRandomKey();
		String val = getProvider().createRandomValue();
		
		int sizeForKey = Integer.SIZE / 8;
		int sizeForVal = val.length();
		
		// insert 10.000 K/V pairs
		int size = 100000;
		long start = System.currentTimeMillis();
		for(int i = 0; i<size;i++){
			getMultiMap().add(key, val);
		}
		
		getTree().sync();
		long end = System.currentTimeMillis();
		
		File file = new File(path);
		Long sizeOfData = (long)(size * (sizeForKey + sizeForVal));
		float realSizePercent = file.length() / sizeOfData * 100; 
		
		System.out.println("====== BTREE: SIZE OVERHEAD TEST ======");
		System.out.println("key + value data inserted:" + sizeOfData / 1024 + "k");
		System.out.println("fileSize: " + file.length()/1024 + "k ("+realSizePercent+"%)");
		System.out.println("time for insert w/ sync in millis: " + (end - start));
		//assertThat("current Size: " + realSizePercent + "%", realSizePercent, lessThan(1000f));
	}
	
	private BTree<Integer, String> getTree(){
		return (BTree<Integer, String>) getMultiMap();
	}	
}
