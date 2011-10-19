/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */

package com.freshbourne.io;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;


public class FileResourceManagerFactoryTest {

    private static Injector injector;
    private FileResourceManagerFactory factory;

    static {
        injector = Guice.createInjector(new IOModule());
    }

    @Before
    public void setUp(){
        factory = injector.getInstance(FileResourceManagerFactory.class);
    }

    @Test
    public void creation(){
        assertNotNull(factory);
    }

    @Test
    public void factoryIsSingleton(){
        FileResourceManagerFactory factory2 = injector.getInstance(FileResourceManagerFactory.class);
        assertEquals(factory, factory2);
    }

    @Test
    public void get() throws IOException {
        FileResourceManager rm = factory.get(new File("/tmp/factorytestFile"), false);
        FileResourceManager rm2 = factory.get(new File("/tmp/factorytestFile"), false);
        FileResourceManager rm3 = factory.get(new File("/tmp/factorytestFile2"), false);


        assertNotNull(rm);
        assertEquals(rm, rm2);
        assertNotSame(rm, rm3);
    }

    @Test(expected = IOException.class)
    public void shouldThrowAnExceptionIfFileDirDoesNotExist() throws IOException {
        factory.get(new File("/tmp/lkjsdfs/bla"), false);
    }
}
