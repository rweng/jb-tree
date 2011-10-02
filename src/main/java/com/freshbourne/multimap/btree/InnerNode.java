/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
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
import org.apache.log4j.Logger;

/**
 * stores pointers to the keys that get push upwards to InnerNodes from LeafPages, as well as the id of nodes
 * in the following order:
 * <p/>
 * NODE_TYPE | NUM_OF_KEYS | NODE_ID | KEY_POINTER | NODE_ID | KEY_POINTER | NODE_ID ...
 * <p/>
 * If the search/insert key is equal to the currently checked key, go to the left.
 *
 * @param <K>
 * @param <V>
 */
public class InnerNode<K, V> implements Node<K, V>, ComplexPage {

    private static final NodeType NODE_TYPE = NodeType.INNER_NODE;
    private static final Logger   LOG       = Logger.getLogger(InnerNode.class);
    private InnerNode<K, V>.Key key;

    static enum Header {
        NODE_TYPE(0) {}, // char
        NUMBER_OF_KEYS(Character.SIZE / 8); // int

        private int offset;

        Header(int offset) {
            this.offset = offset;
        }

        static int size() {
            return (Character.SIZE + Integer.SIZE) / 8;
        } // 6

        int getOffset() {
            return offset;
        }
    }


    private class Key {

        private int pos;

        private Key(int pos) {
            setPos(pos);
        }

        public void setPos(int pos) {
            this.pos = pos;
        }

        public boolean hasNext() {
            return pos < getNumberOfKeys() - 1;
        }

        public void becomeNext() {
            this.pos++;
        }

        private int getOffset() {
            return Header.size() +
                    ((pos + 1) * getSizeOfPageId()) + // one id more that pages, the first id
                    (pos * keySerializer.getSerializedLength());
        }


        private byte[] getSerializedKey() {
            ByteBuffer buf = rawPage().bufferForReading(getOffset());
            byte[] byteBuf = new byte[keySerializer.getSerializedLength()];
            buf.get(byteBuf);

            return byteBuf;
        }

        private K getKey(){
            ByteBuffer buf = rawPage().bufferForReading(getOffset());
            byte[] bytes = new byte[keySerializer.getSerializedLength()];
            buf.get(bytes);
            return keySerializer.deserialize(bytes);
        }

        public String toString() {
            return "K(" + getKey() + ")";
        }

        public String toStringWithLeftAndRightKey() {
            String str = "";
            if(pos > 0){
                str += new Key(pos - 1).toString() + " - ";
            }

            str += toString();

            if(!isLastKey())
                str += " - " + new Key(pos+1).toString();

            return str;
        }

        private boolean isLastKey() {
            return pos == getNumberOfKeys() - 1;
        }

        public Node<K,V> getLeftNode() {
            int offset = getOffset() - Integer.SIZE / 8;
            int pageId = rawPage().bufferForReading(offset).getInt();
            return pageIdToNode(pageId);
        }

        public Node<K,V> getRightNode(){
            int offset = getOffset() + Integer.SIZE / 8;
            int pageId = rawPage().bufferForReading(offset).getInt();
            return pageIdToNode(pageId);
        }

        private Node<K, V> pageIdToNode(int id){
            if(leafPageManager.hasPage(id)) {
                return leafPageManager.getPage(id);
            }
            else {
                return innerNodePageManager.getPage(id);
            }
        }
    }

    private final RawPage                        rawPage;
    private final Comparator<K>                  comparator;
    // private final FixLengthSerializer<PagePointer, byte[]> pointerSerializer;
    private final DataPageManager<K>             keyPageManager;
    private final PageManager<LeafNode<K, V>>    leafPageManager;
    private final PageManager<InnerNode<K, V>>   innerNodePageManager;
    private       FixLengthSerializer<K, byte[]> keySerializer;

    private int numberOfKeys;
    private boolean valid = false;

