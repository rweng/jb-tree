/*
 * Copyright (c) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 */

/**
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 * 
 * (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */
package com.freshbourne.io;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

import java.io.File;

public class IOModule extends AbstractModule{
	
	// ***** CONFIGURATION (CONSTRUCTURS) *****
	private File file;
	private int pageSize = -1;
	
	public IOModule(File file){
		this(file, PageSize.DEFAULT_PAGE_SIZE);
	}
	
	public IOModule(String path){
		this(new File(path));
	}
	
	public IOModule(File file, int pageSize){
		super();
		this.file = file;
		this.pageSize = pageSize;
	}

    public void resourceFile(File file){this.file = file;};
    public void pageSize(int i){pageSize = i;}

	
	// ***** CONFIGURE *****
	
	/* (non-Javadoc)
	 * @see com.google.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
        if(pageSize != -1)
		    bind(Integer.class).annotatedWith(PageSize.class).toInstance(pageSize);

        if(file != null)
		    bind(File.class).annotatedWith(ResourceFile.class).toInstance(file);
		
		bind(ResourceManager.class).to(FileResourceManager.class).in(Singleton.class);
		bind(BufferPoolManager.class).to(BufferPoolManagerImpl.class);
		
		bindConstant().annotatedWith(Names.named("cacheSize")).to(300);
	}

    // this worked
//	@Provides @Singleton
//	public ResourceManager provideFileResourceManager() throws IOException{
//		ResourceManager result = new FileResourceManager(file, pageSize);
//		result.open();
//		return result;
//	}

	
}
