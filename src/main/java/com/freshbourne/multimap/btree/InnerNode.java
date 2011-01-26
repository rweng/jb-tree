/**
 * Copyright (C) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.multimap.btree;

import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.List;

import com.freshbourne.io.ComplexPage;
import com.freshbourne.io.PagePointer;
import com.freshbourne.io.RawPage;
import com.freshbourne.serializer.FixLengthSerializer;

public class InnerNode<K, V> implements Node<K,V>, ComplexPage {

	
	private final RawPage rawPage;
	private final Comparator<K> comperator;
	private final FixLengthSerializer<PagePointer, byte[]> pointerSerializer;
	
	private int numberOfKeys = 0;
	private boolean valid = false;
	
	InnerNode(RawPage rawPage, FixLengthSerializer<PagePointer, byte[]> pointerSerializer,
			Comparator<K> comparator){
		this.rawPage = rawPage;
		this.comperator = comparator;
		this.pointerSerializer = pointerSerializer;
	}
	
	public void initRootState(PagePointer keyPointer, Long pageId1, Long pageId2){
		buffer().position(headerSize());
		buffer().putLong(pageId1);
		buffer().put(pointerSerializer.serialize(keyPointer));
		buffer().putLong(pageId2);
		
		numberOfKeys = 1;
		writeNumberOfKeys();
	}
	
	private static int headerSize() {
		return Integer.SIZE / 8;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#getNumberOfEntries()
	 */
	@Override
	public int getNumberOfEntries() {
		ensureValid();

		throw new UnsupportedOperationException();
		
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#containsKey(java.lang.Object)
	 */
	@Override
	public boolean containsKey(K key) {
		ensureValid();

		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#get(java.lang.Object)
	 */
	@Override
	public List<V> get(K key) {
		ensureValid();
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#remove(java.lang.Object)
	 */
	@Override
	public int remove(K key) {
		ensureValid();
		getChildForKey(key);

		throw new UnsupportedOperationException();
	}
	
	
	private void getChildForKey(K key) {
		
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#remove(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void remove(K key, V value) {
		ensureValid();

		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#clear()
	 */
	@Override
	public void destroy() {
		ensureValid();

		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.ComplexPage#initialize()
	 */
	@Override
	public void initialize() {
		numberOfKeys = 0;
		writeNumberOfKeys();
		valid = true;
	}
	
	private void writeNumberOfKeys(){
		buffer().position(0);
		buffer().putInt(numberOfKeys);
	}
	
	private ByteBuffer buffer(){
		return rawPage.buffer();
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.ComplexPage#load()
	 */
	@Override
	public void load() {
		valid = true;
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.ComplexPage#isValid()
	 */
	@Override
	public boolean isValid() {
		return valid;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.ComplexPage#rawPage()
	 */
	@Override
	public RawPage rawPage() {
		return rawPage;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.btree.Node#insert(java.lang.Object, java.lang.Object)
	 */
	@Override
	public AdjustmentAction<K, V> insert(K key, V value) {
		ensureValid();

		throw new UnsupportedOperationException();
	}
	
	private void ensureValid(){
		if(!isValid()){
			throw new IllegalStateException("inner page with the id " + rawPage().id() + " not valid!");
		}
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.btree.Node#getKeyPointer(int)
	 */
	@Override
	public PagePointer getKeyPointer(int pos) {
		ensureValid();
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.btree.Node#getId()
	 */
	@Override
	public Long getId() {
		return rawPage.id();
	}
}
