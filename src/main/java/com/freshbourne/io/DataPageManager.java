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

import java.io.IOException;

public class DataPageManager<T> implements PageManager<DataPage<T>> {

    private final BufferPoolManager bpm;
    private final FixLengthSerializer<PagePointer, byte[]> pointSerializer;
    private final Serializer<T, byte[]> dataSerializer;

    @Inject
    DataPageManager(
            BufferPoolManager bpm,
            FixLengthSerializer<PagePointer, byte[]> pointSerializer,
			Serializer<T, byte[]> dataSerializer
            ){
        this.bpm = bpm;
        this.pointSerializer = pointSerializer;
        this.dataSerializer = dataSerializer;
    }

    @Override
    public DataPage<T> createPage() throws IOException {
        return new DynamicDataPage<T>(bpm.createPage(), pointSerializer, dataSerializer);
    }

    @Override
    public DataPage<T> getPage(int id) throws IOException {
        return new DynamicDataPage<T>(bpm.getPage(id), pointSerializer, dataSerializer);
    }

    @Override
    public void removePage(int id) {
        bpm.removePage(id);
    }
}
