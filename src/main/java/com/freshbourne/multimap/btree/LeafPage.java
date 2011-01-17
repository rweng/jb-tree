/*
 * Copyright (c) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 */

/**
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 * 
 * (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */
package com.freshbourne.multimap.btree;

import com.freshbourne.io.*;
import com.freshbourne.serializer.FixLengthSerializer;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * This B-Tree-Leaf stores entries by storing the keys and values in seperate pages
 * and keeping track only of the pageId and offset.
 * 
 * @author "Robin Wenglewski <robin@wenglewski.de>"
 *
 * @param <K> KeyType
 * @param <V> ValueType
 */
public class LeafPage<K,V> extends RawPage implements Node<K,V> {
	
	private FixLengthSerializer<PagePointer, byte[]> pointerSerializer;
	
	private DataPageManager<K> keyPageManager;
	private DataPageManager<V> valuePageManager;
	
	// right now, we always store key/value pairs. If the entries are not unique,
	// it could make sense to store the key once with references to all values
	//TODO: investigate if we should do this
	private  int serializedPointerSize;
	private  int maxEntries;
	
	// counters
	private int numberOfEntries = 0;
	
	private int lastKeyPageId = -1;
	private int lastKeyPageRemainingBytes = -1;
	
	private int lastValuePageId = -1;
	private int lastValuePageRemainingBytes = -1;	

    LeafPage(
			RawPage page,
            DataPageManager<K> keyPageManager,
			DataPageManager<V> valuePageManager,
			FixLengthSerializer<PagePointer, byte[]> pointerSerializer
			){
    this(page.buffer(), page.resourceManager(), page.id(), keyPageManager, valuePageManager, pointerSerializer);
}


	//TODO: ensure that the pointerSerializer always creates the same (buffer-)size!
	LeafPage(
			ByteBuffer buffer,
            ResourceManager rm,
            Integer pageId,
            DataPageManager<K> keyPageManager,
			DataPageManager<V> valuePageManager,
			FixLengthSerializer<PagePointer, byte[]> pointerSerializer
			){
		super(buffer, rm, pageId);
		this.keyPageManager = keyPageManager;
		this.valuePageManager = valuePageManager;
		this.pointerSerializer = pointerSerializer;
		
		this.serializedPointerSize = pointerSerializer.serializedLength(PagePointer.class);
		
		// one pointer to key, one to value
		maxEntries = buffer.capacity() / (serializedPointerSize * 2); 
	}
	
	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#add(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void add(K key, V value) throws Exception {
		if(numberOfEntries == maxEntries) {
            throw new NodeFullException(this);
        }
		
		// add to data_page
		PagePointer keyPointer = storeKey(key);
		PagePointer valuePointer = storeValue(value);
		

        // serialize Pointers
        buffer().position(numberOfEntries * serializedPointerSize * 2);
        buffer().put(pointerSerializer.serialize(keyPointer));
        buffer().put(pointerSerializer.serialize(valuePointer));
        numberOfEntries++;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.btree.Node#size()
	 */
	@Override
	public int size() {
		return numberOfEntries;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#containsKey(java.lang.Object)
	 */
	@Override
	public boolean containsKey(K key) throws Exception {
        byte[] buf = new byte[serializedPointerSize];
		
		// iterate over the entries
		for(int i = 0; i < numberOfEntries; i++){

            // fetch the pagepoint binery
            buffer().position(posOfKey(i));
            buffer().get(buf);

            // get the key where the pagepointer is pointing to
            PagePointer pointer = pointerSerializer.deserialize(buf);
            DataPage<K> page = keyPageManager.getPage(pointer.getId());
            K currentKey = page.get(pointer.getOffset());
            if(key.equals(currentKey))
                return true;
        }
        return false;
	}
	
	private int posOfKey(int i){
		return i * serializedPointerSize * 2;
	}
	
	private int posOfValue(int i){
		return posOfKey(i) + serializedPointerSize;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#getFirst(java.lang.Object)
	 */
	@Override
	public V getFirst(K key) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#get(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<V> get(K key) throws Exception {
		List<V> result = new ArrayList<V>();
		
		ByteBuffer buf = buffer();
		byte[] bytebuf = new byte[pointerSerializer.serializedLength(PagePointer.class)];
		
		int pos = posOfKey(key);
		if( pos == -1)
			return result;
		buf.position(pos);
		
		// read first
		buf.get(bytebuf);
		PagePointer p = pointerSerializer.deserialize(bytebuf);
		DataPage<K> dataPage = keyPageManager.getPage(p.getId());
		
		while (dataPage.get(p.getOffset()).equals(key)) {
			buf.get(bytebuf);
			p = pointerSerializer.deserialize(bytebuf);
			DataPage<V> valueDataPage = valuePageManager.getPage(p.getId());

			result.add(valueDataPage.get(p.getOffset()));
			
			// TODO: read next, if next
			if(true)
				return result;
			
			buf.get(bytebuf);
			p = pointerSerializer.deserialize(bytebuf);
			dataPage = keyPageManager.getPage(p.getId());
			
		}
		return result;
	}
	
	/**
	 * @param key
	 * @return position to set the buffer to, where the key starts, -1 if key not found 
	 * @throws Exception 
	 */
	private int posOfKey(K key) throws Exception{
		ByteBuffer buf = buffer();
		int pSize = pointerSerializer.serializedLength(PagePointer.class);
		byte[] bytebuf = new byte[pSize];
		
		buf.position(0);
		
		for(int i = 0; i < numberOfEntries; i++){
			buf.get(bytebuf);
			PagePointer p = pointerSerializer.deserialize(bytebuf);
			DataPage<K> dataPage = keyPageManager.getPage(p.getId());
			if(dataPage.get(p.getOffset()).equals(key)){
				return buf.position() - pSize;
			}
		}
		return -1;
	}


	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#remove(java.lang.Object)
	 */
	@Override
	public List<V> remove(K key) throws Exception {
		List<V> result = new ArrayList<V>();
		
		for(V value : get(key)){
			result.add(remove(key, value));
		}
		
		return result;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#remove(java.lang.Object, java.lang.Object)
	 */
	@Override
	public V remove(K key, V value) {
		DataPage<K> kPage = getKeyDataPage(key);
		DataPage<V> vPage = getValueDataPage(key);
		
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param key
	 * @return
	 */
	private DataPage<V> getValueDataPage(K key) {
		// TODO Auto-generated method stub
		return null;
	}


	/**
	 * @param key
	 * @return
	 */
	private DataPage<K> getKeyDataPage(K key) {
		// TODO Auto-generated method stub
		return null;
	}


	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#clear()
	 */
	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @return the maximal number of Entries
	 */
	public int getMaxEntries() {
		return maxEntries;
	}

	private PagePointer storeKey(K key) throws Exception{
		DataPage<K> page = keyPageManager.createPage();
		page.initialize();
		return new PagePointer(id(),page.add(key));
	}
	
	private PagePointer storeValue(V value) throws Exception {
        DataPage<V> page = valuePageManager.createPage();
        page.initialize();
        return new PagePointer(id(),page.add(value));
	}
}
