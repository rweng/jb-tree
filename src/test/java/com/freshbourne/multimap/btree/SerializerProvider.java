/**
 * Copyright (C) 2010 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.multimap.btree;

import com.freshbourne.io.Serializer;
import com.google.inject.Guice;
import com.google.inject.Provider;

public class SerializerProvider<InputType, ResultType> implements Provider<Serializer<InputType, ResultType>> {

	/* (non-Javadoc)
	 * @see com.google.inject.Provider#get()
	 */
	@Override
	public Serializer<InputType, ResultType> get() {
		
		return null;
	}

}
