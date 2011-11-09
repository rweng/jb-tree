/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 *
 * http://creativecommons.org/licenses/by-nc/3.0/
 *
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2011 "Robin Wenglewski <robin@wenglewski.de>"
 */
package com.freshbourne.io.rm;

import com.freshbourne.serializer.FixLengthSerializer;
import com.freshbourne.serializer.Serializer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class DynamicDataPage<T> implements DataPage<T>, ComplexPage {
	
	private final RawPage rawPage;
	
	private final FixLengthSerializer<PagePointer, byte[]> pointSerializer;
	private final Serializer<T, byte[]> entrySerializer;
	
	
	/**
	 * ByteBuffer.getInt returns 0 if no int could be read. To avoid thinking we already initialized the buffer,
	 * we write down this number instead of 0 if we have no entries.
	 */
	public static final int NO_ENTRIES_INT = -1;
	
	// id | offset in this page
	private final Map<Integer, Integer> entries;
	
	private boolean valid = false;

	private int getHeaderSize(){
		return Integer.SIZE + entries.size() * Integer.SIZE * 2;
	}
	
	DynamicDataPage(
			final RawPage rawPage,
			final FixLengthSerializer<PagePointer, byte[]> pointSerializer,
			final Serializer<T, byte[]> dataSerializer){

		this.rawPage = rawPage;
		
        this.pointSerializer = pointSerializer;
		this.entrySerializer = dataSerializer;
		
		this.entries = new TreeMap<Integer, Integer>();
	}
	
	@Override
	public void initialize() {
		writeAndAdjustHeader();
		this.valid = true;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.btree.DataPage#add(byte[])
	 */
	@Override
	public Integer add(final T entry) {
		ensureValid();
		
		final byte[] bytes = entrySerializer.serialize(entry);
		
		if(bytes.length > remaining())
			return null;
		
		final int bodyOffset = getBodyOffset() - bytes.length;
		rawPage.bufferForWriting(bodyOffset).put(bytes);
		
		final int id = generateId();
		if(entries.containsKey(id)){
			throw new IllegalStateException();
		}
		
		entries.put(id, bodyOffset);
		
		writeAndAdjustHeader();
		
		return id;
	}
	
	private int generateId(){
		final Random r = new Random();
		int id;
		while(entries.containsKey(id = r.nextInt())){}
		return id;
	}
	
	private int bodyUsedBytes(){
		return rawPage.bufferForWriting(0).limit() - getBodyOffset();
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.btree.DataPage#remove(int)
	 */
	@Override
	public void remove(final int id)  {
		
		final Integer offset = entries.get(id);
		if(offset == null)
			return;
		
		// move all body elements
		final int size = sizeOfEntryAt(offset);
		System.arraycopy(rawPage.bufferForWriting(0).array(), getBodyOffset(), rawPage.bufferForWriting(0).array(), getBodyOffset() + size, offset - getBodyOffset() );
		
		// adjust the entries in the entries array
		for(final int key : entries.keySet()){
			if(entries.get(key) < offset)
				entries.put(key, entries.get(key) + size);
		}
		
		entries.remove(id);
		
		// write the adjustments to byte array
		writeAndAdjustHeader();
	}

	/**
	 * Creates a valid header by writing the entries in memory to the header and adjusts the header limit.
	 */
	private void writeAndAdjustHeader() {
		final ByteBuffer buffer = rawPage().bufferForWriting(0);
		buffer.putInt(entries.size() == 0 ? NO_ENTRIES_INT : entries.size());
		
		for(final int key : entries.keySet()){
			buffer.putInt(key);
			buffer.putInt(entries.get(key));
		}	
	}
	
	/* (non-Javadoc)
	 * @see com.freshbourne.btree.DataPage#get(int)
	 */
	@Override
	public T get(final int id) {
		ensureValid();
		
		if(!entries.containsKey(id))
			return null;
		
		final Integer offset = entries.get(id);
		
		final int size = sizeOfEntryAt(offset);
		final byte[] bytes = new byte[size];
		rawPage.bufferForReading(offset).get(bytes);
		
		return entrySerializer.deserialize(bytes);
	}
	
	private void ensureValid() throws InvalidPageException {
		if( !isValid() )
			throw new InvalidPageException(this);
	}
	
	private int nextEntry(final int offset){
		int smallestLarger = -1;
		
		for(final int o : entries.values()){
			if(o > offset){
				if(o < smallestLarger || smallestLarger == -1)
					smallestLarger = o;
			}
		}
		
		return smallestLarger;
	}
	
	private int sizeOfEntryAt(final int offset){
		int smallestLarger = nextEntry(offset);
		
		if(smallestLarger == -1)
			smallestLarger = rawPage.bufferForReading(0).limit();
		
		return smallestLarger - offset;
	}

		/* (non-Javadoc)
	 * @see com.freshbourne.btree.DataPage#remaining()
	 */
	@Override
	public int remaining() {
		return rawPage.bufferForReading(0).limit() - getHeaderSize() - bodyUsedBytes();
	}
	/* (non-Javadoc)
	 * @see com.freshbourne.io.rm.ComplexPage#load()
	 */
	@Override
	public void load() {
        entries.clear();
		
		final ByteBuffer buffer = rawPage().bufferForReading(0);
		final int numberOfEntries = buffer.getInt();
		
		for(int i = 0; i < numberOfEntries; i++){
			final int key = buffer.getInt();
			entries.put(key, buffer.getInt());
		}
		
		valid = true;
	}
	
	private int getBodyOffset(){
		int offset = rawPage().bufferForReading(0).limit();
		for(final int pos : entries.values()){
			if(pos < offset)
				offset = pos;
		}
		return offset;
	}
	
	/* (non-Javadoc)
	 * @see com.freshbourne.io.rm.ComplexPage#isValid()
	 */
	@Override
	public boolean isValid() {
		return valid;
	}

    @Override
    public void loadOrInitialize() throws IOException {
        try {
            load();
        } catch (Exception e){
            initialize();
        }
    }

    /* (non-Javadoc)
      * @see com.freshbourne.io.rm.DataPage#numberOfEntries()
      */
	@Override
	public int numberOfEntries() throws InvalidPageException {
		ensureValid();
		
		return entries.size();
	}
	
	/* (non-Javadoc)
	 * @see com.freshbourne.io.rm.ComplexPage#rawPage()
	 */
	@Override
	public RawPage rawPage() {
		return rawPage;
	}
	
	/* (non-Javadoc)
	 * @see com.freshbourne.io.rm.DataPage#pagePointSerializer()
	 */
	@Override
	public FixLengthSerializer<PagePointer, byte[]> pagePointSerializer() {
		return pointSerializer;
	}
	
	/* (non-Javadoc)
	 * @see com.freshbourne.io.rm.DataPage#dataSerializer()
	 */
	@Override
	public Serializer<T, byte[]> dataSerializer() {
		return entrySerializer;
	}
	
	@Override
	public String toString(){
		return "DynamicDataPage(id: " + System.identityHashCode(this) + ", rawPage:" + rawPage().id() + ", entries: " + entries.size() + ", headerSize: + "+ getHeaderSize()+", bodyOffset: "+getBodyOffset()+")";
	}
	
}
