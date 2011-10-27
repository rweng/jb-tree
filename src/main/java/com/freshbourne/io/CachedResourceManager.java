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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class CachedResourceManager implements ResourceManager {

	private final ResourceManager rm;
	private final Cache<Integer, RawPage> cache;

	CachedResourceManager(ResourceManager _rm, int cacheSize){
		this.rm = _rm;
		this.cache = CacheBuilder.newBuilder().maximumSize(cacheSize).softValues().build(new CacheLoader<Integer, RawPage>() {
			@Override public RawPage load(Integer key) throws Exception {
				return rm.getPage(key);
			}
		});
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
		rm.close();
	}

	@Override public int numberOfPages() {
		return rm.numberOfPages();
	}

	@Override public RawPage createPage() {
		return rm.createPage();
	}

	@Override public RawPage getPage(int id) {
		try {
			return cache.get(id);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	@Override public void removePage(int id) {
		rm.removePage(id);
	}

	@Override public boolean hasPage(int id) {
		return rm.hasPage(id);
	}

	@Override public void sync() {
		rm.sync();
	}

	public ResourceManager getResourceManager() {
		return rm;
	}
}
