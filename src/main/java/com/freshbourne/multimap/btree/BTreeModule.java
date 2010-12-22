/**
 * Copyright (C) 2010 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.multimap.btree;

import java.lang.reflect.ParameterizedType;

import com.freshbourne.io.DynamicDataPage;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;

public class BTreeModule extends AbstractModule {

	/* (non-Javadoc)
	 * @see com.google.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(new TypeLiteral<DynamicDataPage<String>>(){});
	}
	
	protected void bindDynamicPageImpl(Class<?> clazz) { 
		//bind(new TypeLiteral<DynamicDataPage<clazz>>(){});
//		  ParameterizedType daoType = 
//		       Types.newParameterizedType(DynamicDataPage.class, clazz); 
//		  ParameterizedType daoImplType = 
//		       Types.newParameterizedType(DynamicDataPage.class, clazz); 
//		  bind(Key.get(daoType)).to(Key.get(daoImplType)); 

		} 

}
