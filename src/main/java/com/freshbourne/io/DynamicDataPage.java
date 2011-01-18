/*
 * Copyright (c) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 */
package com.freshbourne.io;

import com.freshbourne.serializer.FixLengthSerializer;
import com.freshbourne.serializer.PagePointSerializer;
import com.freshbourne.serializer.Serializer;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/**
 * Wraps around a <code>byte[]</code> and can hold values of variable serialized length.
 * 
 * Since its a dynamic data page, values are written first at the end of the body to allow
 * the header to grow.
 * 
 * The header is kept in memory.
 * 
 * @author Robin Wenglewski <robin@wenglewski.de>
 *
 */
public class DynamicDataPage<T> implements DataPage<T>{
	
	private static final int intSize = 4;
	
	
	/*
	 * build up like this:
	 * NO_ENTRIES_INT | PAGE_POINTER_OFFSETS | DATA
	 * 
	 * the pagepointer in this page contain 
	 * 	id: id of the data element inserted
	 *  offset: in the current page where the data ist stored
	 */
	private final ByteBuffer header;
	
	private final RawPage rawPage;
	
	private final FixLengthSerializer<PagePointer, byte[]> pointSerializer;
	private final Serializer<T, byte[]> entrySerializer;
	
	private int bodyOffset = -1;
	
	
	/**
	 * ByteBuffer.getInt returns 0 if no int could be read. To avoid thinking we already initialized the buffer,
	 * we write down this number instead of 0 if we have no entries.
	 */
	private final int NO_ENTRIES_INT = 345234345;
	
	// id | offset in this page
	private final Map<Integer, Integer> entries;
	
	private boolean valid = false;


	DynamicDataPage(
			RawPage rawPage,
			FixLengthSerializer<PagePointer, byte[]> pointSerializer, 
			Serializer<T, byte[]> dataSerializer){

		this.rawPage = rawPage;
		
        this.header = rawPage.buffer().duplicate();
        this.bodyOffset = rawPage.buffer().limit();
		this.pointSerializer = pointSerializer;
		this.entrySerializer = dataSerializer;
		
		this.entries = new TreeMap<Integer, Integer>();

        adjustHeaderSize();
	}
	/**
	 * sets the size of the header dependent of the size of the entries array,
	 * the size of a serialized entry and the size of an serialized Integer for the first
	 * space (stating how many elements to read from the header-buffer)
	 */
	private void adjustHeaderSize(){
		this.header.limit(pointSerializer.serializedLength(PagePointer.class) * (entries.size() + 1) + intSize );
	}
	
