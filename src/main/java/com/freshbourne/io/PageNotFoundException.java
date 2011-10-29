/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 *
 * http://creativecommons.org/licenses/by-nc/3.0/
 *
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2011 "Robin Wenglewski <robin@wenglewski.de>"
 */

package com.freshbourne.io;

public class PageNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public PageNotFoundException(final ResourceManager rm, final RawPage page){
		this(rm, page.id());
	}
	
    public PageNotFoundException(final ResourceManager rm, final int pageId){
        super("The Page with the id " + pageId + " could not be found in the ResourceManager " + rm.toString());
    }
}
