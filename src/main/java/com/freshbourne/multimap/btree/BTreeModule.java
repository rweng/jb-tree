/**
 * Copyright (C) 2010 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.multimap.btree;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.freshbourne.io.DynamicDataPage;
import com.freshbourne.io.FixLengthSerializer;
import com.freshbourne.io.IntegerSerializer;
import com.freshbourne.io.PagePointer;
import com.freshbourne.io.Serializer;
import com.freshbourne.io.StringSerializer;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;

public class BTreeModule extends AbstractModule {

	/* (non-Javadoc)
	 * @see com.google.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(pageOf(String.class));
		bind(pageOf(Integer.class));
		bind(new TypeLiteral<FixLengthSerializer<PagePointer, byte[]>>(){});
		bind(new TypeLiteral<Serializer<Integer, byte[]>>(){}).to(
				IntegerSerializer.class);
		bind(new TypeLiteral<Serializer<String, byte[]>>(){}).to(
				StringSerializer.class);
		
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
