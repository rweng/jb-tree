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
import com.freshbourne.multimap.btree.BTree.NodeType;
import com.freshbourne.serializer.FixLengthSerializer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * This B-Tree-Leaf stores entries by storing the keys and values in separate pages
 * and keeping track only of the pageId and offset.
 * 
 * Looks like this in binary: NUM_OF_ENTRIES, NEXT_LEAF_ID, KEY_POINTER, VALUE_POINTER, ...
 * 
 * @author "Robin Wenglewski <robin@wenglewski.de>"
 *
 * @param <K> KeyType
 * @param <V> ValueType
 */
public class LeafNode<K,V> implements Node<K,V>, ComplexPage {
	
	/**
	 * If a leaf page is less full than this factor, it may be target of operations
	 * where entries are moved from one page to another. 
	 */
	private static final float MAX_LEAF_ENTRY_FILL_LEVEL_TO_MOVE = 0.75f;
	
	private static final NodeType NODE_TYPE = NodeType.LEAF_NODE;
	
	
	private final FixLengthSerializer<PagePointer, byte[]> pointerSerializer;
	
	private DataPageManager<K> keyPageManager;
	private DataPageManager<V> valuePageManager;
	
	private final Comparator<K> comparator;
	
	private boolean valid = false;
	
	// right now, we always store key/value pairs. If the entries are not unique,
	// it could make sense to store the key once with references to all values
	//TODO: investigate if we should do this
	private  int maxEntries;
	
	
	private static final int NOT_FOUND = -1;
	private static final Integer NO_NEXT_LEAF = 0;
	
	private final RawPage rawPage;
	
	// counters
	private int numberOfEntries = 0;
	
	private Integer lastKeyPageId = null;
	private int lastKeyPageRemainingBytes = -1;
	
	private Integer lastValuePageId = null;
	private int lastValuePageRemainingBytes = -1;
	private final PageManager<LeafNode<K,V>> leafPageManager;
	
	
	LeafNode(
			RawPage page,
            DataPageManager<K> keyPageManager,
			DataPageManager<V> valuePageManager,
			FixLengthSerializer<PagePointer, byte[]> pointerSerializer,
			Comparator<K> comparator,
			PageManager<LeafNode<K,V>> leafPageManager
			){
    	this.leafPageManager = leafPageManager;
    	this.rawPage = page;
		this.keyPageManager = keyPageManager;
		this.valuePageManager = valuePageManager;
		this.pointerSerializer = pointerSerializer;
		this.comparator = comparator;
		
		// one pointer to key, one to value
		maxEntries = (rawPage.bufferForReading(0).capacity() - headerSize()) / (pointerSerializer.getSerializedLength() * 2); 
	}
	
