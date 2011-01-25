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
import java.util.Random;

import com.freshbourne.io.IOModule;
import com.freshbourne.multimap.MultiMap;
import com.freshbourne.multimap.MultiMapSpec;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

public class BTreeSpec extends MultiMapSpec<Integer, String> {

	private final static Injector injector;
	private static SecureRandom srand;
	
	static {
		injector = Guice.createInjector(new BTreeModule("/tmp/btree_spec")); 
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
	protected MultiMap<Integer, String> createMultiMap() {
		return injector.getInstance(Key.get(new TypeLiteral<BTree<Integer,String>>(){}));
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMapSpec#createRandomKey()
	 */
	@Override
	protected Integer createRandomKey() {
		return srand().nextInt();
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMapSpec#createRandomValue()
	 */
	@Override
	protected String createRandomValue() {
		return (new BigInteger(130, srand())).toString(32);
	}

}
