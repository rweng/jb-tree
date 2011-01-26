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

import com.freshbourne.io.BufferPoolManager;
import com.freshbourne.io.ComplexPage;
import com.freshbourne.io.RawPage;
import com.freshbourne.multimap.MultiMap;
import com.freshbourne.multimap.btree.AdjustmentAction.ACTION;
import com.google.inject.Inject;
import java.nio.ByteBuffer;
import java.util.Comparator;
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

	private final LeafPageManager<K,V> leafPageManager;
	private final InnerNodeManager<K, V> innerNodeManager;
	private final Comparator<K> comparator;
	private final BufferPoolManager bpm;
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
	BTree(BufferPoolManager bpm, LeafPageManager<K,V> leafPageManager, InnerNodeManager<K, V> innerNodeManager, Comparator<K> comparator) {
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
	public boolean containsKey(K key) throws Exception {
		ensureValid();
		
		return root.containsKey(key);
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#get(java.lang.Object)
	 */
	@Override
	public List<V> get(K key) throws Exception {
		ensureValid();
		
		return root.get(key);
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#add(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void add(K key, V value) {
		ensureValid();
		
		numberOfEntries++;
		
		AdjustmentAction<K, V> result = root.insert(key, value);
		
		// insert was successful
		if(result == null)
			return;
		
		// a new root must be created
		if(result.getAction() == ACTION.INSERT_NEW_NODE){
			
			// new root
			InnerNode<K, V> newRoot = innerNodeManager.createPage();
			newRoot.initRootState(result.getKeyPointer(), root.getId(), result.getPageId());
			root = newRoot;
		}
		
	}
	
	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#remove(java.lang.Object)
	 */
	@Override
	public void remove(K key) throws Exception {
		ensureValid();
		
		numberOfEntries -= root.remove(key);
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#remove(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void remove(K key, V value) throws Exception {
		ensureValid();
		
		root.remove(key, value);
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#clear()
	 */
	@Override
	public void clear() throws Exception {
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
		
		rawPage = bpm.createPage();
		root = leafPageManager.createPage();
		
		writeHeader();
	}
	
	private void writeHeader(){
		buffer().position(0);
		buffer().putInt(numberOfEntries);
		buffer().putLong(root.getId());
	}
	
	private ByteBuffer buffer(){return rawPage.buffer();}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.ComplexPage#load()
	 */
	@Override
	public void load() {
		buffer().position(0);
		numberOfEntries = buffer().getInt();
		
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

}
