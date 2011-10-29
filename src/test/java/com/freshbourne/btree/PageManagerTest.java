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
import com.freshbourne.io.AutoSaveResourceManager;
import com.freshbourne.io.ResourceManager;
import com.freshbourne.io.ResourceManagerBuilder;
import com.freshbourne.serializer.FixedStringSerializer;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class PageManagerTest {

	private static String path = "/tmp/PageManagerTest";
	private static File file = new File(path);
	private ResourceManager rm;
	private BTree<String, String> tree;
	private LeafPageManager<String, String> lpm;
	private InnerNodeManager<String, String> inm;

	@BeforeMethod
	public void setUp() throws IOException {
		file.delete();
		AutoSaveResourceManager manager = new ResourceManagerBuilder().file(file).buildAutoSave();
		tree = BTree.create(manager, FixedStringSerializer.INSTANCE_1000,
				FixedStringSerializer.INSTANCE_1000,
				StringComparator.INSTANCE);
		lpm = tree.getLeafPageManager();
		inm = tree.getInnerNodeManager();
		rm = tree.getResourceManager();
		if(!rm.isOpen())
			rm.open();
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

	@Test(groups = "skipBeforeMethod")
	public void doubleCreationWithCacheInvalidationShouldReturnOldInstanceIfNotGarbageCollected() throws IOException {
		file.delete();
		AutoSaveResourceManager manager = new ResourceManagerBuilder().file(file).cacheSize(1).buildAutoSave();
		tree = BTree.create(manager, FixedStringSerializer.INSTANCE_1000,
				FixedStringSerializer.INSTANCE_1000,
				StringComparator.INSTANCE);
		lpm = tree.getLeafPageManager();
		inm = tree.getInnerNodeManager();
		rm = tree.getResourceManager();

		LeafNode<String, String> page = lpm.createPage();

		// make sure page is not in the rm cache anymore
		lpm.createPage();
		lpm.createPage();

		LeafNode<String, String> page1 = lpm.getPage(page.getId());

		assertThat(page1).isSameAs(page);
	}
}
