/**
 * Copyright (C) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.io;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class BufferPoolManagerImplSpec extends BufferPoolManagerSpec {
	
	private static Injector injector;
	static {
		injector = Guice.createInjector(new IOModule("/tmp/bpm_test"));
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.BufferPoolManagerSpec#createBufferPoolManager()
	 */
	@Override
	protected BufferPoolManager createBufferPoolManager() {
		return injector.getInstance(BufferPoolManagerImpl.class);
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.BufferPoolManagerSpec#getCacheSize()
	 */
	@Override
	protected int getCacheSize() {
		return 5;
	}

}
