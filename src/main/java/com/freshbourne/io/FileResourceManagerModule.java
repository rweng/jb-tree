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
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;

public class FileResourceManagerModule extends AbstractModule{
	
	private final File file;
	private final int pageSize;
	
	public FileResourceManagerModule(File file){
		this(file, PageSize.DEFAULT_PAGE_SIZE);
	}
	
	public FileResourceManagerModule(File file, int pageSize){
		super();
		this.file = file;
		this.pageSize = pageSize;
	}

	/* (non-Javadoc)
	 * @see com.google.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bindInterceptor(Matchers.any(), Matchers.annotatedWith(MustBeOpen.class), new IsOpenEnsurer());
		
		bind(Integer.class).annotatedWith(PageSize.class).toInstance(pageSize);
		bind(File.class).annotatedWith(ResourceFile.class).toInstance(file);
		
		bind(ResourceManager.class).to(FileResourceManager.class).in(Singleton.class);
	}
	
}