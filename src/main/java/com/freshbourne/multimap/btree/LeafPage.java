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
public class LeafPage<K,V> implements Node<K,V>, ComplexPage {
	
	private FixLengthSerializer<PagePointer, byte[]> pointerSerializer;
	
	private DataPageManager<K> keyPageManager;
	private DataPageManager<V> valuePageManager;
	
	private boolean valid = false;
	
	// right now, we always store key/value pairs. If the entries are not unique,
	// it could make sense to store the key once with references to all values
	//TODO: investigate if we should do this
	private  int serializedPointerSize;
	private  int maxEntries;
	
	
	private static final int NOT_FOUND = -1;
	
	private final RawPage rawPage;
	
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
    	
    	this.rawPage = page;
		this.keyPageManager = keyPageManager;
		this.valuePageManager = valuePageManager;
		this.pointerSerializer = pointerSerializer;
		
		this.serializedPointerSize = pointerSerializer.serializedLength(PagePointer.class);
		
		// one pointer to key, one to value
		maxEntries = (rawPage.buffer().capacity() - headerSize()) / (serializedPointerSize * 2); 
	}
    
    public boolean isFull(){
    	return numberOfEntries == maxEntries;
    }
	
	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#add(java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean add(K key, V value) {
		ensureValid();
		if(isFull())
			return false;
		
		// add to data_page
		PagePointer keyPointer = storeKey(key);
		PagePointer valuePointer = storeValue(value);
		

        // serialize Pointers
		addEntry(keyPointer, valuePointer);
		
		writeHeader();
		return true;
	}
	
	private void ensureValid() {
		if( isValid() )
			return;
		
		System.err.println("The current LeafPage with the id " + rawPage.id() + " is not valid");
		System.exit(1);
	}

	/**
	 * @param keyPointer
	 * @param valuePointer
	 */
	private void addEntry(PagePointer keyPointer, PagePointer valuePointer) {
		rawPage.buffer().position(posBehindLastEntry());
		
		rawPage.buffer().put(pointerSerializer.serialize(keyPointer));
		rawPage.buffer().put(pointerSerializer.serialize(valuePointer));
        
		numberOfEntries++;
	}
	
	private int headerSize(){
		return Integer.SIZE / 8;
	}

	private void writeHeader() {
		buffer().position(0);
		buffer().putInt(numberOfEntries);
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
        return posOfKey(key) != NOT_FOUND;
	}
	
	private int posOfKey(int i){
		return headerSize() + i * serializedPointerSize * 2;
	}
	
	private int posOfValue(int i){
		return posOfKey(i) + serializedPointerSize;
	}
	
	/**
	 * does not alter the header bytebuffer
	 * @return
	 */
	private int posBehindLastEntry(){
		return headerSize() + numberOfEntries * serializedPointerSize * 2;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#get(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<V> get(K key) throws Exception {
		List<V> result = new ArrayList<V>();
		
		byte[] bytebuf = new byte[pointerSerializer.serializedLength(PagePointer.class)];
		
		int pos = posOfKey(key);
		if( pos == NOT_FOUND)
			return result;
		
		int oldPos = buffer().position();
		buffer().position(pos);
		
		// read first
		buffer().get(bytebuf);
		PagePointer p = pointerSerializer.deserialize(bytebuf);
		DataPage<K> dataPage = keyPageManager.getPage(p.getId());
		
		while (dataPage.get(p.getOffset()).equals(key)) {
			buffer().get(bytebuf);
			p = pointerSerializer.deserialize(bytebuf);
			DataPage<V> valueDataPage = valuePageManager.getPage(p.getId());

			result.add(valueDataPage.get(p.getOffset()));
			
			// if no other entries are available, return
			if(buffer().position() >= posBehindLastEntry())
				break;
			
			// otherwise check, if the next entry also has this key
			buffer().get(bytebuf);
			p = pointerSerializer.deserialize(bytebuf);
			dataPage = keyPageManager.getPage(p.getId());
			
		}
		buffer().position(oldPos);
		return result;
	}
	
	private ByteBuffer buffer(){return rawPage.buffer();}
	
	/**
	 * @param key
	 * @return position to set the buffer to, where the key starts, -1 if key not found 
	 * @throws Exception 
	 */
	private int posOfKey(K key) throws Exception{
		int pSize = pointerSerializer.serializedLength(PagePointer.class);
		byte[] bytebuf = new byte[pSize];

		buffer().position(headerSize());

		for(int i = 0; i < numberOfEntries; i++){

			buffer().get(bytebuf);
			PagePointer p = pointerSerializer.deserialize(bytebuf);
			DataPage<K> dataPage = keyPageManager.getPage(p.getId());
			if(dataPage.get(p.getOffset()).equals(key)){
				return buffer().position() - pSize;
			}

			// get the data pointer but do nothing with it
			buffer().get(bytebuf);
		}
		return NOT_FOUND;
	}


	/**
	 * @param currentPos
	 * @return a valid position to read the next key from, or NOT_FOUND
	 */
	private int getPosWhereNextKeyStarts(int currentPos) {
		if(currentPos < headerSize())
			currentPos = headerSize();
		
		currentPos -= headerSize();
		currentPos /= (serializedPointerSize * 2);
		if(currentPos >= numberOfEntries)
			return NOT_FOUND;
		
		currentPos *= (serializedPointerSize * 2);
		return currentPos + headerSize();
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#remove(java.lang.Object)
	 */
	@Override
	public void remove(K key) throws Exception {
		int pos = posOfKey(key);
		if(pos == NOT_FOUND)
			return;
		
		int numberOfValues = get(key).size();
		int sizeOfValues = numberOfValues * serializedPointerSize * 2;
		
		//TODO: free key and value pages
		
		// shift the pointers after key
		System.arraycopy(buffer().array(), pos + sizeOfValues , buffer().array(), pos , buffer().array().length - pos - sizeOfValues);
		numberOfEntries -= numberOfValues;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#remove(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void remove(K key, V value) throws Exception {
		int pos = posOfKey(key);
		if(pos == NOT_FOUND)
			return;
		
		
		int numberOfValues = get(key).size();
		
		buffer().position(pos);
		int sizeOfValues = numberOfValues * serializedPointerSize * 2;
		byte[] buf1 = new byte[serializedPointerSize];
		byte[] buf2 = new byte[serializedPointerSize];
		
		List<Integer> toRemove = new ArrayList<Integer>();
		for(int i = 0; i < numberOfValues; i++){
			buffer().get(buf1);
			buffer().get(buf2); // load only the value
			PagePointer p = pointerSerializer.deserialize(buf2);
			DataPage<V> vPage = valuePageManager.getPage(p.getId());
			V val = vPage.get(p.getOffset());
			if( val != null && val.equals(value)){
				vPage.remove(p.getOffset());
				if(vPage.numberOfEntries() == 0)
					valuePageManager.removePage(vPage.rawPage().id());
				
				// also free value page
				p = pointerSerializer.deserialize(buf2);
				DataPage <K> kPage = keyPageManager.getPage(p.getId());
				kPage.remove(p.getOffset());
				if(kPage.numberOfEntries() == 0)
					keyPageManager.removePage(p.getId());
				
				// move pointers forward and reset buffer
				int startingPos = buffer().position() - buf1.length - buf2.length;
				System.arraycopy(buffer().array(), buffer().position(), buffer().array(), startingPos, posBehindLastEntry() - buffer().position());
				
				buffer().position(startingPos);
				
			}
			
		}
		
		
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
	public void clear() throws Exception {
		byte[] buf = new byte[serializedPointerSize];
		
		//TODO: free pages
		for(int i = 0; i < numberOfEntries; i++){
			
			// key page
			buffer().position(posOfKey(i));
			buffer().get(buf);
			PagePointer p = pointerSerializer.deserialize(buf);
			DataPage<K> keyPage = keyPageManager.getPage(p.getId());
			keyPage.remove(p.getOffset());
			if(keyPage.numberOfEntries() == 0)
				keyPageManager.removePage(keyPage.rawPage().id());
			
			// data page
			buffer().get(buf);
			p = pointerSerializer.deserialize(buf);
			DataPage<V> valuePage = valuePageManager.getPage(p.getId());
			valuePage.remove(p.getOffset());
			if(valuePage.numberOfEntries() == 0)
				valuePageManager.removePage(valuePage.rawPage().id());
			
		}
		
		numberOfEntries = 0;
		writeHeader();
	}

	/**
	 * @return the maximal number of Entries
	 */
	public int getMaxEntries() {
		return maxEntries;
	}

	private PagePointer storeKey(K key) {
		DataPage<K> page = keyPageManager.createPage();
		return new PagePointer(page.rawPage().id(),page.add(key));
	}
	
	private PagePointer storeValue(V value) {
        DataPage<V> page = valuePageManager.createPage();
        return new PagePointer(page.rawPage().id(),page.add(value));
	}


	/* (non-Javadoc)
	 * @see com.freshbourne.io.ComplexPage#initialize()
	 */
	@Override
	public void initialize() {
		numberOfEntries = 0;
		writeHeader();
		valid = true;
	}


	/* (non-Javadoc)
	 * @see com.freshbourne.io.ComplexPage#load()
	 */
	@Override
	public void load() {
		buffer().position(0);
		numberOfEntries = buffer().getInt();
		valid = true;
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
}
