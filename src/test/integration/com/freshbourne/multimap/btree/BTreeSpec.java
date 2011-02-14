/**
 * Copyright (C) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.multimap.btree;

import com.freshbourne.multimap.MultiMapSpec;

public class BTreeSpec extends MultiMapSpec<Integer, String> {
	
	private static BTreeProvider provider = new BTreeProvider("/tmp/btree_spec"); 
	
	public BTreeSpec() {
		super(provider);
	}

	
}
