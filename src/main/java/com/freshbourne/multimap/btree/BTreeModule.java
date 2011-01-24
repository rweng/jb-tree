/*
 * Copyright (c) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 */
package com.freshbourne.multimap.btree;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Comparator;

import com.freshbourne.comparator.IntegerComparator;
import com.freshbourne.io.DynamicDataPage;
import com.freshbourne.serializer.FixLengthSerializer;
import com.freshbourne.serializer.IntegerSerializer;
import com.freshbourne.serializer.PagePointSerializer;
import com.freshbourne.io.PagePointer;
import com.freshbourne.serializer.Serializer;
import com.freshbourne.serializer.StringSerializer;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;

public class BTreeModule extends AbstractModule {

	/* (non-Javadoc)
	 * @see com.google.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(new TypeLiteral<FixLengthSerializer<PagePointer, byte[]>>(){}).
			toInstance(PagePointSerializer.INSTANCE);
		
		bind(new TypeLiteral<Serializer<Integer, byte[]>>(){}).
			toInstance(IntegerSerializer.INSTANCE);
		
		bind(new TypeLiteral<Serializer<String, byte[]>>(){}).toInstance(StringSerializer.INSTANCE);
		
		bind(new TypeLiteral<BTree<Integer,String>>(){});
		
		bind(new TypeLiteral<Comparator<Integer>>(){}).toInstance(IntegerComparator.INSTANCE);
		
	}
	
	@SuppressWarnings("unchecked")
	static <T> TypeLiteral<DynamicDataPage<T>> pageOf(final Class<T> parameterType){
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
				return new Type[] {parameterType};
			}
		});
	}
	

}
