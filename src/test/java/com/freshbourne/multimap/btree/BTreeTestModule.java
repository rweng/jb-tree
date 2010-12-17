/**
 * Copyright (C) 2010 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.multimap.btree;

import com.freshbourne.io.FixLengthSerializer;
import com.freshbourne.io.IntegerSerializer;
import com.freshbourne.io.PagePointSerializer;
import com.freshbourne.io.PagePointer;
import com.freshbourne.io.Serializer;
import com.freshbourne.io.StringSerializer;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;

public class BTreeTestModule extends AbstractModule {
	
	/* (non-Javadoc)
	 * @see com.google.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(LeafNode.class);
		bind(new TypeLiteral<Serializer<String, byte[]>>(){}).to(StringSerializer.class);
		bind(new TypeLiteral<Serializer<Integer, byte[]>>(){}).to(IntegerSerializer.class);
		bind(new TypeLiteral<FixLengthSerializer<PagePointer, byte[]>>(){}).to(PagePointSerializer.class);	
	}
}
