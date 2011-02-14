/**
 * Copyright (C) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.io;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

import com.freshbourne.multimap.btree.InnerNode;

public abstract class AbstractPageManager<T extends ComplexPage> implements PageManager<T> {
	
	private final PageManager<RawPage> rpm;
	private Map<Long, Reference<T>> refs = new HashMap<Long, Reference<T>>();;
	
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
	public T getPage(long id) {
		T result;
		
		if(refs.containsKey(id)){
			result = refs.get(id).get();
			if(result != null)
				return result;
		}

		result = createObjectPage(rpm.getPage(id));
		
		try {
			result.load();
			refs.put(id, new SoftReference<T>(result));
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
		refs.put(l.rawPage().id(), new SoftReference<T>(l));
		return l;
	}

	
	
	/* (non-Javadoc)
	 * @see com.freshbourne.io.PageManager#removePage(int)
	 */
	@Override
	public void removePage(long id) {
		refs.remove(id);
		rpm.removePage(id);
		
	}
	
	protected abstract T createObjectPage(RawPage page);

	/* (non-Javadoc)
	 * @see com.freshbourne.io.PageManager#hasPage(long)
	 */
	@Override
	public boolean hasPage(long id) {
		if(!rpm.hasPage(id))
			return false;
		
		if(refs.containsKey(id))
			return true;
		
		try {
			T page = createObjectPage(rpm.getPage(id));
			page.load();
			refs.put(page.rawPage().id(), new SoftReference<T>(page));
		} catch (Exception e) {
			return false;
		}
		
		return true;
	}
	
}
