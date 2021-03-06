/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 *
 * http://creativecommons.org/licenses/by-nc/3.0/
 *
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2011 "Robin Wenglewski <robin@wenglewski.de>"
 */

package de.rwhq.btree;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import de.rwhq.btree.AdjustmentAction.ACTION;
import de.rwhq.io.MustInitializeOrLoad;
import de.rwhq.io.rm.DataPageManager;
import de.rwhq.io.rm.FileResourceManager;
import de.rwhq.io.rm.RawPage;
import de.rwhq.io.rm.ResourceManager;
import de.rwhq.serializer.FixLengthSerializer;
import de.rwhq.serializer.PagePointSerializer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.ranges.RangeException;

import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.*;

import static com.google.common.base.Preconditions.*;


/**
 * The Btree page, all leafs and innernodes have to be stored in the same RawPageManager. We used to have it differently
 * but it is simpler this way. Now The BTree can make sure that all use the same serializers and comparators.
 * <p/>
 * Header: NUM_OF_ENTRIES ROOT_ID (here comes serializers etc)
 *
 * @param <K>
 * @param <V>
 */
public class BTree<K, V> implements MultiMap<K, V>, MustInitializeOrLoad {

	private static final Log LOG = LogFactory.getLog(BTree.class);


	/**
	 * This is the probably least verbose method for creating BTrees. It accepts a file versus the FileResourceManager of
	 * the constructor. In addition, one does not have to repeat the generic on the right hand side of the creation
	 * assignment.
	 *
	 * @param rm
	 * 		resourceManager
	 * @param keySerializer
	 * @param valueSerializer
	 * @param comparator
	 * @param <K>
	 * @param <V>
	 * @return a new BTree instance
	 *
	 * @throws IOException
	 */
	public static <K, V> BTree<K, V> create(final ResourceManager rm, final FixLengthSerializer<K, byte[]> keySerializer,
	                                        final FixLengthSerializer<V, byte[]> valueSerializer,
	                                        final Comparator<K> comparator) throws IOException {

		checkNotNull(rm);
		checkNotNull(keySerializer);
		checkNotNull(valueSerializer);
		checkNotNull(comparator);

		if (!rm.isOpen())
			rm.open();

		return new BTree<K, V>(rm, keySerializer, valueSerializer, comparator);
	}

	private final LeafPageManager<K, V>  leafPageManager;
	private final InnerNodeManager<K, V> innerNodeManager;
	private final Comparator<K>          comparator;
	private final ResourceManager rm;
	private RawPage rawPage;

	private Node<K, V> root;

	private boolean valid           = false;
	private int     numberOfEntries = 0;
	private FixLengthSerializer<K, byte[]> keySerializer;
	private FixLengthSerializer<V, byte[]> valueSerializer;

	/* (non-Javadoc)
		  * @see MultiMap#size()
		  */
	@Override
	public int getNumberOfEntries() {
		ensureValid();
		return numberOfEntries;
	}

	/* (non-Javadoc)
		  * @see com.rwhq.btree.MultiMap#containsKey(java.lang.Object)
		  */
	@Override
	public boolean containsKey(final K key) {
		ensureValid();

		return root.containsKey(key);
	}

	/* (non-Javadoc)
		  * @see MultiMap#get(java.lang.Object)
		  */
	@Override
	public List<V> get(final K key) {
		ensureValid();

		return root.get(key);
	}


	/* (non-Javadoc)
		  * @see MultiMap#add(java.lang.Object, java.lang.Object)
		  */
	@Override
	public void add(final K key, final V value) {
		ensureValid();

		setNumberOfEntries(getNumberOfEntries() + 1);

		final AdjustmentAction<K, V> result = root.insert(key, value);

		// insert was successful
		if (result == null){
			rawPage.sync();
			return;
		}

		// a new root must be created
		if (result.getAction() == ACTION.INSERT_NEW_NODE) {
			// new root
			final InnerNode<K, V> newRoot = innerNodeManager.createPage();
			newRoot.initRootState(root.getId(), result.getSerializedKey(), result.getPageId());
			setRoot(newRoot);
		}

		rawPage.sync();
	}

	/* (non-Javadoc)
		  * @see MultiMap#remove(java.lang.Object)
		  */
	@Override
	public void remove(final K key) {
		ensureValid();

		numberOfEntries -= root.remove(key);
	}

	/* (non-Javadoc)
		  * @see MultiMap#remove(java.lang.Object, java.lang.Object)
		  */
	@Override
	public void remove(final K key, final V value) {
		ensureValid();

		setNumberOfEntries(getNumberOfEntries() - root.remove(key, value));
		rawPage.sync();
	}

