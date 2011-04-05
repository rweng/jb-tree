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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.freshbourne.io.ComplexPage;
import com.freshbourne.io.DataPageManager;
import com.freshbourne.io.PageManager;
import com.freshbourne.io.PagePointer;
import com.freshbourne.io.RawPage;
import com.freshbourne.multimap.btree.AdjustmentAction.ACTION;
import com.freshbourne.multimap.btree.BTree.NodeType;
import com.freshbourne.serializer.FixLengthSerializer;

/**
 *
 * stores pointers to the keys that get push upwards to InnerNodes from LeafPages, as well as the id of nodes
 * in the following order:
 * 
 * NODE_TYPE | NUM_OF_KEYS | NODE_ID | KEY_POINTER | NODE_ID | KEY_POINTER | NODE_ID ...
 *
 * @author Robin Wenglewski <robin@wenglewski.de>
 *
 * @param <K>
 * @param <V>
 */
public class InnerNode<K, V> implements Node<K,V>, ComplexPage {
	
	private static final NodeType NODE_TYPE = NodeType.INNER_NODE;
	
	static enum Header{
		NODE_TYPE(0){}, // char
		NUMBER_OF_KEYS(Character.SIZE); // int
		
		private int offset;
		Header(int offset){
			this.offset = offset;
		}
		static int size(){return Character.SIZE + Integer.SIZE;}
		int getOffset(){return offset;}
	}
	
	private final RawPage rawPage;
	private final Comparator<K> comparator;
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
		
