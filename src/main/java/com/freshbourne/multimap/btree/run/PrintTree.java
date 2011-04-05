/**
 * Copyright (C) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.multimap.btree.run;

import java.io.File;
import java.util.Iterator;

import com.freshbourne.multimap.btree.BTree;
import com.freshbourne.multimap.btree.BTreeModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

/**
 * Iterates over the values of a tree
 */
public class PrintTree {

	public static void main(String[] args) {
		
		
		File f = new File("/tmp/c1Index");
		if(!f.exists())
			throw new IllegalArgumentException("File does not exist");
		
		Injector injector = Guice.createInjector(new BTreeModule(f.getAbsolutePath()));
		BTree<Integer, String> tree = injector.getInstance(Key.get(new TypeLiteral<BTree<Integer,String>>(){}));
		
		tree.load();
		Iterator<String> it = tree.getIterator();
		while(it.hasNext()){
			System.out.println(it.next());
		}
	}

}