	/* (non-Javadoc)
		  * @see MultiMap#clear()
		  */
	@Override
	public void clear() throws IOException {
		ensureValid();
		rm.clear();
		valid = false;
		initialize();
		// just set another root, the other pages stay in the file
		// LOG.info("BTree#clear() is not fully implemented yet because" +
		// 		" it is not possible to remove entries from the FileResourceManager");
	}

	/* (non-Javadoc)
		  * @see MultiMap#getIterator()
		  */
	@Override
	public Iterator<V> getIterator() {
		return getIterator(root.getFirstLeafKey(), root.getLastLeafKey());
	}

	/* (non-Javadoc)
		  * @see MultiMap#getIterator(java.lang.Object, java.lang.Object)
		  */
	@Override
	public Iterator<V> getIterator(final K from, final K to) {
		ensureValid();
				
		final Iterator<V> result = root.getIterator(from, to);
		return result;
	}

	/* (non-Javadoc)
		  * @see ComplexPage#initialize()
		  */
	@Override
	public void initialize() throws IOException {
		checkState(!valid, "tree is already valid: %s", this);
		
		preInitialize();
		setRoot(leafPageManager.createPage());
		setNumberOfEntries(0);
		rawPage.sync();
	}

	/* (non-Javadoc)
		  * @see ComplexPage#load()
		  */
	@Override
	public void load() throws IOException {
		checkState(!valid, "BTree is already loaded: %s", this);
		
		if (LOG.isDebugEnabled())
			LOG.debug("loading BTree");

		if (!rm.isOpen())
			rm.open();

		if (!rm.hasPage(1)) {
			throw new IOException("Page 1 could not be found. Ensure that the BTree is initialized");
		}


		rawPage = rm.getPage(1);
		numberOfEntries = rawPage.bufferForReading(0).getInt();

		final int rootId = rawPage.bufferForReading(4).getInt();
		if (leafPageManager.hasPage(rootId)) {
			root = leafPageManager.getPage(rootId);
		} else if (innerNodeManager.hasPage(rootId)) {
			root = innerNodeManager.getPage(rootId);
		} else {
			throw new IllegalStateException(
					"Page 1 does exist, but is neither a leafPage nor a innerNodePage. This could be the result of an unclosed B-Tree.");
		}

		valid = true;

		if (LOG.isDebugEnabled()) {
			LOG.debug("BTree loaded: ");
			LOG.debug("Number of Values: " + numberOfEntries);
			LOG.debug("root (id: " + root.getId() + "): " + root);
		}
	}

	/* (non-Javadoc)
		  * @see ComplexPage#isValid()
		  */
	@Override
	public boolean isValid() {
		return valid;
	}

	@Override
	public void loadOrInitialize() throws IOException {
		try {
			load();
		} catch (IOException e) {
			initialize();
		}
	}

	/**
	 * sync, close the ResourceManager and set to invalid
	 *
	 * @throws IOException
	 */
	public void close() throws IOException {
		rm.close();
		valid = false;
	}

	public int getMaxInnerKeys() {
		final int realSize = rm.getPageSize() - InnerNode.Header.size() - Integer.SIZE / 8;
		return realSize / (Integer.SIZE / 8 + keySerializer.getSerializedLength());
	}

	public int getMaxLeafKeys() {
		final int realSize = rm.getPageSize() - LeafNode.Header.size();
		return realSize / (keySerializer.getSerializedLength() + valueSerializer.getSerializedLength());
	}

	public FixLengthSerializer<K, byte[]> getKeySerializer() {
		return keySerializer;
	}

	public FixLengthSerializer<V, byte[]> getValueSerializer() {
		return valueSerializer;
	}

	@VisibleForTesting
	public LeafPageManager<K, V> getLeafPageManager() {
		return leafPageManager;
	}

	@VisibleForTesting
	public InnerNodeManager<K, V> getInnerNodeManager() {
		return innerNodeManager;
	}

	public int getDepth() {
		return root.getDepth();
	}

	public Comparator<K> getKeyComparator() {
		return this.comparator;
	}

	public void bulkInitialize(final SimpleEntry<K, ?>[] kvs, final boolean sorted) throws IOException {
		bulkInitialize(kvs, 0, kvs.length - 1, sorted);
	}

