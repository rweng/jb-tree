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

public class IOModuleTest {
    private static Injector injector;

    static {
        injector = Guice.createInjector(new IOModule());
    }
}
