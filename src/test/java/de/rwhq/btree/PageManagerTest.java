/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 *
 * http://creativecommons.org/licenses/by-nc/3.0/
 *
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2011 "Robin Wenglewski <robin@wenglewski.de>"
 */

package de.rwhq.btree;

import de.rwhq.comparator.StringComparator;
import de.rwhq.io.rm.ResourceManager;
import de.rwhq.io.rm.ResourceManagerBuilder;
import de.rwhq.serializer.FixedStringSerializer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public class PageManagerTest {

	private static String path = "/tmp/PageManagerTest";
	private static File file = new File(path);
	private ResourceManager rm;
	private BTree<String, String> tree;
	private LeafPageManager<String, String>  lpm;
	private InnerNodeManager<String, String> inm;

	@BeforeMethod
	public void setUp() throws IOException {
		file.delete();
		final ResourceManager manager = new ResourceManagerBuilder().file(file).useCache(false).build();
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
		final List<Integer> leafs = new LinkedList();
		final List<Integer> inners = new LinkedList();

		final int count = 10000;
		for(int i = 0;i<count;i++){
			if(i%2==0){
				leafs.add(lpm.createPage().getId());
			} else {
				inners.add(inm.createPage().getId());
			}
		}

		for(final int id : leafs){
			assertThat(lpm.getPage(id)).isNotNull();
		}

		for(final int id : inners){
			assertThat(inm.getPage(id)).isNotNull();
		}
	}
}
