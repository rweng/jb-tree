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

import com.google.common.cache.*;
import com.sun.tools.internal.xjc.reader.xmlschema.parser.CustomizationContextChecker;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This class caches RawPages coming from a ResourceManager. Through the caching, we can ensure that pages are written
 * back to the ResourceManager even if they are not explicitly persisted in the using class.
 * <p/>
 * CachedResourceManager works with all kinds of ResourceManagers, although usually used with a {@link
 * FileResourceManager}
 */
public class CachedResourceManager implements AutoSaveResourceManager {

	private final ResourceManager         rm;
	private final Cache<Integer, RawPage> cache;
	private final int                     cacheSize;

	CachedResourceManager(ResourceManager _rm, int cacheSize) {
		checkNotNull(_rm);
		checkArgument(cacheSize > 0, "cacheSize must be > 0");
		
		this.rm = _rm;
		this.cacheSize = cacheSize;
		this.cache = CacheBuilder.newBuilder().maximumSize(cacheSize)
				.removalListener(new RemovalListener<Integer, RawPage>() {
					@Override
					public void onRemoval(RemovalNotification<Integer, RawPage> integerRawPageRemovalNotification) {
						RawPage rawPage = integerRawPageRemovalNotification.getValue();
						rawPage.sync();
					}
				})
				.build(new CacheLoader<Integer, RawPage>() {
					@Override public RawPage load(Integer key) throws Exception {
						return rm.getPage(key);
					}
				});

	}

	public int getCacheSize() {
		return cacheSize;
	}

	@Override public void writePage(RawPage page) {
		rm.writePage(page);
	}

	@Override public RawPage addPage(RawPage page) {
		return rm.addPage(page);
	}

	@Override public int getPageSize() {
		return rm.getPageSize();
	}

	@Override public void open() throws IOException {
		rm.open();
	}

	@Override public boolean isOpen() {
		return rm.isOpen();
	}

	@Override public void close() throws IOException {
		sync();
		cache.invalidateAll();
		rm.close();
	}

	@Override public int numberOfPages() {
		return rm.numberOfPages();
	}

	@Override public void clear() {
		cache.invalidateAll();
		rm.clear();
	}

	@Override public RawPage createPage() {
		RawPage page = rm.createPage();
		cache.asMap().put(page.id(), page);
		return page;
	}

	@Override public RawPage getPage(int id) {
		try {
			return cache.get(id);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	@Override public void removePage(int id) {
		cache.invalidate(id);
		rm.removePage(id);
	}

	@Override public boolean hasPage(int id) {
		return rm.hasPage(id);
	}

	@Override public void sync() {
		for (RawPage p : cache.asMap().values()) {
			p.sync();
		}

		rm.sync();
	}

	public ResourceManager getResourceManager() {
		return rm;
	}

	public Cache<Integer, RawPage> getCache() {
		return cache;
	}
}
