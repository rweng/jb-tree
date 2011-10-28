/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 *
 * http://creativecommons.org/licenses/by-nc/3.0/
 *
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2011 "Robin Wenglewski <robin@wenglewski.de>"
 */

package com.freshbourne.btree;

import com.freshbourne.comparator.StringComparator;
import com.freshbourne.io.ResourceManager;
import com.freshbourne.serializer.FixedStringSerializer;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class PageManagerTest {

	private static Injector injector;
	private static String path = "/tmp/PageManagerTest";
	private static File file = new File(path);
	private BTreeFactory factory;
	private ResourceManager rm;
	private BTree<String, String> tree;
	private LeafPageManager<String, String> lpm;
	private InnerNodeManager<String, String> inm;

	static{
		injector = Guice.createInjector(new BTreeModule());
	}

	@BeforeMethod
	public void setUp() throws IOException {
		factory = injector.getInstance(BTreeFactory.class);
		file.delete();
		tree = factory.get(file, FixedStringSerializer.INSTANCE_1000, FixedStringSerializer.INSTANCE_1000,
				StringComparator.INSTANCE, true);
		lpm = tree.getLeafPageManager();
		inm = tree.getInnerNodeManager();
		rm = tree.getResourceManager();
	}

	@Test
	public void pageCreations(){
		List<Integer> leafs = new LinkedList();
		List<Integer> inners = new LinkedList();

		int count = 10000;
		for(int i = 0;i<count;i++){
			if(i%2==0){
				leafs.add(lpm.createPage().getId());
			} else {
				inners.add(inm.createPage().getId());
			}
		}

		for(int id : leafs){
			assertNotNull(lpm.getPage(id));
		}

		for(int id : inners){
			assertNotNull(inm.getPage(id));
		}
	}
}
