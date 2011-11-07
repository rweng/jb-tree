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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

public abstract class AbstractPageManager<T extends ComplexPage> implements PageManager<T> {

	private static final Log LOG = LogFactory.getLog(AbstractPageManager.class);
	private final PageManager<RawPage> rpm;

	protected AbstractPageManager(final PageManager<RawPage> rpm) {
		this.rpm = rpm;
	}

	protected PageManager<RawPage> getRawPageManager() {
		return rpm;
	}

	public boolean hasRawPageManager(final PageManager<RawPage> rpm) {
		return this.rpm.equals(rpm);
	}

	/* (non-Javadoc)
		 * @see com.freshbourne.io.PageManager#getPage(int)
		 */
	@Override
	public T getPage(final int id) {
		final T result;


		final RawPage page = rpm.getPage(id);
		result = createObjectPage(page);

		try {
			result.load();
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

	public T createPage(final boolean initialize) {
		final T l = createObjectPage(rpm.createPage());

		if (initialize)
			try {
				l.initialize();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

		if (LOG.isDebugEnabled())
			LOG.debug("node created: type: \t" + l.getClass().getSimpleName().toString() + "\tid: " + l.rawPage().id());

		return l;
	}


	/* (non-Javadoc)
		 * @see com.freshbourne.io.PageManager#removePage(int)
		 */
	@Override
	public void removePage(final int id) {
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
	public boolean hasPage(final int id) {
		if (!rpm.hasPage(id))
			return false;

		try {
			getPage(id);
			return true;
		} catch (Exception e) {
			return false;
		}
	}


	@Override public void writePage(T page) {
		rpm.writePage(page.rawPage());
	}
}
