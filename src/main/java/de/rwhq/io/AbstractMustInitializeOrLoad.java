/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 *
 * http://creativecommons.org/licenses/by-nc/3.0/
 *
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2011 "Robin Wenglewski <robin@wenglewski.de>"
 */

package de.rwhq.io;

import java.io.IOException;

/**
 * wraps the MustInitializeOrLoad interface to implement the pretty generic method loadOrInitialize().
 */
public abstract class AbstractMustInitializeOrLoad implements MustInitializeOrLoad{

    @Override
    public void loadOrInitialize() throws IOException {
        try{
            load();
        } catch (IOException e){
            initialize();
        }
    }
}
