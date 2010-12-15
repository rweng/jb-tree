/**
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 * 
 * (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */
package com.freshbourne.io;

import java.io.File;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

public class ResourceManagerModule extends AbstractModule{

	private int pageSize = 4086;
	private String filePath = "/tmp/default_file";
	private File file;
	
	/* (non-Javadoc)
	 * @see com.google.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bindInterceptor(Matchers.any(), Matchers.annotatedWith(MustBeOpen.class), new IsOpenEnsurer());
	}
	
	public void setPath(String path){
		filePath = path;
	}
	
	public void setFile(File file){
		this.file = file;
	}
	
	public void setPageSize(int pageSize){
		this.pageSize = pageSize;
	}

}
