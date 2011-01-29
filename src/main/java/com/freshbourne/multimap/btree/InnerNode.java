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
import com.freshbourne.io.DataPageManager;
import com.freshbourne.io.PageManager;
import com.freshbourne.io.PagePointer;
import com.freshbourne.io.RawPage;
import com.freshbourne.multimap.btree.AdjustmentAction.ACTION;
import com.freshbourne.multimap.btree.BTree.NodeType;
import com.freshbourne.serializer.FixLengthSerializer;
import com.freshbourne.serializer.Serializer;

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
public class InnerNode<K, V> implements Node<K,V>, ComplexPage {
	
	private static final NodeType NODE_TYPE = NodeType.INNER_NODE;
	
	private static enum Header{
		NODE_TYPE(0){}, // char
		NUMBER_OF_KEYS(1); // int
		
		private int offset;
		Header(int offset){
			this.offset = offset;
		}
		static int size(){return 5;}
		int getOffset(){return offset;}
	}
	
	private final RawPage rawPage;
	private final Comparator<K> comperator;
	private final FixLengthSerializer<PagePointer, byte[]> pointerSerializer;
	private final DataPageManager<K> keyPageManager;
	private final PageManager<LeafNode<K, V>> leafPageManager;
	private final PageManager<InnerNode<K, V>> innerNodePageManager;
	
	private int numberOfKeys;
	private boolean valid = false;
	
	InnerNode(
			RawPage rawPage, 
			FixLengthSerializer<PagePointer, byte[]> pointerSerializer,
			Comparator<K> comparator,
			DataPageManager<K> keyPageManager,
			PageManager<LeafNode<K, V>> leafPageManager,
			PageManager<InnerNode<K, V>> innerNodePageManager
	){
		this.leafPageManager = leafPageManager;
		this.innerNodePageManager = innerNodePageManager;
		this.keyPageManager = keyPageManager;
		this.rawPage = rawPage;
		this.comperator = comparator;
		this.pointerSerializer = pointerSerializer;
	}
	
	public void initRootState(PagePointer keyPointer, Long pageId1, Long pageId2){
		ensureValid();
		
		ByteBuffer buf = rawPage().bufferForWriting(Header.size());
		
		buf.putLong(pageId1);
		buf.put(pointerSerializer.serialize(keyPointer));
		buf.putLong(pageId2);
		
		setNumberOfKeys(1);
	}
	
		/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#getNumberOfEntries()
	 */
	@Override
	public int getNumberOfEntries() {
		ensureValid();

		throw new UnsupportedOperationException("recursive get number of entries not yet supported.");
		
	}
	
