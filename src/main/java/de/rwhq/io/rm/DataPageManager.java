/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 *
 * http://creativecommons.org/licenses/by-nc/3.0/
 *
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2011 "Robin Wenglewski <robin@wenglewski.de>"
 */

package de.rwhq.io.rm;

import de.rwhq.serializer.FixLengthSerializer;
import de.rwhq.serializer.Serializer;

public class DataPageManager<T> extends AbstractPageManager<DataPage<T>> {

    private final PageManager<RawPage>                     bpm;
    private final FixLengthSerializer<PagePointer, byte[]> pointSerializer;
    private final Serializer<T, byte[]> dataSerializer;

    public DataPageManager(
    		final PageManager<RawPage> bpm,
            final FixLengthSerializer<PagePointer, byte[]> pointSerializer,
			final Serializer<T, byte[]> dataSerializer
            ){
    	super(bpm);
        this.bpm = bpm;
        this.pointSerializer = pointSerializer;
        this.dataSerializer = dataSerializer;
    }


	/* (non-Javadoc)
	 * @see PageManager#hasPage(long)
	 */
	@Override
	public boolean hasPage(final int id) {
		return bpm.hasPage(id);
	}
	
	/* (non-Javadoc)
	 * @see AbstractPageManager#createObjectPage(RawPage)
	 */
	@Override
	protected DataPage<T> createObjectPage(final RawPage page) {
		return new DynamicDataPage<T>(page, pointSerializer, dataSerializer);
	}
}
