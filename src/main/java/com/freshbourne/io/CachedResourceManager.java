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
import com.google.common.collect.MapMaker;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This class caches RawPages coming from a ResourceManager. Through the caching, we can ensure that pages are written
 * back to the ResourceManager even if they are not explicitly persisted in the using class.
 * <p/>
 * Additionally, if a Page is requested which is not in cache but still in Memory (meaning that maybe it's still being
 * used), then the memory instance is added to the cache and returned.
 * <p/>
 * CachedResourceManager works with all kinds of ResourceManagers, although usually used with a {@link
 * FileResourceManager}
 */
public class CachedResourceManager implements AutoSaveResourceManager {

	private final ResourceManager         rm;
	private final Cache<Integer, RawPage> cache;
	private final int                     cacheSize;
	private final Map<Integer, RawPage> weakMap = new MapMaker().weakValues().makeMap();

	CachedResourceManager(final ResourceManager _rm, final int cacheSize) {
		checkNotNull(_rm);
		checkArgument(cacheSize > 0, "cacheSize must be > 0");
		
		this.rm = _rm;
		this.cacheSize = cacheSize;
		this.cache = CacheBuilder.newBuilder().maximumSize(cacheSize)
				.removalListener(new RemovalListener<Integer, RawPage>() {
					@Override
					public void onRemoval(final RemovalNotification<Integer, RawPage> integerRawPageRemovalNotification) {
						final RawPage rawPage = integerRawPageRemovalNotification.getValue();
						rawPage.sync();
					}
				})
				.build(new CacheLoader<Integer, RawPage>() {
					@Override public RawPage load(final Integer key) throws Exception {
						if(weakMap.containsKey(key))
							return weakMap.get(key);

						final RawPage page = rm.getPage(key);
						weakMap.put(page.id(), page);
						return page;
					}
				});

	}

	public int getCacheSize() {
		return cacheSize;
	}

	@Override public void writePage(final RawPage page) {
		cache.asMap().put(page.id(), page);
		weakMap.put(page.id(), page);
		rm.writePage(page);
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
		weakMap.clear();
		rm.close();
	}

	@Override public int numberOfPages() {
		return rm.numberOfPages();
	}

	@Override public void clear() {
		cache.invalidateAll();
		weakMap.clear();
		rm.clear();
	}

	@Override public RawPage createPage() {
		final RawPage page = rm.createPage();
		cache.asMap().put(page.id(), page);
		weakMap.put(page.id(), page);
		return page;
	}

	@Override public RawPage getPage(final int id) {
		try {
			return cache.get(id);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	@Override public void removePage(final int id) {
		cache.invalidate(id);
		weakMap.remove(id);
		rm.removePage(id);
	}

	@Override public boolean hasPage(final int id) {
		return rm.hasPage(id);
	}

	public void sync() {
		for (final RawPage p : cache.asMap().values()) {
			p.sync();
		}

		for(final RawPage p : weakMap.values()){
			p.sync();
		}
	}

	public ResourceManager getResourceManager() {
		return rm;
	}

	public Cache<Integer, RawPage> getCache() {
		return cache;
	}
}
