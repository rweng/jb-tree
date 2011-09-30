/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */

package com.freshbourne.io;

import com.freshbourne.comparator.StringComparator;
import com.freshbourne.multimap.btree.BTree;
import com.freshbourne.multimap.btree.BTreeFactory;
import com.freshbourne.multimap.btree.BTreeModule;
import com.freshbourne.serializer.FixLengthSerializer;
import com.freshbourne.serializer.FixedStringSerializer;
import com.freshbourne.serializer.StringSerializer;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class BTreeFactoryTest {

    private static Injector     injector;
    private        BTreeFactory factory;

    static {
        injector = Guice.createInjector(new BTreeModule());
    }

    @Before
    public void setUp() {
        factory = injector.getInstance(BTreeFactory.class);
    }

    @Test
    public void creation() {
        assertNotNull(factory);
    }

    @Test
    public void factoryIsSingleton() {
        BTreeFactory factory2 = injector.getInstance(BTreeFactory.class);
        assertEquals(factory, factory2);
    }


    @Test
    public void get() {
        BTree rm = factory.get(new File("/tmp/factorytestFile"),
                FixedStringSerializer.INSTANCE, FixedStringSerializer.INSTANCE,
                StringComparator.INSTANCE);

        BTree rm2 = factory.get(new File("/tmp/factorytestFile"),
                FixedStringSerializer.INSTANCE, FixedStringSerializer.INSTANCE,
                StringComparator.INSTANCE);
        
        BTree rm3 = factory.get(new File("/tmp/factorytestFile2"),
                FixedStringSerializer.INSTANCE, FixedStringSerializer.INSTANCE,
                StringComparator.INSTANCE);


        assertNotNull(rm);
        assertEquals(rm, rm2);
        assertNotSame(rm, rm3);
    }

}
