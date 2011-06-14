/**
 * Copyright (C) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.multimap.btree;

import org.junit.Test;

public class InnerNodeSpec extends InnerNodeTestBase {
	
	@Test(expected= IllegalStateException.class)
	public void insertShouldRequireInitializedRoot(){
		node.insert(key1, val1);
	}
}