	public void initialize() {
		header.position(0);
		header.putInt(NO_ENTRIES_INT);
		this.valid = true;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.btree.DataPage#add(byte[])
	 */
	public int add(T entry) throws NoSpaceException, InvalidPageException {
		ensureValid();
		
		byte[] bytes = entrySerializer.serialize(entry);
		
		if(bytes.length > remaining())
			throw new NoSpaceException();
		
		bodyOffset -= bytes.length;
		rawPage.buffer().position(bodyOffset);
		rawPage.buffer().put(bytes);
		
		int id = generateId();
		entries.put(id, bodyOffset);
		
		writeAndAdjustHeader();
		
		return id;
	}
	
	private int generateId(){
		Random r = new Random();
		int id;
		while(entries.containsKey(id = r.nextInt())){}
		return id;
	}
	
	/**
	 * appends a PagePointer to the header and increases the header size, if possible
	 * @param p
	 */
	private void addToHeader(PagePointer p){
		int size = pointSerializer.serializedLength(PagePointer.class);
		header.position(header.limit()-size);
		header.put(pointSerializer.serialize(p));
		
		if(rawPage.buffer().capacity() - header.position() - bodyUsedBytes() > size)
			header.limit(header.position() + size);
	}
	
	
	private int bodyUsedBytes(){
		return rawPage.buffer().limit() - bodyOffset;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.btree.DataPage#remove(int)
	 */
	public void remove(int id) throws ElementNotFoundException {
		
		Integer offset = entries.remove(id);
		if(offset == null)
			throw new ElementNotFoundException();
		
		// move all body elements
		int size = sizeOfEntryAt(offset);
		System.arraycopy(rawPage.buffer().array(), bodyOffset, rawPage.buffer().array(), bodyOffset + size, offset - bodyOffset );
		
		// adjust the entries in the entries array
		for(int key : entries.keySet()){
			if(entries.get(key) < offset)
				entries.put(key, entries.get(key) + size);
		}
		
		
		bodyOffset += size;
		
		// write the adjustments to byte array
		writeAndAdjustHeader();
	}

	/**
	 * Creates a valid header by writing the entries in memory to the header and adjusts the header limit.
	 */
	private void writeAndAdjustHeader() {
		header.position(0);
		header.putInt(entries.size() == 0 ? NO_ENTRIES_INT : entries.size());
		
		int size = Integer.SIZE * 2 / 8;
		for(int key : entries.keySet()){
			header.putInt(key);
			header.putInt(entries.get(key));
		}
		
		header.limit(header.position() + size);
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.btree.DataPage#get(int)
	 */
	public T get(int id) throws Exception {
		ensureValid();
		
		Integer offset = entries.get(id);
		if( offset == null){
			throw new ElementNotFoundException();
		}
		
		rawPage.buffer().position(offset);
		int size = sizeOfEntryAt(offset);
		byte[] bytes = new byte[size];
		rawPage.buffer().get(bytes);
		
		return entrySerializer.deserialize(bytes);
	}
	
	private void ensureValid() throws InvalidPageException {
		if( !isValid() )
			throw new InvalidPageException(this);
	}
	
	private int nextEntry(int offset){
		int smallestLarger = -1;
		
		for(int o : entries.values()){
			if(o > offset){
				if(o < smallestLarger || smallestLarger == -1)
					smallestLarger = o;
			}
		}
		
		return smallestLarger;
	}
	
	private int sizeOfEntryAt(int offset){
		int smallestLarger = nextEntry(offset);
		
		if(smallestLarger == -1)
			smallestLarger = rawPage.buffer().capacity();
		
		return smallestLarger - offset;
	}

		/* (non-Javadoc)
	 * @see com.freshbourne.multimap.btree.DataPage#remaining()
	 */
	public int remaining() {
		return rawPage.buffer().capacity() - header.limit() - bodyUsedBytes();
	}
	/* (non-Javadoc)
	 * @see com.freshbourne.io.ComplexPage#load()
	 */
	@Override
	public void load() {
		entries.clear();
		
		header.position(0);
		int numberOfEntries = header.getInt();
		
		for(int i = 0; i < numberOfEntries; i++){
			byte[] buf = new byte[pointSerializer.serializedLength(PagePointer.class)];
			header.get(buf);
			PagePointer p = pointSerializer.deserialize(buf);
			//entries.put(p.getOffset(), p);
		}
		
		valid = true;
	}
	
	/* (non-Javadoc)
	 * @see com.freshbourne.io.ComplexPage#isValid()
	 */
	@Override
	public boolean isValid() {
		return valid;
	}
	
	/* (non-Javadoc)
	 * @see com.freshbourne.io.DataPage#numberOfEntries()
	 */
	@Override
	public int numberOfEntries() throws InvalidPageException {
		ensureValid();
		
		return entries.size();
	}
	
	/* (non-Javadoc)
	 * @see com.freshbourne.io.ComplexPage#rawPage()
	 */
	@Override
	public RawPage rawPage() {
		return rawPage;
	}
	
	/* (non-Javadoc)
	 * @see com.freshbourne.io.DataPage#pagePointSerializer()
	 */
	@Override
	public FixLengthSerializer<PagePointer, byte[]> pagePointSerializer() {
		return pointSerializer;
	}
	
	/* (non-Javadoc)
	 * @see com.freshbourne.io.DataPage#dataSerializer()
	 */
	@Override
	public Serializer<T, byte[]> dataSerializer() {
		return entrySerializer;
	}
	
}
