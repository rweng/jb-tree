/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */

package com.freshbourne.multimap.btree;

import com.freshbourne.io.ComplexPage;
import com.freshbourne.io.FileResourceManager;
import com.freshbourne.io.PageManager;
import com.freshbourne.io.RawPage;
import com.freshbourne.multimap.KeyValueObj;
import com.freshbourne.multimap.MultiMap;
import com.freshbourne.multimap.btree.AdjustmentAction.ACTION;
import com.freshbourne.serializer.IntegerSerializer;
import com.google.inject.Inject;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class BTree<K, V> implements MultiMap<K, V>, ComplexPage {

	private static final Log LOG = LogFactory.getLog(BTree.class);
	
	/**
	 * This enum is used to make it possible for all nodes in the BTree to serialize and deserialize
	 * in a unique fashion
	 * 
	 * @author Robin Wenglewski <robin@wenglewski.de>
	 *
	 */
	public enum NodeType {
		LEAF_NODE('L'), INNER_NODE('I');
		
		private final char serialized;
		NodeType(char value){
			this.serialized = value;
		}
		
		public char serialize(){
			return serialized;
		}
		
		public static NodeType deserialize(char serialized){
			for(NodeType nt : values())
				if(nt.serialized == serialized)
					return nt;
			
			return null;
		}
	}
	
	private final LeafPageManager<K,V> leafPageManager;
	private final InnerNodeManager<K, V> innerNodeManager;
	private final Comparator<K> comparator;
	private final PageManager<RawPage> bpm;
	private RawPage rawPage;
	
	private Node<K, V> root;
	
	private boolean valid = false;
	private int numberOfEntries = 0;
	
	
	/**
	 * @param bpm for getting a rawPage for storing meta-information like size and depth of the tree and root page
	 * @param leafPageManager
	 * @param innerNodeManager
	 * @param comparator
	 */
	@Inject
	BTree(PageManager<RawPage> bpm, LeafPageManager<K,V> leafPageManager, 
			InnerNodeManager<K, V> innerNodeManager, Comparator<K> comparator) {
		this.leafPageManager = leafPageManager;
		this.innerNodeManager = innerNodeManager;
		this.comparator = comparator;
		this.bpm = bpm;
	}

    public Comparator<K> getKeyComparator(){
        return this.comparator;
    }
	
	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#size()
	 */
	@Override
	public int getNumberOfEntries() {
		ensureValid();
		return numberOfEntries;
	}

	private void ensureValid() {
		if(!isValid())
			throw new IllegalStateException("Btree must be initialized or loaded");
	}

	public void bulkInitialize(KeyValueObj<K, V>[] kvs, boolean sorted){
		if(!sorted)
			throw new IllegalArgumentException("KeyValueObjects must be sorted for bulkInsert to work right now");
		
		initialize();
		setNumberOfEntries(kvs.length);
		
		if(kvs.length == 0){
			return;
		}
		
		LeafNode<K, V> leafPage;
		ArrayList<byte[]> rawKeys = new ArrayList<byte[]>();
		ArrayList<Integer> pageIds = new ArrayList<Integer>();
		HashMap<Integer, byte[]> smallestKeyOfNode = new HashMap<Integer, byte[]>();
		HashMap<Integer, byte[]> largestKeyOfNode = new HashMap<Integer, byte[]>();
		
		
		// first insert all leafs and remember the insertedLastKeys
		int inserted = 0;
		LeafNode<K, V> previousLeaf = null;
		while(inserted < kvs.length){
			leafPage = leafPageManager.createPage(false);
			inserted += leafPage.bulkInitialize(kvs, inserted);
			
			largestKeyOfNode.put(leafPage.getId(), leafPage.getLastSerializedKey());
			rawKeys.add(leafPage.getLastSerializedKey());

			// LOG.debug("largest key of page " + leafPage.getId() + " = " + leafPage.getLastLeafKey());
			
			// set nextLeafId of previous leaf
			// dont store the first key
			if(previousLeaf != null){
				previousLeaf.setNextLeafId(leafPage.getId());
				// rawKeys.add(leafPage.getFirstSerializedKey());
			}
			
			previousLeaf = leafPage;
			pageIds.add(leafPage.getId());
		}

		// we are done if everything fits in one leaf
		if(pageIds.size() == 1){
			root = leafPageManager.getPage(pageIds.get(0));
			return;
		}
		
		// if not, build up tree
		InnerNode<K, V> node = null;
		
		while(pageIds.size() > 1){
			LOG.debug("next inner node layer");
			ArrayList<Integer> newPageIds = new ArrayList<Integer>();
			ArrayList<byte[]> newRawKeys = new ArrayList<byte[]>();
			inserted = 0; // page ids
			
			// we assume that from each pageId the largest key was stored, we need to remove the last one for innernode bulkinsert
			rawKeys.remove(rawKeys.size() - 1);

			LOG.debug("new pageIds.size: " + pageIds.size());
			LOG.debug("new rawKeys.size: " + rawKeys.size());

			// fill the inner node row
			while(inserted < pageIds.size()){
				
				// create a inner node and store the smallest key
				node = innerNodeManager.createPage(false);
				newPageIds.add(node.getId());

				inserted += node.bulkInitialize(rawKeys, pageIds, inserted);
				LOG.debug("inserted " + inserted + " in inner node, pageIds.size()=" + pageIds.size());
				
				// byte[] smallestKey = smallestKeyOfNode.get(pageIds.get(inserted));
				// smallestKeyOfNode.put(node.getId(), smallestKey);

				byte[] largestKey = largestKeyOfNode.get(pageIds.get(inserted - 1));
				largestKeyOfNode.put(node.getId(), largestKey);
				newRawKeys.add(largestKey);

				if(pageIds.size() == 4){
					LOG.debug("largest key of current node: " + IntegerSerializer.INSTANCE.deserialize(largestKey));
				}
			}
			
			// next turn, insert the ids of the pages we just created
			pageIds = newPageIds;
			rawKeys = newRawKeys;
		}
		
		// here, pageIds should be 1, and the page should be an inner node
		if(pageIds.size() == 1){
			root = innerNodeManager.getPage(pageIds.get(0));
			return;
		}
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#containsKey(java.lang.Object)
	 */
	@Override
	public boolean containsKey(K key) {
		ensureValid();
		
		return root.containsKey(key);
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#get(java.lang.Object)
	 */
	@Override
	public List<V> get(K key) {
		ensureValid();
		
		return root.get(key);
	}



	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#add(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void add(K key, V value) {
		ensureValid();
		
		LOG.debug("adding key/value: " + key.toString() + "/" + value.toString());
		
		setNumberOfEntries(getNumberOfEntries() + 1);
		
		AdjustmentAction<K, V> result = root.insert(key, value);
		
		// insert was successful
		if(result == null)
			return;
		
		// a new root must be created
		if(result.getAction() == ACTION.INSERT_NEW_NODE){
			
			@SuppressWarnings("unused")
			int i = getNumberOfEntries();
			i = leafPageManager.getPage(result.getPageId()).getNumberOfEntries();
			
			// new root
			InnerNode<K, V> newRoot = innerNodeManager.createPage();
			newRoot.initRootState(root.getId(), result.getSerializedKey(), result.getPageId());
			root = newRoot;
		}
		
	}
	
	/**
	 * @param i
	 */
	private void setNumberOfEntries(int i) {
		numberOfEntries = i;
		rawPage().bufferForWriting(0).putInt(numberOfEntries);
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#remove(java.lang.Object)
	 */
	@Override
	public void remove(K key) {
		ensureValid();
		
		numberOfEntries -= root.remove(key);
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#remove(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void remove(K key, V value) {
		ensureValid();

		setNumberOfEntries(getNumberOfEntries() - root.remove(key, value));
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#clear()
	 */
	@Override
	public void clear() {
		ensureValid();
		
		root.destroy();
		
		root = leafPageManager.createPage();
		numberOfEntries = 0;
	}
	
	/**
	 * The maximum number of levels in the B-Tree. Used to prevent infinite loops when
	 * the structure is corrupted.
	 */
	private static final int MAX_BTREE_DEPTH = 50;
	
	/* (non-Javadoc)
	 * @see com.freshbourne.io.ComplexPage#initialize()
	 */
	@Override
	public void initialize() {
		numberOfEntries = 0;
		valid = true;
		
		if(bpm.hasPage(1))
			rawPage = bpm.getPage(1);
		else
			rawPage = bpm.createPage();
		
		if(rawPage.id() != 1)
			throw new IllegalStateException("rawPage must have id 1");
		
		root = leafPageManager.createPage();
		
		ByteBuffer buffer = rawPage.bufferForWriting(0);
		buffer.putInt(numberOfEntries);
		buffer.putInt(root.getId());
	}
	
	/* (non-Javadoc)
	 * @see com.freshbourne.io.ComplexPage#load()
	 */
	@Override
	public void load() {
		rawPage = bpm.getPage(1);
		
		valid = true;
		numberOfEntries = rawPage.bufferForReading(0).getInt();
		
		int rootId = rawPage().bufferForReading(4).getInt();
		if(leafPageManager.hasPage(rootId)){
			root = leafPageManager.getPage(rootId);
		} else if(innerNodeManager.hasPage(rootId)){
			root = innerNodeManager.getPage(rootId);
		} else {
			throw new IllegalStateException("the root page should exist");
		}
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.ComplexPage#isValid()
	 */
	@Override
	public boolean isValid() {
		return valid ;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.ComplexPage#rawPage()
	 */
	@Override
	public RawPage rawPage() {
		ensureValid();
		
		return rawPage;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#sync()
	 */
	@Override
	public void sync() {
		bpm.sync();
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#getIterator()
	 */
	@Override
	public Iterator<V> getIterator() {
		return getIterator(root.getFirstLeafKey(), root.getLastLeafKey());
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#getIterator(java.lang.Object, java.lang.Object)
	 */
	@Override
	public Iterator<V> getIterator(K from, K to) {
		Iterator<V> result = root.getIterator(from, to);
		return result;
	}

	public String getPath(){
		return ((FileResourceManager)bpm).getFile().getAbsolutePath();
	}
}
