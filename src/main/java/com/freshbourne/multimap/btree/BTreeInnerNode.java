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
import com.freshbourne.multimap.btree.BTree.NodeType;
import com.freshbourne.serializer.FixLengthSerializer;

/**
 *
 * stores pointers to the keys that get push upwards to InnerNodes from LeafPages, as well as the id of nodes
 * in the following order:
 * 
 * NODE_ID | KEY_POINTER | NODE_ID | KEY_POINTER | NODE_ID ...
 *
 * @author Robin Wenglewski <robin@wenglewski.de>
 *
 * @param <K>
 * @param <V>
 */
public class BTreeInnerNode<K, V> implements Node<K,V>, ComplexPage {
	
	private static final NodeType NODE_TYPE = NodeType.INNER_NODE;
	
	private static enum Header{
		NODE_TYPE(0){},
		NUMBER_OF_KEYS(1);
		
		private int pos;
		Header(int pos){
			this.pos = pos;
		}
		
		int getPosition(){return pos;}
	}
	
	private final RawPage rawPage;
	private final Comparator<K> comperator;
	private final FixLengthSerializer<PagePointer, byte[]> pointerSerializer;
	
	private int numberOfKeys;
	private boolean valid = false;
	
	BTreeInnerNode(RawPage rawPage, FixLengthSerializer<PagePointer, byte[]> pointerSerializer,
			Comparator<K> comparator){
		this.rawPage = rawPage;
		this.comperator = comparator;
		this.pointerSerializer = pointerSerializer;
	}
	
	public void initRootState(PagePointer keyPointer, Long pageId1, Long pageId2){
		ensureValid();
		
		ByteBuffer buf = buffer();
		buf.position(headerSize());
		
		buffer().putLong(pageId1);
		buffer().put(pointerSerializer.serialize(keyPointer));
		buffer().putLong(pageId2);
		
		setNumberOfKeys(1);
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

		throw new UnsupportedOperationException("recursive get number of entries not yet supported.");
		
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#containsKey(java.lang.Object)
	 */
	@Override
	public boolean containsKey(K key) {
		ensureValid();

		throw new UnsupportedOperationException();
	}
	
	private void writeNumberOfKeys() {
		ByteBuffer buf = rawPage.buffer();
		buf.position(Header.NUMBER_OF_KEYS.getPosition());
		buf.putInt(numberOfKeys);
	}
	
	/**
	 * @param numberOfKeys the numberOfKeys to set
	 */
	private void setNumberOfKeys(int numberOfKeys) {
		this.numberOfKeys = numberOfKeys;
		writeNumberOfKeys();
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
	
	private int posOfKey(int i){
		return ((i + 1) * Long.SIZE / 8) + (i * pointerSerializer.serializedLength(PagePointer.class)); 
	}
	
	private PagePointer getChildForKey(K key) {
		for(int i = 0; i < numberOfKeys; i++){
			PagePointer pp = getPointerAtOffset(posOfKey(i));
			
		}
		return null;
	}

	private PagePointer getPointerAtOffset(int offset) {
		ByteBuffer buf = buffer();
		buf.position(offset);
		byte[] byteBuf = new byte[pointerSerializer.serializedLength(PagePointer.class)];
		buf.get(byteBuf);
		return pointerSerializer.deserialize(byteBuf);
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
	
	private ByteBuffer buffer(){
		return rawPage.buffer();
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.ComplexPage#load()
	 */
	@Override
	public void load() {
		ByteBuffer buf = rawPage.bufferAtZero();
		if(NodeType.deserialize(buf.getChar()) != NODE_TYPE)
			throw new IllegalStateException("You are trying to load a InnerNode from a byte array, that does not contain an InnerNode");
		

		buf.position(Header.NUMBER_OF_KEYS.getPosition());
		numberOfKeys = buf.getInt();

		
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
		
		getChildForKey(key);

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

	/* (non-Javadoc)
	 * @see com.freshbourne.io.MustInitializeOrLoad#initialize()
	 */
	@Override
	public void initialize() {
		ByteBuffer buf = rawPage().buffer();
		buf.position(Header.NODE_TYPE.getPosition());
		buf.putChar(NODE_TYPE.serialize());
		setNumberOfKeys(0);
		
		valid = true;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.btree.Node#getNumberOfUniqueKeys()
	 */
	@Override
	public int getNumberOfUniqueKeys() {
		return numberOfKeys;
	}
}