	/**
	 * If a leaf page has at least that many free slots left, we can move pointers to it
	 * from another node. This number is computed from the
	 * <tt>MAX_LEAF_ENTRY_FILL_LEVEL_TO_MOVE</tt> constant.
	 */
	private int getMinFreeLeafEntriesToMove(){
		return (int) (getMaximalNumberOfEntries() *
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
	
	public void prependEntriesFromOtherPage(LeafNode<K, V> source, int num){
		
		// checks
		if(num < 0)
			throw new IllegalArgumentException("num must be > 0");
		
		if(num > source.getNumberOfEntries())
			throw new IllegalArgumentException("the source leaf has not enough entries");
		
		if(getNumberOfEntries() + num > maxEntries)
			throw new IllegalArgumentException("not enough space in this leaf to prepend " + num + " entries from other leaf");
		
		if(getNumberOfEntries() > 0 && comparator.compare(source.getLastLeafKey(), getFirstLeafKey()) > 0)
			throw new IllegalStateException("the last key of the provided source leaf is larger than this leafs first key");
		
		ByteBuffer buffer = rawPage().bufferForWriting(0);
		
		// make space in this leaf, move all elements to the right
		int totalSize = num * (pointerSerializer.getSerializedLength() * 2);
		int byteToMove = buffer.limit() - headerSize() - totalSize;
		System.arraycopy(buffer.array(), headerSize(), buffer.array(), headerSize() + totalSize, byteToMove);
		
		// copy from other to us
		int sourceOffset = source.getOffsetForKeyPos(source.getNumberOfEntries() - 1 - num);
		System.arraycopy(source.rawPage().bufferForWriting(0).array(), sourceOffset, buffer.array(), headerSize(), totalSize);
		
		// update headers, also sets modified
		source.setNumberOfEntries(source.getNumberOfEntries() - num);
		setNumberOfEntries(getNumberOfEntries() + num);
	}
	
	private void setNumberOfEntries(int num){
		numberOfEntries = num;
		rawPage().bufferForWriting(0).putInt(numberOfEntries);
	}
	
	public K getLastLeafKey(){
		if(getNumberOfEntries() == 0)
			throw new IllegalStateException("you can only get the last key if there are any Keys in the Leaf");
		
		int offset = offsetBehindLastEntry();
		offset -= 2 * pointerSerializer.getSerializedLength();
		return getKeyAtOffset(offset);
	}
	
	public byte[] getLastKey(){
		ByteBuffer buffer = rawPage().bufferForReading(getOffsetForKeyPos(getNumberOfEntries() - 1));
		byte[] buf = new byte[pointerSerializer.getSerializedLength()];
		buffer.get(buf);
		return buf;
	}
	
	public K getKeyAtOffset(int offset){
		ByteBuffer buffer = rawPage().bufferForReading(offset);
		byte[] buf = new byte[pointerSerializer.getSerializedLength()];
		buffer.get(buf);
		
		PagePointer p = pointerSerializer.deserialize(buf);
		return keyPageManager.getPage(p.getId()).get(p.getOffset());
	}
	
	public K getFirstLeafKey(){
		if(getNumberOfEntries() == 0)
			return null;
		
		int pos = getOffsetForKeyPos(0);
		return getKeyAtOffset(pos);
	}

	/**
	 * @param keyPointer
	 * @param valuePointer
	 */
	private void addEntry(K key, PagePointer keyPointer, PagePointer valuePointer) {
		
		ByteBuffer buf = rawPage().bufferForWriting(0);
		int offset = offsetOfKey(key, true);
		
		if (offset == -1) {
			offset = offsetBehindLastEntry();
		} else {
			// move everything including pos backwards
			System.arraycopy(buf.array(), offset, buf.array(), offset + 2
					* pointerSerializer.getSerializedLength(), buf.capacity() - (offset + 2 * pointerSerializer.getSerializedLength()));
		}
		// insert both
		buf.position(offset);
		buf.put(pointerSerializer.serialize(keyPointer));
		buf.put(pointerSerializer.serialize(valuePointer));
        
		setNumberOfEntries(getNumberOfEntries() + 1);
	}
	
	private int headerSize(){
		// number of entries + next leaf id
		return (Integer.SIZE + Integer.SIZE) / 8;
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
	public boolean containsKey(K key) {
        return offsetOfKey(key) != NOT_FOUND;
	}
	
	/**
	 * @param pos, must be between 0 and numberOfEntries - 1
	 * @return offset
	 */
	int getOffsetForKeyPos(int pos){
		if(pos < 0 || pos >= getNumberOfEntries())
			throw new IllegalArgumentException("invalid pos: " + pos + ". pos must be between 0 and numberOfEntries - 1");
		
		return headerSize() + pos * pointerSerializer.getSerializedLength() * 2;
	}
	
	private int offsetForValue(int i){
		return getOffsetForKeyPos(i) + pointerSerializer.getSerializedLength();
	}
	
	/**
	 * does not alter the header bytebuffer
	 * @return
	 */
	private int offsetBehindLastEntry(){
		return headerSize() + getNumberOfEntries() * pointerSerializer.getSerializedLength() * 2;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#get(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<V> get(K key) {
		List<V> result = new ArrayList<V>();
		
		byte[] bytebuf = new byte[pointerSerializer.getSerializedLength()];
		
		int pos = offsetOfKey(key);
		if( pos == NOT_FOUND)
			return result;
		
		ByteBuffer buffer = rawPage().bufferForReading(pos);
		
		// read first
		buffer.get(bytebuf);
		PagePointer p = pointerSerializer.deserialize(bytebuf);
		DataPage<K> dataPage = keyPageManager.getPage(p.getId());
		
		while (dataPage.get(p.getOffset()).equals(key)) {
			buffer.get(bytebuf);
			p = pointerSerializer.deserialize(bytebuf);
			DataPage<V> valueDataPage = valuePageManager.getPage(p.getId());

			result.add(valueDataPage.get(p.getOffset()));
			
			// if no other entries are available, return
			if(buffer.position() >= offsetBehindLastEntry())
				break;
			
			// otherwise check, if the next entry also has this key
			buffer.get(bytebuf);
			p = pointerSerializer.deserialize(bytebuf);
			dataPage = keyPageManager.getPage(p.getId());
			
		}
		return result;
	}
	
	/**
	 * @param key
	 * @param takeNext boolean, whether if the key was not found the next higher key should be taken
	 * @return position to set the buffer to, where the key starts, -1 if key not found 
	 */
	private int offsetOfKey(K key, boolean takeNext) {
		
		int pSize = pointerSerializer.getSerializedLength();
		byte[] bytebuf = new byte[pSize];

		ByteBuffer buffer = rawPage().bufferForReading(headerSize());

		for(int i = 0; i < getNumberOfEntries(); i++){

			buffer.get(bytebuf);
			PagePointer p = pointerSerializer.deserialize(bytebuf);
			DataPage<K> dataPage = keyPageManager.getPage(p.getId());
			if(dataPage == null)
				throw new IllegalStateException("dataPage should not be null");
			
			int compResult = comparator.compare(dataPage.get(p.getOffset()),key);
			if( compResult == 0){
				return buffer.position() - pSize;
			} else if ( compResult > 0){
				if(takeNext)
					return buffer.position() - pSize;
				else
					return NOT_FOUND;
			}
			
			// if compresult < 0:
			// get the data pointer but do nothing with it
			buffer.get(bytebuf);
		}
		return NOT_FOUND;
	}
	
	private int offsetOfKey(K key) {
		return offsetOfKey(key, false);
	}


	/**
	 * @param currentPos
	 * @return a valid position to read the next key from, or NOT_FOUND
	 */
	private int getPosWhereNextKeyStarts(int currentPos) {
		if(currentPos < headerSize())
			currentPos = headerSize();
		
		currentPos -= headerSize();
		currentPos /= (pointerSerializer.getSerializedLength() * 2);
		if(currentPos >= getNumberOfEntries())
			return NOT_FOUND;
		
		currentPos *= (pointerSerializer.getSerializedLength() * 2);
		return currentPos + headerSize();
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#remove(java.lang.Object)
	 */
	@Override
	public int remove(K key) {
		int pos = offsetOfKey(key);
		if(pos == NOT_FOUND)
			return 0;
		
		int numberOfValues = get(key).size();
		int sizeOfValues = numberOfValues * pointerSerializer.getSerializedLength() * 2;
		
		//TODO: free key and value pages
		
		// shift the pointers after key
		ByteBuffer buffer = rawPage().bufferForWriting(0);
		System.arraycopy(buffer.array(), pos + sizeOfValues , buffer.array(), pos , buffer.capacity() - pos - sizeOfValues);
		setNumberOfEntries(getNumberOfEntries() - numberOfValues);
		
		return numberOfValues;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.btree.Node#remove(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int remove(K key, V value) {
		int pos = offsetOfKey(key); //TODO: refactor pos to offset
		if(pos == NOT_FOUND)
			return 0;
		
		
		int numberOfValues = get(key).size();
		
		ByteBuffer buffer = rawPage().bufferForWriting(pos);
		byte[] buf1 = new byte[pointerSerializer.getSerializedLength()];
		byte[] buf2 = new byte[pointerSerializer.getSerializedLength()];
		int removed = 0;
		
		for(int i = 0; i < numberOfValues; i++){
			buffer.get(buf1);
			buffer.get(buf2); // load only the value
			PagePointer p = pointerSerializer.deserialize(buf2);
			DataPage<V> vPage = valuePageManager.getPage(p.getId());
			V val = vPage.get(p.getOffset());
			
			if(val == null)
				throw new IllegalStateException("value retrieved from a value page should not be null");
			
			// we cant use a comparator here since we have none for values (its the only case we need it)
			if( val.equals(value) ){
				vPage.remove(p.getOffset());
				if(vPage.numberOfEntries() == 0)
					valuePageManager.removePage(vPage.rawPage().id());
				
				// also free key page
				p = pointerSerializer.deserialize(buf1);
				DataPage <K> kPage = keyPageManager.getPage(p.getId());
				kPage.remove(p.getOffset());
				if(kPage.numberOfEntries() == 0)
					keyPageManager.removePage(p.getId());
				
				// move pointers forward and reset buffer
				int startingPos = buffer.position() - buf1.length - buf2.length;
				System.arraycopy(buffer.array(), buffer.position(), buffer.array(), startingPos, buffer.capacity() - buffer.position());
				
				buffer.position(startingPos);
				
				removed++;
			}	
		}
		
		setNumberOfEntries(getNumberOfEntries() - removed);
		return removed;
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
	public void destroy() {
		byte[] buf = new byte[pointerSerializer.getSerializedLength()];
		
		//TODO: free pages
		for(int i = 0; i < getNumberOfEntries(); i++){
			
			// key page
			ByteBuffer buffer = rawPage().bufferForWriting(getOffsetForKeyPos(i));
			buffer.get(buf);
			PagePointer p = pointerSerializer.deserialize(buf);
			DataPage<K> keyPage = keyPageManager.getPage(p.getId());
			keyPage.remove(p.getOffset());
			if(keyPage.numberOfEntries() == 0)
				keyPageManager.removePage(keyPage.rawPage().id());
			
			// data page
			buffer.get(buf);
			p = pointerSerializer.deserialize(buf);
			DataPage<V> valuePage = valuePageManager.getPage(p.getId());
			valuePage.remove(p.getOffset());
			if(valuePage.numberOfEntries() == 0)
				valuePageManager.removePage(valuePage.rawPage().id());
			
		}
		
		leafPageManager.removePage(rawPage().id());
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
		DataPage<V> page = null;
		Integer entryId = null;
		
		if(lastValuePageId != null){
			page = valuePageManager.getPage(lastValuePageId);
			entryId = page.add(value);
		}
		
		if(entryId == null){
			page = valuePageManager.createPage();
			entryId = page.add(value);
		}
		
		lastValuePageId = page.rawPage().id();
		lastValuePageRemainingBytes = page.remaining();
		
		return new PagePointer(page.rawPage().id(), entryId);
	}


	/* (non-Javadoc)
	 * @see com.freshbourne.io.ComplexPage#initialize()
	 */
	@Override
	public void initialize() {
		setNumberOfEntries(0);
		setNextLeafId(NO_NEXT_LEAF);
		valid = true;
	}


	/* (non-Javadoc)
	 * @see com.freshbourne.io.ComplexPage#load()
	 */
	@Override
	public void load() {
		numberOfEntries = rawPage().bufferForReading(0).getInt();
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
			addEntry(key, keyPointer, valuePointer);
			
			return null;	
		}
		
		// if leaf does not have enough space but we can move some data to the next leaf
		if (hasNextLeaf()) {
			LeafNode<K, V> nextLeaf = leafPageManager.getPage(this.getNextLeafId());
			
			if(nextLeaf.getRemainingEntries() >= getMinFreeLeafEntriesToMove()){
				nextLeaf.prependEntriesFromOtherPage(this, nextLeaf.getRemainingEntries() >> 1);
				
				// see on which page we will insert the value
				if(comparator.compare(key, this.getLastLeafKey()) > 0){
					nextLeaf.insert(key, value);
				} else {
					this.insert(key, value);
				}
				
				return new AdjustmentAction<K, V>(ACTION.UPDATE_KEY, this.getLastKey(), null);
			}
			
			
		}
		
		// allocate new leaf
		LeafNode<K,V> newLeaf = leafPageManager.createPage();
		newLeaf.setNextLeafId(getNextLeafId());
		setNextLeafId(newLeaf.getId());
		
		// newLeaf.setLastKeyContinuesOnNextPage(root.isLastKeyContinuingOnNextPage());
			
		// move half of the keys to new page
		newLeaf.prependEntriesFromOtherPage(this,
				this.getNumberOfEntries() >> 1);

		// see on which page we will insert the value
		if (comparator.compare(key, this.getLastLeafKey()) > 0) {
			newLeaf.insert(key, value);
		} else {
			this.insert(key, value);
		}
		
		// just to make sure, that the adjustment action is correct:
		AdjustmentAction<K, V> action = new AdjustmentAction<K, V>(ACTION.INSERT_NEW_NODE,
				this.getLastKey(), newLeaf.rawPage().id());
			
		return action;
	}
	
	
	/**
	 * @return id of the next leaf or null
	 */
	public Integer getNextLeafId() {
		ByteBuffer buffer = rawPage().bufferForReading(posOfNextLeafId());
		Integer result = buffer.getInt();
		return result == 0 ? null : result;
	}
	
	private int posOfNextLeafId(){
		return Integer.SIZE / 8;
	}

	public void setNextLeafId(Integer id) {
		ByteBuffer buffer = rawPage().bufferForWriting(posOfNextLeafId());
		buffer.putInt(id == null ? NO_NEXT_LEAF : id);
	}
	
	public boolean hasNextLeaf(){
		return getNextLeafId() != null;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.btree.Node#getKeyPointer(int)
	 */
	@Override
	public PagePointer getKeyPointer(int pos) {
		
		if(pos >= 0){
			getOffsetForKeyPos(pos);
		}
		
		return null;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.btree.Node#getId()
	 */
	@Override
	public Integer getId() {
		return rawPage.id();
	}

	public int getRemainingEntries() {
		return getMaximalNumberOfEntries() - getNumberOfEntries();
	}

	/**
	 * @return the maximal number of Entries
	 */
	public int getMaximalNumberOfEntries() {
		return maxEntries;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.btree.Node#getNumberOfUniqueKeys()
	 */
	@Override
	public int getNumberOfKeys() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @param pos, starting with 0, going to numberOfEntries - 1
	 * @return
	 */
	public K getKeyAtPosition(int pos) {
		return getKeyAtOffset(getOffsetForKeyPos(pos));
	}
	
	public List<K> getKeySet(){
		List<K> result = new ArrayList<K>();
		for(int i = 0; i < getNumberOfEntries(); i++){
			result.add(getKeyAtPosition(i));
		}
		return result;
	}
	
	
	public class LeafNodeIterator implements Iterator<V>{
		
		private final K from;
		private final K to;
		private V next;
		private int currentKeyOffset;
		private LeafNode<K, V> currentNode;
		private byte[] pointerBuffer;

		public LeafNodeIterator(LeafNode<K, V> node, K from, K to) {
			this.from = from;
			this.to = to;
			this.currentNode = node;
			
			pointerBuffer = new byte[(pointerSerializer.getSerializedLength())];
			
			this.currentKeyOffset = currentNode.offsetOfKey(from, true);
			
			if(currentKeyOffset == NOT_FOUND)
				throw new RuntimeException("all keys are smaller in this leaf");
			
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			if(next != null)
				return true;
			
			next = next();
			
			return next != null;
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		@Override
		public V next() {
			
			if(next != null){
				V result = next;
				next = null;
				return result;
			}
			
			if(currentKeyOffset > getOffsetForKeyPos(getNumberOfEntries() - 1)){
				if(currentNode.getNextLeafId() == null){
					return null;
				}
				
				// if we have a next key
				currentNode = leafPageManager.getPage(currentNode.getNextLeafId());
				currentKeyOffset = currentNode.headerSize();
			}
			
			ByteBuffer buf = currentNode.rawPage().bufferForReading(currentKeyOffset + pointerSerializer.getSerializedLength());
			buf.get(pointerBuffer);
			PagePointer p = pointerSerializer.deserialize(pointerBuffer);
			
			currentKeyOffset = buf.position();
			
			return valuePageManager.getPage(p.getId()).get(p.getOffset());
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException();
		}

	}


	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.btree.Node#getIterator(java.lang.Object, java.lang.Object)
	 */
	@Override
	public Iterator<V> getIterator(K from, K to) {
		return new LeafNodeIterator(this, from, to);
	}
}