	/**
	 * Bulk initialize first creates all leafs, then goes the tree up toIndex create the InnerNodes.
	 *
	 * @param kvs
	 * @param fromIndex
	 * 		including
	 * @param toIndex
	 * 		including
	 * @param sorted
	 * @throws IOException
	 */
	public void bulkInitialize(final SimpleEntry<K, ?>[] kvs, final int fromIndex, final int toIndex, final boolean sorted) throws IOException {
		LOG.info("bulkInitializing BTree: " + this);
		
		checkState(!valid, "BTree is already loaded: %s", this);

		for(int i=fromIndex;i<=toIndex;i++){
			checkNotNull(kvs[i], "array given to bulkInitialize must not contain null values");
		}

		final int count = toIndex - fromIndex + 1;
		if (count < 0)
			throw new IllegalArgumentException(
					"fromIndex(" + fromIndex + ") must be smaller or equal to toIndex(" + toIndex + ")");

		// sort if not already sorted
		if (!sorted) {
			Arrays.sort(kvs, fromIndex, toIndex + 1, // +1 because excluding toIndex
					new Comparator<SimpleEntry<K, ?>>() {
						@Override
						public int compare(final SimpleEntry<K, ?> kvSimpleEntry, final SimpleEntry<K, ?> kvSimpleEntry1) {
							return comparator.compare(kvSimpleEntry.getKey(), kvSimpleEntry1.getKey());
						}
					});
		}

		// initialize but do not create a root page or set the number of keys
		preInitialize();
		setNumberOfEntries(count);

		if (getNumberOfEntries() == 0) {
			rawPage.sync();
			return;
		}

		LeafNode<K, V> leafPage;
		ArrayList<byte[]> keysForNextLayer = new ArrayList<byte[]>();
		ArrayList<Integer> pageIds = new ArrayList<Integer>();
		final HashMap<Integer, byte[]> pageIdToSmallestKeyMap = new HashMap<Integer, byte[]>();


		// first insert all leafs and remember the insertedLastKeys
		int inserted = 0;
		LeafNode<K, V> previousLeaf = null;
		while (inserted < getNumberOfEntries()) {
			leafPage = leafPageManager.createPage(false);

			inserted += leafPage.bulkInitialize(kvs, inserted + fromIndex, toIndex);

			pageIdToSmallestKeyMap.put(leafPage.getId(), leafPage.getFirstLeafKeySerialized());

			// set nextLeafId of previous leaf
			// dont store the first key
			if (previousLeaf != null) {
				// next layer doesn't need the first key
				keysForNextLayer.add(leafPage.getFirstLeafKeySerialized());
				previousLeaf.setNextLeafId(leafPage.getId());
			}

			previousLeaf = leafPage;
			pageIds.add(leafPage.getId());
			leafPage.rawPage().sync();
		}

		// we are done if everything fits in one leaf
		if (pageIds.size() == 1) {
			setRoot(leafPageManager.getPage(pageIds.get(0)));
			rawPage.sync();
			return;
		}

		// if not, build up tree
		InnerNode<K, V> node;

		// for each layer, if pageId == 1, this page becomes the root
		while (pageIds.size() > 1) {
			if (LOG.isDebugEnabled())
				LOG.debug("next inner node layer");

			final ArrayList<Integer> newPageIds = new ArrayList<Integer>();
			final ArrayList<byte[]> newKeysForNextLayer = new ArrayList<byte[]>();
			inserted = 0; // page ids

			// we assume that fromIndex each pageId the smallest key was stored, we need to remove the last one for InnerNode#bulkinsert()
			if (LOG.isDebugEnabled()) {
				LOG.debug("new pageIds.size: " + pageIds.size());
				LOG.debug("new keysForNextLayer.size: " + keysForNextLayer.size());
			}

			// fill the layer row while we have pageIds to insert left
			while (inserted < pageIds.size()) {

				// create a inner node and store the smallest key
				node = innerNodeManager.createPage(false);
				newPageIds.add(node.getId());
				final byte[] smallestKey = pageIdToSmallestKeyMap.get(pageIds.get(inserted));
				pageIdToSmallestKeyMap.put(node.getId(), smallestKey);

				// dont insert the first small key to the keys for the next layer
				if (inserted > 0)
					newKeysForNextLayer.add(smallestKey);

				inserted += node.bulkInitialize(keysForNextLayer, pageIds, inserted);
				
				if (LOG.isDebugEnabled())
					LOG.debug("inserted " + inserted + " in inner node, pageIds.size()=" + pageIds.size());
			}

			// next turn, insert the ids of the pages we just created
			pageIds = newPageIds;
			keysForNextLayer = newKeysForNextLayer;
		}

		// here, pageIds should be 1, and the page should be an inner node
		if (pageIds.size() == 1) {
			setRoot(innerNodeManager.getPage(pageIds.get(0)));
			rawPage.sync();
			return;
		}
	}

	public String toString(){
		final Objects.ToStringHelper helper = Objects.toStringHelper(this);
		if(isValid()){
			helper.add("numberOfEntries", getNumberOfEntries());
			helper.add("root", root);
		}
		helper.add("resourceManager", rm);
		helper.add("valid", valid);
		return helper.toString();
	}

	public void checkStructure() throws IllegalStateException {
		root.checkStructure();
	}


