/*
 * Copyright (c) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 */
package com.freshbourne.multimap.btree;

import com.freshbourne.io.*;
import com.freshbourne.multimap.btree.AdjustmentAction.ACTION;
import com.freshbourne.serializer.FixLengthSerializer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
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
	
	/**
	 * If a leaf page is less full than this factor, it may be target of operations
	 * where entries are moved from one page to another. 
	 */
	private static final float MAX_LEAF_ENTRY_FILL_LEVEL_TO_MOVE = 0.75f;

	
	private final FixLengthSerializer<PagePointer, byte[]> pointerSerializer;
	
	private DataPageManager<K> keyPageManager;
	private DataPageManager<V> valuePageManager;
	
	private final Comparator<K> comparator;
	
	private boolean valid = false;
	
	// right now, we always store key/value pairs. If the entries are not unique,
	// it could make sense to store the key once with references to all values
	//TODO: investigate if we should do this
	private  int serializedPointerSize;
	private  int maxEntries;
	
	
	private static final int NOT_FOUND = -1;
	private static final Long NO_NEXT_LEAF = 0L;
	
	private final RawPage rawPage;
	
	// counters
	private int numberOfEntries = 0;
	
	private Long lastKeyPageId = null;
	private int lastKeyPageRemainingBytes = -1;
	
	private Long lastValuePageId = null;
	private int lastValuePageRemainingBytes = -1;
	private final PageManager<LeafPage<K,V>> leafPageManager;
	
	private BTree<K, V> tree;
	
	LeafPage(
			RawPage page,
            DataPageManager<K> keyPageManager,
			DataPageManager<V> valuePageManager,
			FixLengthSerializer<PagePointer, byte[]> pointerSerializer,
			Comparator<K> comparator,
			PageManager<LeafPage<K,V>> leafPageManager
			){
    	this.leafPageManager = leafPageManager;
    	this.rawPage = page;
		this.keyPageManager = keyPageManager;
		this.valuePageManager = valuePageManager;
		this.pointerSerializer = pointerSerializer;
		this.comparator = comparator;
		
		this.serializedPointerSize = pointerSerializer.serializedLength(PagePointer.class);
		
		// one pointer to key, one to value
		maxEntries = (rawPage.buffer().capacity() - headerSize()) / (serializedPointerSize * 2); 
	}
	
	/**
	 * If a leaf page has at least that many free slots left, we can move pointers to it
	 * from another node. This number is computed from the
	 * <tt>MAX_LEAF_ENTRY_FILL_LEVEL_TO_MOVE</tt> constant.
	 */
	private int getMinFreeLeafEntriesToMove(){
		return (int) (getMaxEntries() *
                (1 - MAX_LEAF_ENTRY_FILL_LEVEL_TO_MOVE)) + 2;
	}
    
    public boolean isFull(){
    	return numberOfEntries == maxEntries;
    }
    
    
	
	
	private void ensureValid() {
		if( isValid() )
			return;
		
		System.err.println("The current LeafPage with the id " + rawPage.id() + " is not valid");
		System.exit(1);
	}
	
	public void prependEntriesFromOtherPage(LeafPage<K, V> source, int num){
		
		// checks
		if(num < 0)
			throw new IllegalArgumentException("num must be > 0");
		
		if(num > source.getNumberOfEntries())
			throw new IllegalArgumentException("the source leaf has not enough entries");
		
		if(getNumberOfEntries() + num > maxEntries)
			throw new IllegalArgumentException("not enough space in this leaf to prepend " + num + " entries from other leaf");
		
		if(getNumberOfEntries() > 0 && comparator.compare(source.getLastKey(), getFirstKey()) > 0)
			throw new IllegalArgumentException("the last key of the provided source leaf is larger than this leafs first key");
		
		// make space in this leaf, move all elements to the right
		int totalSize = num * (serializedPointerSize * 2);
		System.arraycopy(buffer().array(), headerSize(), buffer().array(), headerSize() + totalSize, totalSize);
		
		// copy from other to us
		System.arraycopy(source.rawPage().buffer().array(), source.posOfKey(source.getNumberOfEntries() - num), buffer().array(), headerSize(), totalSize);
		
		// update headers, also sets modified
		source.setNumberOfEntries(source.getNumberOfEntries() - num);
		setNumberOfEntries(getNumberOfEntries() + num);
		
		
	}
	
	private void setNumberOfEntries(int num){
		numberOfEntries = num;
		writeNumberOfEntries();
	}
	
	public K getLastKey(){
		int pos = posBehindLastEntry();
		pos -= 2 * serializedPointerSize;
		return getKeyOfPos(pos);
	}
	
	public PagePointer getLastKeyPointer(){
		buffer().position(posOfKey(-1));
		byte[] buf = new byte[serializedPointerSize];
		buffer().get(buf);
		return pointerSerializer.deserialize(buf);
	}
	
	public K getKeyOfPos(int pos){
		buffer().position(pos);
		byte[] buf = new byte[serializedPointerSize];
		buffer().get(buf);
		
		PagePointer p = pointerSerializer.deserialize(buf);
		return keyPageManager.getPage(p.getId()).get(p.getOffset());
	}
	
	public K getFirstKey(){
		if(getNumberOfEntries() == 0)
			return null;
		
		int pos = posOfKey(0);
		return getKeyOfPos(pos);
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
		// number of entries + next leaf id
		return (Integer.SIZE + Long.SIZE) / 8;
	}

	private void writeNumberOfEntries() {
		buffer().position(0);
		buffer().putInt(numberOfEntries);
		rawPage.setModified(true);
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.btree.Node#getNumberOfEntries()
	 */
	@Override
	public int getNumberOfEntries() {
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
		if(!((i >= 0 && i < getNumberOfEntries()) || (i < 0 && i >= -1 * getNumberOfEntries())))
			throw new IllegalArgumentException("i must be between -numberOfEntries and +(numberOfEntries - 1)");
		
		return ( i >= 0 ) ? headerSize() + i * serializedPointerSize * 2 : posOfKey(getNumberOfEntries() + i);
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
	public int remove(K key) throws Exception {
		int pos = posOfKey(key);
		if(pos == NOT_FOUND)
			return 0;
		
		int numberOfValues = get(key).size();
		int sizeOfValues = numberOfValues * serializedPointerSize * 2;
		
		//TODO: free key and value pages
		
		// shift the pointers after key
		System.arraycopy(buffer().array(), pos + sizeOfValues , buffer().array(), pos , buffer().array().length - pos - sizeOfValues);
		numberOfEntries -= numberOfValues;
		return numberOfValues;
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
	 * @see com.freshbourne.multimap.btree.Node#destroy()
	 */
	@Override
	public void destroy() throws Exception {
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
		
		leafPageManager.removePage(rawPage().id());
	}

	/**
	 * @return the maximal number of Entries
	 */
	public int getMaxEntries() {
		return maxEntries;
	}

	private PagePointer storeKey(K key) {
		DataPage<K> page = null;
		Integer entryId = null;
		
		if(lastKeyPageId != null){
			page = keyPageManager.getPage(lastKeyPageId);
			entryId = page.add(key);
		}
		
		if(entryId == null){
			page = keyPageManager.createPage();
			entryId = page.add(key);
		}
		
		lastKeyPageId = page.rawPage().id();
		lastKeyPageRemainingBytes = page.remaining();
		
		return new PagePointer(page.rawPage().id(), entryId);
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
		setNextLeafId(NO_NEXT_LEAF);
		writeNumberOfEntries();
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

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.btree.Node#insert(java.lang.Object, java.lang.Object)
	 */
	@Override
	public AdjustmentAction<K, V> insert(K key, V value) {
		ensureValid();
		
		if(!isFull()){
			// add to data_page
			PagePointer keyPointer = storeKey(key);
			PagePointer valuePointer = storeValue(value);
			

	        // serialize Pointers
			addEntry(keyPointer, valuePointer);
			
			writeNumberOfEntries();
			return null;	
		}
		
		// if leaf does not have enough space but we can move some data to the next leaf
		if (this.getNextLeafId() != null) {
			LeafPage<K, V> nextLeaf = leafPageManager.getPage(this.getNextLeafId());
			
			if(nextLeaf.getRemainingEntries() >= getMinFreeLeafEntriesToMove()){
				nextLeaf.prependEntriesFromOtherPage(this, nextLeaf.getRemainingEntries() >> 1);
				
				// see on which page we will insert the value
				if(comparator.compare(key, this.getLastKey()) > 0){
					nextLeaf.insert(key, value);
				} else {
					this.insert(key, value);
				}
				
				return new AdjustmentAction<K, V>(ACTION.UPDATE_KEY, this.getLastKeyPointer(), null);
			}
		}
		
		// if we have to allocate a new leaf
		
		// allocate new leaf
		LeafPage<K,V> newLeaf = leafPageManager.createPage();
		newLeaf.setNextLeafId(this.getId());
		this.setNextLeafId(newLeaf.rawPage().id());
			
		// newLeaf.setLastKeyContinuesOnNextPage(root.isLastKeyContinuingOnNextPage());
			
		// move half of the keys to new page
		newLeaf.prependEntriesFromOtherPage(this,
				this.getNumberOfEntries() >> 1);

		// see on which page we will insert the value
		if (comparator.compare(key, this.getLastKey()) > 0) {
			newLeaf.insert(key, value);
		} else {
			this.insert(key, value);
		}

		return new AdjustmentAction<K, V>(ACTION.INSERT_NEW_NODE,
				this.getLastKeyPointer(), newLeaf.rawPage().id());
	}
	
	
	/**
	 * @return id of the next leaf or null
	 */
	public Long getNextLeafId() {
		buffer().position(posOfNextLeafId());
		Long result = buffer().getLong();
		return result == NO_NEXT_LEAF ? null : result;
	}
	
	private int posOfNextLeafId(){
		return Integer.SIZE / 8;
	}

	public void setNextLeafId(Long id) {
		buffer().position(posOfNextLeafId());
		buffer().putLong(id);
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.btree.Node#getKeyPointer(int)
	 */
	@Override
	public PagePointer getKeyPointer(int pos) {
		
		if(pos >= 0){
			posOfKey(pos);
		}
		
		return null;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.btree.Node#getId()
	 */
	@Override
	public Long getId() {
		return rawPage.id();
	}

	public int getRemainingEntries() {
		return getMaxEntries() - getNumberOfEntries();
	}

	public int getMaximalNumberOfEntries() {
		return maxEntries;
	}

	/**
	 * @param tree the tree to set
	 */
	public void setTree(BTree<K, V> tree) {
		this.tree = tree;
	}

	/**
	 * @return the tree
	 */
	public BTree<K, V> getTree() {
		return tree;
	}
}
