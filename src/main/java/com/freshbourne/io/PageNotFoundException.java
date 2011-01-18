/*
 * Copyright (c) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 */

package com.freshbourne.io;

import java.io.IOException;

public class PageNotFoundException extends IOException {

	public PageNotFoundException(ResourceManager rm, RawPage page){
		this(rm, page.id());
	}
	
    public PageNotFoundException(ResourceManager rm, Long pageId){
        super("The Page with the id " + pageId + " could not be found in the ResourceManager " + rm.toString());
    }
}
