/**
 * Copyright (C) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.io;

import java.io.IOException;
import java.util.Map;

public abstract class AbstractPageManager<T extends ComplexPage> implements PageManager<T> {
	
	private final PageManager<RawPage> rpm;
	private Map<Integer, T> cache = new SoftReferenceCacheMap<Integer, T>();;
	
	protected AbstractPageManager(PageManager<RawPage> rpm) {
		this.rpm = rpm;
	}
	
	protected PageManager<RawPage> getRawPageManager(){
		return rpm;
	}
	
	/* (non-Javadoc)
	 * @see com.freshbourne.io.PageManager#getPage(int)
	 */
	@Override
	public T getPage(int id) {
		T result;
		
		if (cache.containsKey(id)) {
			result = cache.get(id);
			return result;
		}

		result = createObjectPage(rpm.getPage(id));
		
		try {
			result.load();
			cache.put(id, result);
		} catch (IOException e) {
			throw new IllegalArgumentException("cant load InnerNodePage with id " + id);
		}
		
		return result;
	}
	
	/* (non-Javadoc)
	 * @see com.freshbourne.io.PageManager#createPage()
	 */
	@Override
	public T createPage() {
		T l = createObjectPage(rpm.createPage());
		l.initialize();
		cache.put(l.rawPage().id(), l);
		return l;
	}

	
	
	/* (non-Javadoc)
	 * @see com.freshbourne.io.PageManager#removePage(int)
	 */
	@Override
	public void removePage(int id) {
		cache.remove(id);
		rpm.removePage(id);
		
	}
	
	protected abstract T createObjectPage(RawPage page);

	/* (non-Javadoc)
	 * @see com.freshbourne.io.PageManager#hasPage(long)
	 */
	@Override
	public boolean hasPage(int id) {
		if(!rpm.hasPage(id))
			return false;
		
		if(cache.containsKey(id))
			return true;
		
		try {
			T page = createObjectPage(rpm.getPage(id));
			page.load();
			cache.put(page.rawPage().id(), page);
		} catch (Exception e) {
			return false;
		}
		
		return true;
	}	
	
	/* (non-Javadoc)
	 * @see com.freshbourne.io.PageManager#sync()
	 */
	@Override
	public void sync() {
		getRawPageManager().sync();
	}
}
