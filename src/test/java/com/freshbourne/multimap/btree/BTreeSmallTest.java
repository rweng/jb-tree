/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */

package com.freshbourne.multimap.btree;

import com.freshbourne.io.FileResourceManager;
import com.freshbourne.io.PageSize;
import com.google.inject.*;
import com.google.inject.util.Modules;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BTreeSmallTest {

    private static Injector                injector;
    private        BTree<Integer, Integer> tree;
    private static final Logger LOG = Logger.getLogger(BTreeSmallTest.class);
    private static final int PAGE_SIZE = 20;


    static {
        injector = Guice.createInjector(
                Modules.override(new BTreeModule("/tmp/btree-small-test")).with(new AbstractModule() {
                    @Override protected void configure() {
                        bind(Integer.class).annotatedWith(PageSize.class).toInstance(PAGE_SIZE);
                    }
                }));
    }

    @Before
    public void setUp() {
        tree = injector.getInstance(Key.get(new TypeLiteral<BTree<Integer, Integer>>() {
        }));
    }

    @Test
    public void ensurePageSizeIsSmall(){
        assertEquals(PAGE_SIZE, injector.getInstance(FileResourceManager.class).pageSize());
    }

    @Test
    public void testsWorking() {
        assertTrue(true);
    }

    @Test
    public void testInitialize() throws IOException {
        tree.initialize();
        assertTrue(tree.isValid());
    }

    @Test
    public void test() throws IOException {
        int count = 1000;

        tree.initialize();

        for (int i = 0; i < count; i++) {
            tree.add(i, i);
        }

        LOG.info("Depth: " + tree.getDepth());

        assertEquals(count, tree.getNumberOfEntries());
    }

}
