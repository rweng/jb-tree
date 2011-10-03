/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */
package com.freshbourne.btree.run;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import com.freshbourne.btree.BTree;
import com.freshbourne.btree.BTreeModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

/**
 * Iterates over the values of a tree
 */
public class PrintTree {

	public static void main(String[] args) throws IOException {
		
		
		File f = new File("/tmp/c1Index");
		if(!f.exists())
			throw new IllegalArgumentException("File does not exist");
		
		Injector injector = Guice.createInjector(new BTreeModule(f.getAbsolutePath()));
		BTree<String, String> tree = injector.getInstance(Key.get(new TypeLiteral<BTree<String,String>>(){}));
		
		tree.load();
		Iterator<String> it = tree.getIterator();
		while(it.hasNext()){
			System.out.println(it.next());
		}
	}

}
