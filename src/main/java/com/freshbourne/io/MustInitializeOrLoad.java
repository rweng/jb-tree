/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */
package com.freshbourne.io;

import java.io.IOException;


public interface MustInitializeOrLoad {
	
	/**
	 *
     * Since this is an extremely generic interface,
     * some implementations might throw an IOException, some might not.
     *
     * Just be be sure, the interface specifies the Exception.
     * In the documentation of you implementation it can be specified that this
     * Exception is never thrown.
     *
     * @throws java.io.IOException
     */
	public void initialize() throws IOException;
	
	/**
	 * @throws IOException
	 */
	public void load() throws IOException;
	
	/**
	 * @return if the object has been initialized or loaded
	 */
	public boolean isValid();

    /**
     * Loads the object if it can be loaded, otherwise initializes it
     *
     * @throws IOException
     */
    public void loadOrInitialize() throws IOException;
}
