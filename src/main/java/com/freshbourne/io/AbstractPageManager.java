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

import com.google.common.cache.CacheBuilder;
import com.google.common.collect.MapMaker;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Map;

/**
 * There is this special use-case when the same page is fetched twice from the PageManager. If we have no cache here,
 * at least the wrapping LeafNode (not the RawPage) will be a separate instance. If we then change one instance the
 * change isn't reflected in the second instance.
 * <p/>
 * There are basically two ways to avoid that: don't cache any values in the leafPage and always get the values
 * directly from the RawPage. Or we could cache here, too, so that both times the same instance is returned. Last
 * option seems better regarding performance.
 */
public abstract class AbstractPageManager<T extends ComplexPage> implements PageManager<T> {

	private static final Logger LOG = Logger.getLogger(AbstractPageManager.class);
	private final PageManager<RawPage> rpm;

	private final Map<Integer, T> cache = new MapMaker().weakValues().makeMap();

	protected AbstractPageManager(PageManager<RawPage> rpm) {
		this.rpm = rpm;
	}

	protected PageManager<RawPage> getRawPageManager() {
		return rpm;
	}

	public boolean hasRawPageManager(PageManager<RawPage> rpm) {
		return this.rpm.equals(rpm);
	}

	/* (non-Javadoc)
		 * @see com.freshbourne.io.PageManager#getPage(int)
		 */
	@Override
	public T getPage(int id) {
		T result;

		// just ensure here, that if rpm caches, it's in this cache, too
		RawPage page = rpm.getPage(id);

		if(cache.containsKey(id)){

			T t = cache.get(id);
			if(t.rawPage() == page){
				return t;
			} else {
				throw new IllegalStateException();
				// what, if it is a different RawPage instance, but the old RawPage might still be used somewhere?
			}
		}
		
		result = createObjectPage(page);

		try {
			result.load();
			cache.put(result.rawPage().id(), result);
		} catch (IOException e) {
			// if the page cannot be loaded, something is off.
			// we should only be able to fetch initialized pages from the rpm.
			throw new IllegalArgumentException("cant load InnerNodePage with id " + id);
		}

		return result;
	}

	/* (non-Javadoc)
		 * @see com.freshbourne.io.PageManager#createPage()
		 */
	@Override
	public T createPage() {
		return createPage(true);
	}

	public T createPage(boolean initialize) {
		T l = createObjectPage(rpm.createPage());

		if (initialize)
			try {
				l.initialize();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

		if (LOG.isDebugEnabled())
			LOG.debug("node created: type: \t" + l.getClass().getSimpleName().toString() + "\tid: " + l.rawPage().id());

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

	/**
	 * This method is a utility method since the dependencies for the concrete page creation are only available in the
	 * extensions of this AbstractPageManager
	 *
	 * @param page
	 * 		which should be initialized with the page-specific data
	 * @return a Complex Page
	 */
	protected abstract T createObjectPage(RawPage page);

	/* (non-Javadoc)
	 * @see com.freshbourne.io.PageManager#hasPage(long)
	 */
	@Override
	public boolean hasPage(int id) {
		if (!rpm.hasPage(id))
			return false;

		try {
			getPage(id);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/* (non-Javadoc)
		 * @see com.freshbourne.io.PageManager#sync()
		 */
	@Override
	public void sync() {
		getRawPageManager().sync();
	}
}
