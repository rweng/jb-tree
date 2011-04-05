/*
 * Copyright (c) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 */

package com.freshbourne.multimap.btree;

import com.freshbourne.io.ComplexPage;
import com.freshbourne.io.PageManager;
import com.freshbourne.io.RawPage;
import com.freshbourne.multimap.MultiMap;
import com.freshbourne.multimap.btree.AdjustmentAction.ACTION;
import com.google.inject.Inject;
import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;


/**
 * An implementation of a Map that can hold more that one value for each key.
 * 
 * This class does all the rotating and balancing of the BTree so that Leafs and InnerNodes are not polluted by having
 * to create new nodes. This is done exclusively in this class.
 * 
 * @author Robin Wenglewski <robin@wenglewski.de>
 *
 * @param <K>
 * @param <V>
 */
public class BTree<K, V> implements MultiMap<K, V>, ComplexPage {

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
	BTree(PageManager<RawPage> bpm, LeafPageManager<K,V> leafPageManager, InnerNodeManager<K, V> innerNodeManager, Comparator<K> comparator) {
		this.leafPageManager = leafPageManager;
		this.innerNodeManager = innerNodeManager;
		this.comparator = comparator;
		this.bpm = bpm;
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
		
		setNumberOfEntries(getNumberOfEntries() + 1);
		
		AdjustmentAction<K, V> result = root.insert(key, value);
		
		// insert was successful
		if(result == null)
			return;
		
		// a new root must be created
		if(result.getAction() == ACTION.INSERT_NEW_NODE){
			
			@SuppressWarnings("unused")
			int i = root.getNumberOfEntries();
			i = leafPageManager.getPage(result.getPageId()).getNumberOfEntries();
			
			// new root
			InnerNode<K, V> newRoot = innerNodeManager.createPage();
			newRoot.initRootState(result.getKeyPointer(), root.getId(), result.getPageId());
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
		rawPage.bufferForReading(0).getInt();
		leafPageManager.sync();
		innerNodeManager.sync();
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
}
