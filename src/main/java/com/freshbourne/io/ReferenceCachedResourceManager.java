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

import com.google.common.base.Objects;
import com.google.common.collect.MapMaker;

import java.io.IOException;
import java.util.concurrent.ConcurrentMap;

/**
 * This class uses a map with weakReferences to alway return the same instance if the instance is still
 * in memory.
 */
public class ReferenceCachedResourceManager implements ResourceManager {

	private ResourceManager rm;
	private ConcurrentMap<Integer, RawPage> map;

	ReferenceCachedResourceManager(ResourceManager rm){
		this.rm = rm;
		this.map = new MapMaker().weakValues().makeMap();
	}

	@Override public Integer getPageSize() {
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
		map.clear();
	}

	@Override public int numberOfPages() {
		return rm.numberOfPages();
	}

	@Override public void clear() {
		rm.clear();
		map.clear();
	}

	@Override public void writePage(RawPage page) {
		rm.writePage(page);
	}

	@Override public RawPage createPage() {
		RawPage page = rm.createPage();
		map.put(page.id(), page);
		return page;
	}

	@Override public RawPage getPage(int id) {
		RawPage page = map.get(id);
		if(page != null)
			return page;

		page = rm.getPage(id);
		map.put(page.id(), page);
		return page;
	}

	@Override public void removePage(int id) {
		rm.removePage(id);
		map.remove(id);
	}

	@Override public boolean hasPage(int id) {
		return rm.hasPage(id);
	}

	public String toString(){
		return Objects.toStringHelper(this)
				.add("resourceManager", rm)
				.add("mapSize", map.size())
				.toString();
	}
}
