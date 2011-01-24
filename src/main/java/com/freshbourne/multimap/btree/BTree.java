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

import com.freshbourne.multimap.MultiMap;
import com.freshbourne.multimap.btree.AdjustmentAction.ACTION;
import com.google.inject.Inject;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;


public class BTree<K, V> implements MultiMap<K, V> {

	private final LeafPageManager<K,V> leafPageManager;
	private final InnerNodeManager<K, V> innerNodeManager;
	private final Comparator<K> comparator;
	
	private LeafPage<K, V> root;
	
	
	@Inject
	BTree(LeafPageManager<K,V> leafPageManager, InnerNodeManager<K, V> innerNodeManager, Comparator<K> comparator) {
		this.leafPageManager = leafPageManager;
		this.innerNodeManager = innerNodeManager;
		this.comparator = comparator;
		root = leafPageManager.createPage();
	}
	
	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#size()
	 */
	@Override
	public int getNumberOfEntries() {
		return root.getNumberOfEntries();
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#containsKey(java.lang.Object)
	 */
	@Override
	public boolean containsKey(K key) throws Exception {
		return root.containsKey(key);
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#get(java.lang.Object)
	 */
	@Override
	public List<V> get(K key) throws Exception {
		return root.get(key);
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#add(java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean add(K key, V value) {
		AdjustmentAction<K, V> result = recursivelyInsert(key, value);
		
		if(result == null)
			return true;
		
		if(result.getAction() == ACTION.INSERT_NEW_NODE){
			// new root
			InnerNode<K, V> newRoot = innerNodeManager.createPage();
			
			newRoot.initRootState(result.getKeyPointer(), root.rawPage().id(), result.getPageId());
			
			
		}
		
		return false;
	}
	
	private AdjustmentAction<K, V> recursivelyInsert(K key, V value){
		if (root instanceof LeafPage) {
			if (!root.isFull()) {
				root.add(key, value);
				return null;
			} else if (root instanceof LeafPage && root.getNextLeafId() != null) {
				throw new UnsupportedOperationException(
						"push some entries to next leaf");
			} else {
				// allocate new leaf
				LeafPage<K,V> newLeaf = leafPageManager.createPage();
				newLeaf.setNextLeafId(root.rawPage().id());
				root.setNextLeafId(newLeaf.rawPage().id());
				
				// newLeaf.setLastKeyContinuesOnNextPage(root.isLastKeyContinuingOnNextPage());
				
				// move half of the keys to new page
				newLeaf.prependEntriesFromOtherPage(root, root.getNumberOfEntries() >> 1);
				
				// see on which page we will insert the value
				if(comparator.compare(key, root.getLastKey()) > 0){
					newLeaf.insert(key, value);
				} else {
					root.insert(key, value);
				}
				
				return new AdjustmentAction<K, V>(ACTION.INSERT_NEW_NODE, root.getLastKeyPointer(), newLeaf.rawPage().id());
			}
		} else {
			throw new UnsupportedOperationException(
					"innernodes not supported yet");
		}
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#remove(java.lang.Object)
	 */
	@Override
	public void remove(K key) throws Exception {
		root.remove(key);
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#remove(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void remove(K key, V value) throws Exception {
		root.remove(key, value);
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.MultiMap#clear()
	 */
	@Override
	public void clear() throws Exception {
		root.clear();
	}
	
	/**
	 * The maximum number of levels in the B-Tree. Used to prevent infinite loops when
	 * the structure is corrupted.
	 */
	private static final int MAX_BTREE_LEVELS = 50;
	
	/**
	 * If a leaf page is less full than this factor, it may be target of operations
	 * where entries are moved from one page to another. 
	 */
	private static final float MAX_LEAF_ENTRY_FILL_LEVEL_TO_MOVE = 0.75f;

}