    protected InnerNode(
            RawPage rawPage,
            FixLengthSerializer<K, byte[]> keySerializer,
            Comparator<K> comparator,
            DataPageManager<K> keyPageManager,
            PageManager<LeafNode<K, V>> leafPageManager,
            PageManager<InnerNode<K, V>> innerNodePageManager
    ) {

        if (comparator == null) {
            throw new IllegalStateException("comparator must not be null");
        }


        this.leafPageManager = leafPageManager;
        this.innerNodePageManager = innerNodePageManager;
        this.keyPageManager = keyPageManager;
        this.rawPage = rawPage;
        this.comparator = comparator;
        this.keySerializer = keySerializer;


    }

    public void initRootState(Integer pageId1, byte[] serializedKey, Integer pageId2) {
        ensureValid();
        validateLengthOfSerializedKey(serializedKey);


        ByteBuffer buf = rawPage().bufferForWriting(Header.size());

        buf.putInt(pageId1);
        buf.put(serializedKey);
        buf.putInt(pageId2);

        setNumberOfKeys(1);
    }

    /**
     * @param serializedKey
     */
    private void validateLengthOfSerializedKey(byte[] serializedKey) {
        if (serializedKey.length != keySerializer.getSerializedLength())
            throw new IllegalArgumentException(
                    "serializedByteKey has " + serializedKey.length + " bytes instead of " + keySerializer.getSerializedLength());
    }

    public void initRootState(Integer pageId1, K key, Integer pageId2) {
        initRootState(pageId1, keySerializer.serialize(key), pageId2);
    }

    private Integer getPageIdForKey(K key) {
        ByteBuffer buf = rawPage.bufferForReading(getOffsetOfPageIdForKey(key));
        return buf.getInt();
    }


    /**
     * Recursively check, if on of the leafs contains the given key
     */
    public boolean containsKey(K key) {
        ensureValid();
        ensureKeyNotNull(key);

        return getPageForPageId(getPageIdForKey(key)).containsKey(key);
    }

    private void ensureKeyNotNull(K key) {
        if (key == null) {
            throw new IllegalArgumentException("key must not be null");
        }
    }

    private Node<K, V> getPageForPageId(Integer pageId) {

        if (innerNodePageManager.hasPage(pageId)) {
            return innerNodePageManager.getPage(pageId);
        }

        if (leafPageManager.hasPage(pageId))
            return leafPageManager.getPage(pageId);

        throw new IllegalArgumentException(
                "the requested pageId " + pageId + " is neither in InnerNodePageManager nor in LeafPageManager");
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

        if (getNumberOfKeys() == 0)
            return new ArrayList<V>();

        Integer pageId = getPageIdForKey(key);
        Node<K, V> node = getPageForPageId(pageId);

        return node.get(key);
    }

    /* (non-Javadoc)
      * @see com.freshbourne.multimap.MultiMap#remove(java.lang.Object)
      */
    @Override
    public int remove(K key) {
        ensureValid();

        if (getNumberOfKeys() == 0)
            return 0;

        Integer id = getPageIdForKey(key);
        Node<K, V> node = getPageForPageId(id);
        return node.remove(key);
    }

    private int getSizeOfPageId() {
        return Integer.SIZE / 8;
    }

    private int posOfFirstLargerOrEqualKey(K key) {

        for (int i = 0; i < getNumberOfKeys(); i++) {

            byte[] sKey = getKey(i).getSerializedKey();
            if (comparator.compare(keySerializer.deserialize(sKey), key) >= 0) {
                return i;
            }
        }
        return -1;
    }

    private Integer getLeftPageIdOfKey(int i) {
        return rawPage().bufferForReading(getOffsetForLeftPageIdOfKey(i)).getInt();
    }

    private int getOffsetForLeftPageIdOfKey(int i) {
        return getKey(i).getOffset() - Integer.SIZE / 8;
    }

    private int getOffsetForRightPageIdOfKey(int i) {
        return getKey(i).getOffset() + keySerializer.getSerializedLength();
    }

