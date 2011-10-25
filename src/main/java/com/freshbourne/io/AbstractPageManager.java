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

import com.google.common.collect.MapMaker;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Map;

public abstract class AbstractPageManager<T extends ComplexPage> implements PageManager<T> {

	private static final Logger LOG = Logger.getLogger(AbstractPageManager.class);
	private final PageManager<RawPage> rpm;
	private Map<Integer, T> cache = new MapMaker().weakValues().makeMap();

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

		if (cache.containsKey(id)) {
			result = cache.get(id);
			return result;
		}

		result = createObjectPage(rpm.getPage(id));

		try {
			result.load();
			cache.put(id, result);
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

		cache.put(l.rawPage().id(), l);

		if (LOG.isDebugEnabled())
			LOG.debug("node created: type: \t" + l.getClass().getSimpleName().toString() + "\tid: " + l.rawPage().id());
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

		if (cache.containsKey(id))
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
