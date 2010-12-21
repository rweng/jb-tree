/**
 * Copyright (C) 2010 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.io;

import java.nio.ByteBuffer;

import org.perfectjpattern.core.api.behavioral.observer.IObserver;


/**
 * A HashPage is the most basic for of a Page. A hashpage is only re
 * @author Robin Wenglewski <robin@wenglewski.de>
 *
 */
public interface HashPage extends Page {
	/**
	 * writes the header to the <code>byte[]</code> and makes the page valid
	 */
	public void initialize();
	
	/**
	 * @return a readOnly ByteBuffer over the complete <code>byte[]</code> underneath this page
	 */
	public ByteBuffer buffer();
	
	/**
	 * @return a ByteBuffer over the<code>byte[]</code> underneath this page without the header.
	 */
	public ByteBuffer body();
	
	/**
	 * @return true if the <code>byte[]</code> is valid (e.g. header is written).
	 */
	public boolean valid();
}