    private Integer getRightPageIdOfKey(int i) {
        int offset = getOffsetForRightPageIdOfKey(i);
        return rawPage().bufferForReading(offset).getInt();
    }

    private K getKeyFromPagePointer(PagePointer pp) {
        return keyPageManager.getPage(pp.getId()).get(pp.getOffset());
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
        if (NodeType.deserialize(buf.getChar()) != NODE_TYPE)
            throw new IOException(
                    "You are trying to load a InnerNode from a byte array, that does not contain an InnerNode");


        buf.position(Header.NUMBER_OF_KEYS.getOffset());
        numberOfKeys = buf.getInt();
        valid = true;
    }

    /* (non-Javadoc)
      * @see com.freshbourne.io.ComplexPage#isValid()
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

    /* (non-Javadoc)
      * @see com.freshbourne.io.ComplexPage#rawPage()
      */
    @Override
    public RawPage rawPage() {
        return rawPage;
    }


    private int getOffsetOfPageIdForKey(K key) {
        int posOfFirstLargerOrEqualKey = posOfFirstLargerOrEqualKey(key);

        if (posOfFirstLargerOrEqualKey < 0) // if key is largest
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
        if (posOfFirstLargerOrEqualKey < 0) // if key is largest
            pageId = getRightPageIdOfKey(getNumberOfKeys() - 1);
        else
            pageId = getLeftPageIdOfKey(posOfFirstLargerOrEqualKey);

        if (pageId <= 0) {
            throw new IllegalArgumentException(
                    "pageId must not be 0 ( posOfFirstLargerOrEqualKey: " + posOfFirstLargerOrEqualKey + " )");
        }

        Node<K, V> node;
        if (leafPageManager.hasPage(pageId))
            node = leafPageManager.getPage(pageId);
        else
            node = innerNodePageManager.getPage(pageId);

        AdjustmentAction<K, V> result;
        result = node.insert(key, value);

        // insert worked fine, no adjustment
        if (result == null)
            return null;

        if (result.getAction() == ACTION.UPDATE_KEY) {
            LOG.debug("ACTION.UPDATE_KEY");
            LOG.debug("Key inserted = " + key + "at lhs of pos " + posOfFirstLargerOrEqualKey);
            // some values have been pushed to rhs of posOfFirstLargerOrEqualKey
            Key thisKey = getKey(posOfFirstLargerOrEqualKey);

            return updateKey(posOfFirstLargerOrEqualKey, result);
        }

        if (result.getAction() == ACTION.INSERT_NEW_NODE) {
            return handleNewNodeAction(result, posOfFirstLargerOrEqualKey);
        }

        throw new UnsupportedOperationException();
    }