		if(comparator == null){
			throw new IllegalStateException("comparator must not be null");
		}
		
		
		this.leafPageManager = leafPageManager;
		this.innerNodePageManager = innerNodePageManager;
		this.keyPageManager = keyPageManager;
		this.rawPage = rawPage;
		this.comparator = comparator;
		this.pointerSerializer = pointerSerializer;
		
		
	}
	
	public void initRootState(PagePointer keyPointer, Integer pageId1, Integer pageId2){
		ensureValid();
		
		ByteBuffer buf = rawPage().bufferForWriting(Header.size());
		
		buf.putInt(pageId1);
		buf.put(pointerSerializer.serialize(keyPointer));
		buf.putInt(pageId2);
		
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
	
	private Integer getPageIdForKey(K key){
		ByteBuffer buf = rawPage.bufferForReading(getOffsetOfPageIdForKey(key));
		return buf.getInt();
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#containsKey(java.lang.Object)
	 */
	@Override
	public boolean containsKey(K key) {
		ensureValid();
		ensureKeyNotNull(key);
		
		return getPageForPageId(getPageIdForKey(key)).containsKey(key);
	}
	
	private void ensureKeyNotNull(K key){
		if(key == null){
			throw new IllegalArgumentException("key must not be null");
		}
	}
	
	private Node<K, V> getPageForPageId(Integer pageId){
		
		if(innerNodePageManager.hasPage(pageId)){
			return innerNodePageManager.getPage(pageId);
		}
		
		if(leafPageManager.hasPage(pageId))
			return leafPageManager.getPage(pageId);
		
		throw new IllegalArgumentException("the requested pageId " + pageId + " is neither in InnerNodePageManager nor in LeafPageManager");
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
		
		if(getNumberOfKeys() == 0)
			return new ArrayList<V>();
		
		Integer pageId = getPageIdForKey(key);
		Node<K,V> node = getPageForPageId(pageId);
		
		return node.get(key);
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#remove(java.lang.Object)
	 */
	@Override
	public int remove(K key) {
		ensureValid();
		
		if(getNumberOfKeys() == 0)
			return 0;
		
		Integer id = getPageIdForKey(key);
		Node<K,V>node = getPageForPageId(id);
		return node.remove(key);
	}
	
	private int getSizeOfPageId(){
		return Integer.SIZE / 8;
	}
	
	private int offsetForKey(int i){
		return Header.size() + 
			((i+1) * getSizeOfPageId()) + // one id more that pages, the first id
			(i * getSizeOfSerializedPointer());
	}
	
	private int posOfFirstLargerOrEqualKey(K key){
		
		for(int i = 0; i < getNumberOfKeys(); i++){
			
			PagePointer pp = getPointerAtOffset(offsetForKey(i));
			K keyFromPointer = getKeyFromPagePointer(pp);
			if(keyFromPointer == null){
				throw new IllegalStateException("key " + i + " retrieved from PagePointer " + pp + " must not be null!");
			}
			
			if(comparator.compare(keyFromPointer, key) >= 0){
				return i;
			}
		}
		return -1;
	}
	
	private Integer getLeftPageIdOfKey(int i) {
		return rawPage().bufferForReading(getOffsetForLeftPageIdOfKey(i)).getInt();
	}
	
	private int getOffsetForLeftPageIdOfKey(int i){
		return offsetForKey(i) - Integer.SIZE / 8;
	}
	
	private int getOffsetForRightPageIdOfKey(int i){
		return offsetForKey(i) + getSizeOfSerializedPointer();
	}
	
	private int getSizeOfSerializedPointer(){
		return pointerSerializer.serializedLength(PagePointer.class);
	}
	
	private Integer getRightPageIdOfKey(int i) {
		int offset = getOffsetForRightPageIdOfKey(i);
		return rawPage().bufferForReading(offset).getInt();
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
		Integer pageId;
		if(posOfFirstLargerOrEqualKey < 0) // if key is largest
			pageId = getRightPageIdOfKey(getNumberOfKeys() - 1);
		else
			pageId = getLeftPageIdOfKey(posOfFirstLargerOrEqualKey);
		
		if(pageId <= 0){
			throw new IllegalArgumentException("pageId must not be 0 ( posOfFirstLargerOrEqualKey: " + posOfFirstLargerOrEqualKey + " )");
		}
		
		if(!leafPageManager.hasPage(pageId))
			throw new UnsupportedOperationException("we cant load from next innerNode yet");
		
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
				
				int posForInsert = posOfFirstLargerOrEqualKey == -1 ? getNumberOfKeys() : posOfFirstLargerOrEqualKey;
				insertKeyPointerPageIdAtPosition(
						result.getKeyPointer(), result.getPageId(),  posForInsert);
				
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
			Integer pageId, int posOfKeyForInsert) {
		
		ByteBuffer buf = rawPage().bufferForWriting(offsetForKey(posOfKeyForInsert));
		
		int spaceNeededForInsert = getSizeOfPageId() + getSizeOfSerializedPointer();
		System.arraycopy(buf.array(), buf.position(), buf.array(), buf.position() + spaceNeededForInsert, buf.limit() - buf.position() - spaceNeededForInsert);
		
		buf.put(pointerSerializer.serialize(keyPointer));
		buf.putInt(pageId);
		
		setNumberOfKeys(getNumberOfKeys() + 1);
	}

	private int getMaximalNumberOfKeys() {
		int size = rawPage.bufferForReading(0).limit() - Header.size();
		
		// size first page id
		size -= Integer.SIZE / 8;
		
		return size / (Integer.SIZE / 8 + pointerSerializer.serializedLength(PagePointer.class));
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
	public Integer getId() {
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

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.btree.Node#getFirstKey()
	 */
	@Override
	public K getFirstLeafKey() {
		ByteBuffer buf = rawPage().bufferForReading(getOffsetForLeftPageIdOfKey(0));
		return getPageForPageId(buf.getInt()).getFirstLeafKey();
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.btree.Node#getLastKey()
	 */
	@Override
	public K getLastLeafKey() {
		ByteBuffer buf = rawPage().bufferForReading(getOffsetForRightPageIdOfKey(getNumberOfKeys()) - 1);
		return getPageForPageId(buf.getInt()).getFirstLeafKey();
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.btree.Node#getIterator(java.lang.Object, java.lang.Object)
	 */
	@Override
	public Iterator<V> getIterator(K from, K to) {
		return getPageForPageId(getLeftPageIdOfKey(0)).getIterator(from, to);
	}
}
