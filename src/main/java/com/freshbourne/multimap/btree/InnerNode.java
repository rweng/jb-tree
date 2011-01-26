/**
 * Copyright (C) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.multimap.btree;

import java.io.IOException;
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
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#containsKey(java.lang.Object)
	 */
	@Override
	public boolean containsKey(K key) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#get(java.lang.Object)
	 */
	@Override
	public List<V> get(K key) throws IOException, Exception {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#add(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void add(K key, V value) {
		// TODO Auto-generated method stub
		return;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#remove(java.lang.Object)
	 */
	@Override
	public int remove(K key) throws Exception {
		getChildForKey(key);
		
		return 0;
	}
	
	
	private void getChildForKey(K key) {
		
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#remove(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void remove(K key, V value) throws Exception {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#clear()
	 */
	@Override
	public void clear() throws Exception {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.ComplexPage#initialize()
	 */
	@Override
	public void initialize() {
		numberOfKeys = 0;
		writeNumberOfKeys();
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
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.ComplexPage#isValid()
	 */
	@Override
	public boolean isValid() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.ComplexPage#rawPage()
	 */
	@Override
	public RawPage rawPage() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.btree.Node#insert(java.lang.Object, java.lang.Object)
	 */
	@Override
	public AdjustmentAction insert(K key, V value) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.btree.Node#getKeyPointer(int)
	 */
	@Override
	public PagePointer getKeyPointer(int pos) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.btree.Node#getId()
	 */
	@Override
	public Long getId() {
		return rawPage.id();
	}
}