    /**
     * this method should be called when an insert action results in a new node that has to be inserted
     * in this node.
     * <p/>
     * Although this is a code-smell, it is made package-visible for testing purposes.
     *
     * @param result                     of the insertion
     * @param posOfFirstLargerOrEqualKey
     * @return adjustment action or null
     */
    AdjustmentAction<K, V> handleNewNodeAction(AdjustmentAction<K, V> result, int posOfFirstLargerOrEqualKey) {
        if (result.getAction() != ACTION.INSERT_NEW_NODE) {
            throw new IllegalArgumentException("result action type must be INSERT_NEW_NODE");
        }

        Key currentKey = getKey(posOfFirstLargerOrEqualKey);

        LOG.debug("handleNewNodeAction()");
        LOG.debug("adjustmentActionKey: " + keySerializer.deserialize(result.getSerializedKey()));
        LOG.debug("posOfFirstLargerOrEqualKey:" + currentKey.toStringWithLeftAndRightKey());

        // a new child node has been created and a key must be inserted, check for available space
        if (getNumberOfKeys() < getMaxNumberOfKeys()) {
            LOG.debug("Space available");
            // space left, simply insert the key/pointer.
            // the key replaces the old key for our node, since the split caused a different
            // key to be the now highest in the subtree

            int posForInsert = posOfFirstLargerOrEqualKey == -1 ? getNumberOfKeys() : posOfFirstLargerOrEqualKey;
            insertKeyPointerPageIdAtPosition(
                    result.getSerializedKey(), result.getPageId(), posForInsert);
            LOG.debug("key inserted: " + getKey(posForInsert).toStringWithLeftAndRightKey());


            // no further adjustment necessary. even if we inserted to the last position, the
            // highest key in the subtree below is still the same, because otherwise we would
            // have never ended up here during the descend from the root, or we are in the
            // right-most path of the subtree.
            return null;
        }

        // else split is required, allocate new node
        InnerNode<K, V> inp = innerNodePageManager.createPage();

        // move half the keys/pointers to the new node. remember the dropped key.
        byte[] keyUpwardsBytes = moveLastToNewPage(inp, getNumberOfKeys() >> 1);

        // decide where to insert the pointer we are supposed to insert
        // if the old key position is larger than the current numberOfKeys, the
        // entry has to go to the next node
        if (posOfFirstLargerOrEqualKey > getNumberOfKeys() || posOfFirstLargerOrEqualKey == -1) {
            insertKeyPointerPageIdAtPosition(result.getSerializedKey(), result.getPageId(),
                    posOfFirstLargerOrEqualKey - getNumberOfKeys() + 1);
        } else {
            insertKeyPointerPageIdAtPosition(result.getSerializedKey(), result.getPageId(), posOfFirstLargerOrEqualKey);
        }

        return new AdjustmentAction<K, V>(ACTION.INSERT_NEW_NODE, keyUpwardsBytes, inp.getId());
    }


    private byte[] moveLastToNewPage(InnerNode<K, V> newPage, int numberOfKeys) {
        LOG.debug("moveLastToNewPage():");
        LOG.debug("currentPage: " + toString());

        if (!newPage.isValid())
            newPage.initialize();

        ByteBuffer buf = newPage.rawPage().bufferForWriting(0);
        int from = getOffsetForLeftPageIdOfKey(getNumberOfKeys() - numberOfKeys);
        int to = Header.size();
        int length_to_copy = rawPage().bufferForReading(0).limit() - from;
        System.arraycopy(rawPage().bufferForWriting(0).array(), from, buf.array(), to, length_to_copy);
        newPage.setNumberOfKeys(numberOfKeys);

        // last key is dropped, get the keyPointerByteSequence
        byte[] result = new byte[keySerializer.getSerializedLength()];
        rawPage().bufferForReading(from - getSizeOfPageId()).get(result);
        setNumberOfKeys(getNumberOfKeys() - numberOfKeys - 1); // one key less


        LOG.debug("currentPage: " + toString());
        LOG.debug("newPage: " + newPage.toString());

        return result;
    }

    public String toString() {
        String str = "InnerNode(id: " + getId() + ", keys: " + getNumberOfKeys()+ "):";
        Key key = null;
        do {
            if (key == null)
                key = getKey(0);
            else
                key.becomeNext();

            str += " " + key.toString();
        } while (key.hasNext());
        return str;
    }

    private Key getKey() {
        if (this.key != null)
            return this.key;

        key = new Key(0);
        return key;
    }

    private Key getKey(int i) {
        key = getKey();
        key.setPos(i);
        return key;
    }

    private void ensureRoot() {
        if (getNumberOfKeys() == 0)
            throw new IllegalStateException("use inizializeRootState() for the first insert!");
    }

    /**
     * @param serializedKey
     * @param pageId
     * @param posOfKeyForInsert
     */
    private void insertKeyPointerPageIdAtPosition(byte[] serializedKey,
                                                  Integer pageId, int posOfKeyForInsert) {

        Key thisKey = getKey(posOfKeyForInsert);
        ByteBuffer buf = rawPage().bufferForWriting(thisKey.getOffset());

        int spaceNeededForInsert = getSizeOfPageId() + keySerializer.getSerializedLength();
        System.arraycopy(buf.array(), buf.position(), buf.array(), buf.position() + spaceNeededForInsert,
                buf.limit() - buf.position() - spaceNeededForInsert);

        buf.put(serializedKey);
        buf.putInt(pageId);

        setNumberOfKeys(getNumberOfKeys() + 1);
    }

