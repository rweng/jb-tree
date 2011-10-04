/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */
package com.freshbourne.btree;

import com.freshbourne.comparator.IntegerComparator;
import com.freshbourne.comparator.StringComparator;
import com.freshbourne.io.DynamicDataPage;
import com.freshbourne.io.IOModule;
import com.freshbourne.io.PagePointer;
import com.freshbourne.serializer.*;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;

import java.io.File;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Comparator;


public class BTreeModule extends AbstractModule {

    private String indexFile;
    private Long   bTreePageId;

    public BTreeModule() {
        this(null, null);
    }

    public BTreeModule(String indexFile) {
        this(indexFile, null);
    }

    public BTreeModule(String indexFile, Long bTreePageId) {
        this.indexFile = indexFile;
        this.bTreePageId = bTreePageId;
    }

    public void setIndexFile(String indexFile) {
        this.indexFile = indexFile;
    }

    /* (non-Javadoc)
      * @see com.google.inject.AbstractModule#configure()
      */
    @Override
    protected void configure() {
        bind(new TypeLiteral<FixLengthSerializer<PagePointer, byte[]>>() {
        }).
                toInstance(PagePointSerializer.INSTANCE);

        bind(new TypeLiteral<FixLengthSerializer<Integer, byte[]>>() {
        }).
                toInstance(IntegerSerializer.INSTANCE);
        bind(new TypeLiteral<Serializer<Integer, byte[]>>() {
        }).
                toInstance(IntegerSerializer.INSTANCE);


        bind(new TypeLiteral<Serializer<String, byte[]>>() {
        }).toInstance(FixedStringSerializer.INSTANCE);
        bind(new TypeLiteral<FixLengthSerializer<String, byte[]>>() {
        }).toInstance(FixedStringSerializer.INSTANCE);

        bind(new TypeLiteral<Comparator<Integer>>() {
        }).toInstance(IntegerComparator.INSTANCE);
        bind(new TypeLiteral<Comparator<String>>() {
        }).toInstance(StringComparator.INSTANCE);

        IOModule module = new IOModule();

        if (indexFile != null)
            module.setFile(new File(indexFile));

        binder().install(module);

        bind(new TypeLiteral<BTree<Integer, String>>() {
        });
    }


    @SuppressWarnings("unchecked")
    static <T> TypeLiteral<DynamicDataPage<T>> pageOf(final Class<T> parameterType) {
        return (TypeLiteral<DynamicDataPage<T>>) TypeLiteral.get(new ParameterizedType() {

            @Override
            public Type getRawType() {
                return DynamicDataPage.class;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }

            @Override
            public Type[] getActualTypeArguments() {
                return new Type[]{parameterType};
            }
        });
    }


}
