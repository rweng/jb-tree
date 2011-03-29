/*
 * Copyright (c) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 */

package com.freshbourne.io;

import com.freshbourne.serializer.FixLengthSerializer;
import com.freshbourne.serializer.Serializer;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class DataPageManager<T> extends AbstractPageManager<DataPage<T>> {

    private final PageManager<RawPage> bpm;
    private final FixLengthSerializer<PagePointer, byte[]> pointSerializer;
    private final Serializer<T, byte[]> dataSerializer;

    @Inject
    DataPageManager(
    		PageManager<RawPage> bpm,
            FixLengthSerializer<PagePointer, byte[]> pointSerializer,
			Serializer<T, byte[]> dataSerializer
            ){
    	super(bpm);
        this.bpm = bpm;
        this.pointSerializer = pointSerializer;
        this.dataSerializer = dataSerializer;
    }


	/* (non-Javadoc)
	 * @see com.freshbourne.io.PageManager#hasPage(long)
	 */
	@Override
	public boolean hasPage(int id) {
		return bpm.hasPage(id);
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.PageManager#sync()
	 */
	@Override
	public void sync() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.AbstractPageManager#createObjectPage(com.freshbourne.io.RawPage)
	 */
	@Override
	protected DataPage<T> createObjectPage(RawPage page) {
		return new DynamicDataPage<T>(page, pointSerializer, dataSerializer);
	}
}
