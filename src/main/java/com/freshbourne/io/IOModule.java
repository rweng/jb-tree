/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */

ackage com.freshbourne.io;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import java.io.File;
import java.io.IOException;

public class IOModule extends AbstractModule{
	
	// ***** CONFIGURATION (CONSTRUCTURS) *****
	private File file;
	private int pageSize = PageSize.DEFAULT_PAGE_SIZE;
	private int cacheSize = 10;
	
	public IOModule(File file){
		super();
		this.file = file;
	}
	
	public IOModule(String path){
		this(new File(path));
	}
	
	public IOModule(File file, int pageSize){
		this(file);
		this.pageSize = pageSize;
	}
	
	public IOModule(File file, int pageSize, int cacheSize){
		this(file, pageSize);
		this.cacheSize = cacheSize;
	}

	public IOModule(String file, int pageSize, int cacheSize){
		this(new File(file), pageSize);
		this.cacheSize = cacheSize;
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
		
		bindConstant().annotatedWith(Names.named("cacheSize")).to(cacheSize);
		
		bind(new TypeLiteral<PageManager<RawPage>>(){}).to(ResourceManager.class).in(Singleton.class);
	}

    @Provides @Singleton
	public ResourceManager provideFileResourceManager() {
		ResourceManager result = new FileResourceManager(file, pageSize, false);
		try {
			result.open();
		} catch (IOException e) {
			e.printStackTrace();
			// throw no runtime error, since the user can handle the fact that the resource is closed
		}
		return result;
	}

}
