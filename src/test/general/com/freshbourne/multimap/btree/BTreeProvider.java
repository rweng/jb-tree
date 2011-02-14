/**
 * Copyright (C) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.multimap.btree;

import java.math.BigInteger;
import java.security.SecureRandom;

import com.freshbourne.multimap.MultiMap;
import com.freshbourne.multimap.MultiMapProvider;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

public class BTreeProvider implements MultiMapProvider<Integer, String> {

	private final Injector injector;
	private static SecureRandom srand;
	
	public BTreeProvider(String path) {
		injector = Guice.createInjector(new BTreeModule(path)); 
	}
	
	private static SecureRandom srand(){
		if(srand == null)
			srand = new SecureRandom(); 
		
		return srand;
	}
	
	
	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMapSpec#createMultiMap()
	 */
	@Override
	public MultiMap<Integer, String> createMultiMap() {
		BTree<Integer, String> tree = injector.getInstance(Key.get(new TypeLiteral<BTree<Integer,String>>(){}));
		tree.initialize();
		return tree;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMapSpec#createRandomKey()
	 */
	@Override
	public Integer createRandomKey() {
		return srand().nextInt();
	}
	
	

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMapSpec#createRandomValue()
	 */
	@Override
	public String createRandomValue() {
		return (new BigInteger(130, srand())).toString(32);
	}


	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMapSpec#createMaxKey()
	 */
	@Override
	public Integer createMaxKey() {
		return Integer.MAX_VALUE;
	}


	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMapSpec#createMinKey()
	 */
	@Override
	public Integer createMinKey() {
		return Integer.MIN_VALUE;
	}

}
