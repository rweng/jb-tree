/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 *
 * http://creativecommons.org/licenses/by-nc/3.0/
 *
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2011 "Robin Wenglewski <robin@wenglewski.de>"
 */
package com.freshbourne.btree.run;

import com.freshbourne.btree.BTree;
import com.freshbourne.comparator.IntegerComparator;
import com.freshbourne.io.AutoSaveResourceManager;
import com.freshbourne.io.ResourceManager;
import com.freshbourne.io.ResourceManagerBuilder;
import com.freshbourne.serializer.FixedStringSerializer;
import com.freshbourne.serializer.IntegerSerializer;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * Iterates over the values of a tree
 */
public class PrintTree {

	private static Logger LOG = Logger.getLogger(PrintTree.class);
	static{
		Logger.getLogger("com.freshbourne").setLevel(Level.DEBUG);
	}
	public static void main(final String[] args) throws IOException {
		
		
		final File f = new File("/tmp/indexha");
		if(!f.exists())
			throw new IllegalArgumentException("File does not exist");

		final ResourceManager resourceManager = new ResourceManagerBuilder().file(f).build();
		final BTree<Integer, String> tree = BTree.create(resourceManager, IntegerSerializer.INSTANCE, FixedStringSerializer.INSTANCE_1000,
				IntegerComparator.INSTANCE);
		
		final Iterator<String> it = tree.getIterator();
		while(it.hasNext()){
			System.out.println(it.next());
		}
	}

}