    public int getMaxNumberOfKeys() {
        int size = rawPage.bufferForReading(0).limit() - Header.size();

        // size first page id
        size -= Integer.SIZE / 8;

        return size / (Integer.SIZE / 8 + keySerializer.getSerializedLength());
    }

    private AdjustmentAction<K, V> updateKey(int posOfFirstLargerOrEqualKey, AdjustmentAction<K, V> result) {
        if (result.getAction() != ACTION.UPDATE_KEY)
            throw new IllegalArgumentException("action must be of type UPDATE_KEY");


        if (posOfFirstLargerOrEqualKey < 0) { // last page
            return result; // last page must be propagated up
        }

        // We need to adjust our own key, because keys were moved to the next node.
        // That changes the highest key in this page, so the corresponding key
        // must be adjusted.
        setKey(result.getSerializedKey(), posOfFirstLargerOrEqualKey);
        return null;
    }

    private void setKey(byte[] serializedKey, int pos) {
        ByteBuffer buf = rawPage().bufferForWriting(getKey(pos).getOffset());
        buf.put(serializedKey);
    }

    private void ensureValid() {
        if (!isValid()) {
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

    /**
     * @param rawKeys
     * @param pageIds
     * @param fromId
     * @return
     */
    public int bulkInitialize(ArrayList<byte[]> rawKeys,
                              ArrayList<Integer> pageIds, int fromId) {

        if (pageIds.size() < (fromId + 2) || rawKeys.size() != (pageIds.size() - 1))
            throw new IllegalArgumentException(
                    "for bulkinsert, you must have at least 2 page ids and keys.size() == (pageIds.size() - 1)\n" +
                            "pageIds.size()=" + pageIds.size() + ";fromId=" + fromId + ";rawKeys.size()=" + rawKeys.size());

        int fromId2 = fromId;

        initialize();
        ByteBuffer buf = rawPage().bufferForWriting(Header.size());
        buf.putInt(pageIds.get(fromId2));

        int requiredSpace = Integer.SIZE / 8 + rawKeys.get(0).length;
        int spaceForEntries = buf.remaining() / requiredSpace;
        int totalEntriesToInsert = (pageIds.size() - fromId - 1);
        int entriesToInsert = spaceForEntries < totalEntriesToInsert ? spaceForEntries : totalEntriesToInsert;

        for (int i = 0; i < entriesToInsert; i++) {
            // System.out.println("fetching rawKey " + (fromId + i) + " from array length " + rawKeys.size() + " with i=" + i);
            buf.put(rawKeys.get(fromId + i)); // fromId + 1 - 1 +i
            //LOG.debug("insert key: " + keySerializer.deserialize(rawKeys.get(fromId + i)));
            buf.putInt(pageIds.get(fromId + 1 + i));
        }

        setNumberOfKeys(entriesToInsert);
        return entriesToInsert + 1; // page ids
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
        return getKey(getNumberOfKeys() - 1).getRightNode().getLastLeafKey();
    }

    /* (non-Javadoc)
      * @see com.freshbourne.multimap.btree.Node#getIterator(java.lang.Object, java.lang.Object)
      */
    @Override
    public Iterator<V> getIterator(K from, K to) {
        return getPageForPageId(getLeftPageIdOfKey(0)).getIterator(from, to);
    }

    @Override public int getDepth() {
        return getKey(0).getLeftNode().getDepth() + 1;
    }

    /* (non-Javadoc)
      * @see com.freshbourne.multimap.btree.Node#getFirst(java.lang.Object)
      */
    @Override
    public V getFirst(K key) {
        List<V> res = get(key);
        return res.size() > 0 ? res.get(0) : null;
    }
}