	private Long getPageIdForKey(K key){
		ByteBuffer buf = rawPage.bufferForReading(getOffsetOfPageIdForKey(key));
		return buf.getLong();
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#containsKey(java.lang.Object)
	 */
	@Override
	public boolean containsKey(K key) {
		ensureValid();

		return getPageForPageId(getPageIdForKey(key)).containsKey(key);
	}
	
	private Node<K, V> getPageForPageId(Long pageId){
		Node <K, V> result = innerNodePageManager.getPage(pageId);
		
		if(result == null)
			result = leafPageManager.getPage(pageId);
		
		if(result == null)
			throw new IllegalArgumentException("the requested pageId " + pageId + " is neither in InnerNodePageManager nor in LeafPageManager");
		
		return result;
	}
	
	private void writeNumberOfKeys() {
		ByteBuffer buf = rawPage.bufferForWriting(Header.NUMBER_OF_KEYS.getOffset());
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
		ensureRoot();
		
		long pageId = getPageIdForKey(key);
		Node<K,V> node = getPageForPageId(pageId);
		
		return node.get(key);
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#remove(java.lang.Object)
	 */
	@Override
	public int remove(K key) {
		ensureValid();
		Long id = getChildForKey(key);
		
		throw new UnsupportedOperationException();
	}
	
	private int getSizeOfPageId(){
		return Long.SIZE / 8;
	}
	
	private int offsetForKey(int i){
		return Header.size() + 
			((i+1) * getSizeOfPageId()) + // one id more that pages, the first id
			(i * getSizeOfSerializedPointer());
	}
	
	private int offsetForPageId(int i){
		return Header.size() + (i * getSizeOfPageId()) + (i == 0 ? 0 : (i-1) * getSizeOfSerializedPointer());
	}
	
	private int posOfFirstLargerKey(K key){
		for(int i = 0; i < numberOfKeys; i++){
			PagePointer pp = getPointerAtOffset(offsetForKey(i));
			if(comperator.compare(getKeyFromPagePointer(pp), key) > 0){
				return i;
			}
		}
		return -1;
	}
	
	private int posOfFirstLargerOrEqualKey(K key){
		if(comperator == null){
			throw new IllegalStateException("comparator must not be null");
		}
		
		if(key == null){
			throw new IllegalArgumentException("key must not be null");
		}
		
		for(int i = 0; i < numberOfKeys; i++){
			
			PagePointer pp = getPointerAtOffset(offsetForKey(i));
			K keyFromPointer = getKeyFromPagePointer(pp);
			if(keyFromPointer == null){
				throw new IllegalStateException("key retrieved from PagePointer " + pp + " must not be null!");
			}
			
			if(comperator.compare(keyFromPointer, key) >= 0){
				return i;
			}
		}
		return -1;
	}
	private Long getChildForKey(K key) {
		return getLeftPageIdOfKey(posOfFirstLargerKey(key));
	}
	
	private Long getLeftPageIdOfKey(int i) {
		return readPageId(getOffsetForLeftPageIdOfKey(i));
	}
	
	private int getOffsetForLeftPageIdOfKey(int i){
		return offsetForKey(i) - Long.SIZE / 8;
	}
	
	private int getOffsetForRightPageIdOfKey(int i){
		return offsetForKey(i) + getSizeOfSerializedPointer();
	}
	
	private int getSizeOfSerializedPointer(){
		return pointerSerializer.serializedLength(PagePointer.class);
	}
	
	private Long getRightPageIdOfKey(int i) {
		return readPageId(getOffsetForRightPageIdOfKey(i));
	}
	
	private Long readPageId(int offset){
		ByteBuffer buf = rawPage().bufferForReading(offset);
		return buf.getLong();
	}

	private K getKeyFromPagePointer(PagePointer pp) {
		return keyPageManager.getPage(pp.getId()).get(pp.getOffset());
	}

	private PagePointer getPointerAtOffset(int offset) {
		ByteBuffer buf = rawPage().bufferForReading(offset);
		byte[] byteBuf = new byte[getSizeOfSerializedPointer()];
		buf.get(byteBuf);
		return pointerSerializer.deserialize(byteBuf);
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.btree.Node#remove(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int remove(K key, V value) {
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
	 * @see com.freshbourne.io.ComplexPage#load()
	 */
	@Override
	public void load() throws IOException {
		ByteBuffer buf = rawPage.bufferForReading(0);
		if(NodeType.deserialize(buf.getChar()) != NODE_TYPE)
			throw new IOException("You are trying to load a InnerNode from a byte array, that does not contain an InnerNode");
		

		buf.position(Header.NUMBER_OF_KEYS.getOffset());
		numberOfKeys = buf.getInt();
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
	
	
	private int getOffsetOfPageIdForKey(K key){
		int posOfFirstLargerOrEqualKey = posOfFirstLargerOrEqualKey(key);
		
		if(posOfFirstLargerOrEqualKey < 0) // if key is largest
			return getOffsetForRightPageIdOfKey((getNumberOfKeys() - 1));
		
		
		return getOffsetForLeftPageIdOfKey(posOfFirstLargerOrEqualKey);
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.btree.Node#insert(java.lang.Object, java.lang.Object)
	 */
	@Override
	public AdjustmentAction<K, V> insert(K key, V value) {
		ensureValid();
		
		ensureRoot();
		
		int posOfFirstLargerOrEqualKey = posOfFirstLargerOrEqualKey(key);
		Long pageId;
		if(posOfFirstLargerOrEqualKey < 0) // if key is largest
			pageId = getRightPageIdOfKey(getNumberOfKeys());
		else
			pageId = getLeftPageIdOfKey(posOfFirstLargerOrEqualKey);
		
		LeafNode<K, V> leaf = leafPageManager.getPage(pageId);
		AdjustmentAction<K, V> result;
		
		if(leaf != null){
			result = leaf.insert(key, value);
		} else {
			InnerNode<K, V> innerNode = innerNodePageManager.getPage(pageId);
			result = innerNode.insert(key, value);
		}
		
		// insert worked fine, no adjustment
		if(result == null)
			return null;
		
		if(result.getAction() == ACTION.UPDATE_KEY){
			return updateKey(posOfFirstLargerOrEqualKey, result);
		}
		
		if(result.getAction() == ACTION.INSERT_NEW_NODE){
			// a new child node has been created, check for available space
			if(getNumberOfKeys() < getMaximalNumberOfKeys()){
				// space left, simply insert the key/pointer.
				// the key replaces the old key for our node, since the split caused a different
				// key to be the now highest in the subtree
				
				int posForInsert = posOfFirstLargerOrEqualKey == -1 ? getNumberOfKeys() + 1 : posOfFirstLargerOrEqualKey;
				insertKeyPointerPageIdAtPosition(
						result.getKeyPointer(), result.getPageId(),  posForInsert);
				rawPage().setModified(true);
				// no further adjustment necessary. even if we inserted to the last position, the
				// highest key in the subtree below is still the same, because otherwise we would
				// have never ended up here during the descend from the root, or we are in the
				// right-most path of the subtree.
				return null;
			}
		}
		
		throw new UnsupportedOperationException();
	}

	private void ensureRoot() {
		if(getNumberOfKeys() == 0)
			throw new IllegalStateException("use inizializeRootState() for the first insert!");
	}

	/**
	 * @param keyPointer
	 * @param pageId
	 * @param posOfKeyForInsert
	 */
	private void insertKeyPointerPageIdAtPosition(PagePointer keyPointer,
			Long pageId, int posOfKeyForInsert) {
		ByteBuffer buf = rawPage().bufferForWriting(offsetForKey(posOfKeyForInsert));
		
		throw new UnsupportedOperationException();
	}

	private int getMaximalNumberOfKeys() {
		int size = rawPage.bufferForReading(0).limit() - Header.size();
		
		// size first page id
		size -= Long.SIZE / 8;
		
		return size / (Long.SIZE / 8 + pointerSerializer.serializedLength(PagePointer.class));
	}

	private AdjustmentAction<K, V> updateKey(int posOfFirstLargerOrEqualKey, AdjustmentAction<K, V> result) {
		if(result.getAction() != ACTION.UPDATE_KEY)
			throw new IllegalArgumentException("action must be of type UPDATE_KEY");
		
		
		if(posOfFirstLargerOrEqualKey < 0){ // last page
			return result; // last page must be propagated up
		}
		
		// We need to adjust our own key, because keys were moved to the next node.
		// That changes the highest key in this page, so the corresponding key
		// must be adjusted.
		setKey(result.getKeyPointer(), posOfFirstLargerOrEqualKey);
		return null;
	}
	
	private void setKey(PagePointer pointer, int pos){
		ByteBuffer buf = rawPage().bufferForWriting(offsetForKey(pos));
		buf.put(pointerSerializer.serialize(pointer));
	}
	
	private void setPageId(Long pageId, int pos){
		ByteBuffer buf = rawPage().bufferForWriting(offsetForPageId(pos));
		buf.putLong(pageId);
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
		ByteBuffer buf = rawPage().bufferForWriting(Header.NODE_TYPE.getOffset());
		buf.putChar(NODE_TYPE.serialize());
		setNumberOfKeys(0);
		
		valid = true;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.btree.Node#getNumberOfKeys()
	 */
	@Override
	public int getNumberOfKeys() {
		return numberOfKeys;
	}
}
