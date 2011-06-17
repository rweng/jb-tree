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
	
	static enum Header{
		NODE_TYPE(0){}, // char
		NUMBER_OF_KEYS(Character.SIZE / 8), // int
		NEXT_LEAF_ID((Character.SIZE + Integer.SIZE) / 8); // int
		
		
		private int offset;
		Header(int offset){
			this.offset = offset;
		}
		static int size(){return (Character.SIZE + 2 * Integer.SIZE) / 8;} // 6
		int getOffset(){return offset;}
	}
	
	/**
	 * how many values must at least fit in the RawPage
	 */
	static final int MIN_VALUES_IN_RAWPAGE = 1;
	
	
	/**
	 * If a leaf page is less full than this factor, it may be target of operations
	 * where entries are moved from one page to another. 
	 */
	private static final float MAX_LEAF_ENTRY_FILL_LEVEL_TO_MOVE = 0.75f;
	
	private static final NodeType NODE_TYPE = NodeType.LEAF_NODE;
	
	private final FixLengthSerializer<V, byte[]> valueSerializer;
	
	private final Comparator<K> comparator;
	
	private boolean valid = false;
	
	// right now, we always store key/value pairs. If the entries are not unique,
	// it could make sense to store the key once with references to all values
	//TODO: investigate if we should do this
	private  int maxEntries;
	
	
	private static final int NOT_FOUND = -1;
	protected static final Integer NO_NEXT_LEAF = 0;
	
	private final RawPage rawPage;
	
	// counters
	private int numberOfEntries = 0;
	
	private Integer lastKeyPageId = null;
	private int lastKeyPageRemainingBytes = -1;
	
	private Integer lastValuePageId = null;
	private int lastValuePageRemainingBytes = -1;
	private final PageManager<LeafNode<K,V>> leafPageManager;

	private final FixLengthSerializer<K, byte[]> keySerializer;
	
	
	LeafNode(
			RawPage page,
            FixLengthSerializer<K, byte[]> keySerializer,
			FixLengthSerializer<V, byte[]> valueSerializer,
			Comparator<K> comparator,
			PageManager<LeafNode<K,V>> leafPageManager
			){
    	this.leafPageManager = leafPageManager;
    	this.rawPage = page;
		this.keySerializer = keySerializer;
		this.valueSerializer = valueSerializer;
		this.comparator = comparator;
		
		// one pointer to key, one to value
		maxEntries = (rawPage.bufferForReading(0).limit() - Header.size()) / 
			(valueSerializer.getSerializedLength() + keySerializer.getSerializedLength());
		
		int requiredBytes =Header.size() + MIN_VALUES_IN_RAWPAGE * 
			(keySerializer.getSerializedLength() + valueSerializer.getSerializedLength());
		if(page.bufferForReading(0).limit() - requiredBytes < 0)
			throw new IllegalArgumentException("The RawPage must have space for at least " + 
					MIN_VALUES_IN_RAWPAGE + " Entries (" + requiredBytes + " bytes)");
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
		int totalSize = num * (keySerializer.getSerializedLength() + valueSerializer.getSerializedLength() );
		int byteToMove = buffer.limit() - Header.size() - totalSize;
		System.arraycopy(buffer.array(), Header.size(), buffer.array(), Header.size() + totalSize, byteToMove);
		
		// copy from other to us
		int sourceOffset = source.getOffsetForKeyPos(source.getNumberOfEntries() - 1 - num);
		System.arraycopy(source.rawPage().bufferForWriting(0).array(), sourceOffset, buffer.array(), Header.size(), totalSize);
		
		// update headers, also sets modified
		source.setNumberOfEntries(source.getNumberOfEntries() - num);
		setNumberOfEntries(getNumberOfEntries() + num);
	}
	
	private void setNumberOfEntries(int num){
		numberOfEntries = num;
		rawPage().bufferForWriting(Header.NUMBER_OF_KEYS.getOffset()).putInt(numberOfEntries);
	}
	
	public K getLastLeafKey(){
		if(getNumberOfEntries() == 0)
			throw new IllegalStateException("you can only get the last key if there are any Keys in the Leaf");
		
		int offset = offsetBehindLastEntry();
		offset -= keySerializer.getSerializedLength() + valueSerializer.getSerializedLength();
		return getKeyAtOffset(offset);
	}
	
	public byte[] getLastKey(){
		ByteBuffer buffer = rawPage().bufferForReading(getOffsetForKeyPos(getNumberOfEntries() - 1));
		byte[] buf = new byte[valueSerializer.getSerializedLength()];
		buffer.get(buf);
		return buf;
	}
	
	public K getKeyAtOffset(int offset){
		throw new UnsupportedOperationException();
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
	private void addEntry(K key, V value) {
		
		ByteBuffer buf = rawPage().bufferForWriting(0);
		int offset = offsetOfKey(key, true);
		
		if (offset == -1) {
			offset = offsetBehindLastEntry();
		} else {
			// move everything including pos backwards
			System.arraycopy(buf.array(), offset, buf.array(), offset + 2
					* valueSerializer.getSerializedLength(), buf.capacity() - (offset + keySerializer.getSerializedLength() + valueSerializer.getSerializedLength()));
		}
		// insert both
		buf.position(offset);
		buf.put(keySerializer.serialize(key));
		buf.put(valueSerializer.serialize(value));
        
		setNumberOfEntries(getNumberOfEntries() + 1);
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
		
		return Header.size() + pos * (valueSerializer.getSerializedLength() + keySerializer.getSerializedLength()) ;
	}
	
	private int offsetForValue(int i){
		return getOffsetForKeyPos(i) + valueSerializer.getSerializedLength();
	}
	
	/**
	 * does not alter the header bytebuffer
	 * @return
	 */
	private int offsetBehindLastEntry(){
		return Header.size() + getNumberOfEntries() * (valueSerializer.getSerializedLength() * keySerializer.getSerializedLength());
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#get(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<V> get(K key) {
		List<V> result = new ArrayList<V>();
		
		byte[] bytebuf = new byte[valueSerializer.getSerializedLength()];
		
		int pos = offsetOfKey(key);
		if( pos == NOT_FOUND)
			return result;
		
		ByteBuffer buffer = rawPage().bufferForReading(pos);
		
		// read first
		buffer.get(bytebuf);
		V p = valueSerializer.deserialize(bytebuf);
		
		return result;
	}
	
	/**
	 * @param key
	 * @param takeNext boolean, whether if the key was not found the next higher key should be taken
	 * @return position to set the buffer to, where the key starts, -1 if key not found 
	 */
	private int offsetOfKey(K key, boolean takeNext) {
		
		byte[] pointerBuf = new byte[valueSerializer.getSerializedLength()];
		byte[] keyBuf = new byte[keySerializer.getSerializedLength()];
		
		ByteBuffer buffer = rawPage().bufferForReading(Header.size());

		for(int i = 0; i < getNumberOfEntries(); i++){

			buffer.get(keyBuf);
			
			int compResult = comparator.compare(keySerializer.deserialize(keyBuf),key);
			
			if( compResult == 0){
				return buffer.position() - keySerializer.getSerializedLength();
			} else if ( compResult > 0){
				if(takeNext)
					return buffer.position() - keySerializer.getSerializedLength();
				else
					return NOT_FOUND;
			}
			
			// if compresult < 0:
			// get the data pointer but do nothing with it
			buffer.get(pointerBuf);
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
		if(currentPos < Header.size())
			currentPos = Header.size();
		
		currentPos -= Header.size();
		currentPos /= (valueSerializer.getSerializedLength() + keySerializer.getSerializedLength());
		if(currentPos >= getNumberOfEntries())
			return NOT_FOUND;
		
		currentPos *= (valueSerializer.getSerializedLength() + keySerializer.getSerializedLength());
		return currentPos + Header.size();
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
		int sizeOfValues = numberOfValues * (valueSerializer.getSerializedLength() + keySerializer.getSerializedLength());
		
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
		byte[] buf1 = new byte[valueSerializer.getSerializedLength()];
		byte[] buf2 = new byte[valueSerializer.getSerializedLength()];
		int removed = 0;
		
		for(int i = 0; i < numberOfValues; i++){
			buffer.get(buf1);
			buffer.get(buf2); // load only the value
			V val = valueSerializer.deserialize(buf2);
			
			if(val == null)
				throw new IllegalStateException("value retrieved from a value page should not be null");
			
			// we cant use a comparator here since we have none for values (its the only case we need it)
			if( val.equals(value) ){
				// also free key page
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
		byte[] buf = new byte[valueSerializer.getSerializedLength()];
		
		//TODO: free pages
		for(int i = 0; i < getNumberOfEntries(); i++){
			
			// key page
			ByteBuffer buffer = rawPage().bufferForWriting(getOffsetForKeyPos(i));
			buffer.get(buf);
			V p = valueSerializer.deserialize(buf);
			
			// data page
			buffer.get(buf);
			p = valueSerializer.deserialize(buf);
			
		}
		
		leafPageManager.removePage(rawPage().id());
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.ComplexPage#initialize()
	 */
	@Override
	public void initialize() {
		rawPage.bufferForWriting(Header.NODE_TYPE.getOffset()).putChar(NODE_TYPE.serialize());
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
			// serialize data
			addEntry(key, value);
			
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
		ByteBuffer buffer = rawPage().bufferForReading(Header.NEXT_LEAF_ID.getOffset());
		Integer result = buffer.getInt();
		return result == 0 ? null : result;
	}
	
	public void setNextLeafId(Integer id) {
		ByteBuffer buffer = rawPage().bufferForWriting(Header.NEXT_LEAF_ID.getOffset());
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
			
			pointerBuffer = new byte[(valueSerializer.getSerializedLength())];
			
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
				currentKeyOffset = Header.size();
			}
			
			ByteBuffer buf = currentNode.rawPage().bufferForReading(currentKeyOffset + valueSerializer.getSerializedLength());
			buf.get(pointerBuffer);
			V p = valueSerializer.deserialize(pointerBuffer);
			
			currentKeyOffset = buf.position();
			
			return p;
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