	public Iterator<V> getIterator(final Collection<Range<K>> ranges) {
		return new BTreeIterator(ranges);
	}

	ResourceManager getResourceManager() {
		return rm;
	}

	static enum Header {
		NUM_OF_ENTRIES(0),
		ROOT_ID(Integer.SIZE / 8);

		static int size() {
			return (2 * Integer.SIZE) / 8;
		} // 8

		private int offset;

		int getOffset() {
			return offset;
		}

		private Header(final int offset) {
			this.offset = offset;
		}
	}

	/**
	 * This enum is used to make it possible for all nodes in the BTree to serialize and deserialize in a unique fashion
	 *
	 * @author Robin Wenglewski <robin@wenglewski.de>
	 */
	static enum NodeType {
		LEAF_NODE('L'), INNER_NODE('I');

		public static NodeType deserialize(final char serialized) {
			for (final NodeType nt : values())
				if (nt.serialized == serialized)
					return nt;

			return null;
		}

		private final char serialized;

		public char serialize() {
			return serialized;
		}

		NodeType(final char value) {
			this.serialized = value;
		}
	}

	private class BTreeIterator implements Iterator<V> {

		private Collection<Range<K>> ranges;
		
		private Iterator<V> currentIterator = null;
		private Iterator<Range<K>> rangesIterator;


		@Override public boolean hasNext() {
			if (currentIterator == null) {
				if (!rangesIterator.hasNext())
					return false;
				else {
					final Range<K> range = rangesIterator.next();
					currentIterator = root.getIterator(range.getFrom(), range.getTo());
				}
			}

			if (currentIterator.hasNext())
				return true;

			currentIterator = null;
			return hasNext();
		}

		@Override public V next() {
			if (!hasNext())
				return null;
			else
				return currentIterator.next();
		}

		@Override public void remove() {
			throw new UnsupportedOperationException();
		}

		public BTreeIterator(final Collection<Range<K>> ranges) {
			this.ranges = Range.merge(ranges, comparator);

			if(ranges.isEmpty()){
				this.ranges.add(new Range(null, null));
			}

			this.rangesIterator = this.ranges.iterator();
		}
	}

	/**
	 * This constructor is for manual construction.
	 *
	 * @param rm
	 * @param keySerializer
	 * @param valueSerializer
	 * @param comparator
	 */
	private BTree(final ResourceManager rm,
	              final FixLengthSerializer<K, byte[]> keySerializer, final FixLengthSerializer<V, byte[]> valueSerializer,
	              final Comparator<K> comparator) {

		this.rm = rm;
		this.keySerializer = keySerializer;
		this.valueSerializer = valueSerializer;
		this.comparator = comparator;

		final DataPageManager<K> keyPageManager = new DataPageManager<K>(rm, PagePointSerializer.INSTANCE, keySerializer);
		final DataPageManager<V> valuePageManager =
				new DataPageManager<V>(rm, PagePointSerializer.INSTANCE, valueSerializer);

		leafPageManager = new LeafPageManager<K, V>(rm, valueSerializer, keySerializer, comparator);
		innerNodeManager =
				new InnerNodeManager(rm, keyPageManager, valuePageManager, leafPageManager, keySerializer, comparator);

		if (LOG.isDebugEnabled()) {
			LOG.debug("BTree created: ");
			LOG.debug("key serializer: " + keySerializer);
			LOG.debug("value serializer: " + valueSerializer);
			LOG.debug("comparator: " + comparator);
		}
	}

	private void ensureValid() {
		checkState(isValid(), "Btree must be initialized or loaded");
	}

	private void setRoot(final Node<K, V> root) {
		this.root = root;
		rawPage.bufferForWriting(Header.ROOT_ID.getOffset()).putInt(root.getId());
	}

	/**
	 * Loads a node, either as leaf or as innernode
	 *
	 * @param id
	 * @return
	 */
	private Node<K, V> getNode(final int id) {
		if (leafPageManager.hasPage(id))
			return leafPageManager.getPage(id);
		else
			return innerNodeManager.getPage(id);
	}

	/** @param i */
	private void setNumberOfEntries(final int i) {
		numberOfEntries = i;
		rawPage.bufferForWriting(Header.NUM_OF_ENTRIES.getOffset()).putInt(numberOfEntries);
	}

	/**
	 * opens the ResourceManager, sets the rawPage and sets valid, but does not create a root leaf or set the number of
	 * entries
	 * @throws java.io.IOException
	 */
	private void preInitialize() throws IOException {
		if (!rm.isOpen())
			rm.open();

		if (rm.hasPage(1))
			rawPage = rm.getPage(1);
		else
			rawPage = rm.createPage();

		if (rawPage.id() != 1)
			throw new IllegalStateException("rawPage must have id 1");

		valid = true;
	}
}
