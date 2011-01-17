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
	private final ByteBuffer body;
	
	private final RawPage rawPage;
	
	private final FixLengthSerializer<PagePointer, byte[]> pointSerializer;
	private final Serializer<T, byte[]> entrySerializer;
	
	
	/**
	 * ByteBuffer.getInt returns 0 if no int could be read. To avoid thinking we already initialized the buffer,
	 * we write down this number instead of 0 if we have no entries.
	 */
	private final int NO_ENTRIES_INT = 345234345;
	private final Map<Long, PagePointer> entries;
	
	private boolean valid = false;


	DynamicDataPage(
			RawPage rawPage,
			FixLengthSerializer<PagePointer, byte[]> pointSerializer, 
			Serializer<T, byte[]> dataSerializer){

		this.rawPage = rawPage;
		
        this.header = rawPage.buffer().duplicate();
		this.body = rawPage.buffer().duplicate();
		this.pointSerializer = pointSerializer;
		this.entrySerializer = dataSerializer;
		
		this.entries = new TreeMap<Long, PagePointer>();

        //TODO: try to load buffer here? or seperate function load?

		body.position(body.capacity());
		
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

	public ByteBuffer body() {
		int pos = body.position();
		body.position(header.limit());
		ByteBuffer result = body.slice();
		body.position(pos);
		return result;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.btree.DataPage#add(byte[])
	 */
	public int add(T entry) throws NoSpaceException, InvalidPageException {
		ensureValid();
		
		byte[] bytes = entrySerializer.serialize(entry);
		
		if(bytes.length > remaining())
			throw new NoSpaceException();
		
		body.position(body.position() - bytes.length);
		int pos = body.position();
		body.put(bytes);
		
		body.position(pos);
		int id = generateId();
		PagePointer p = new PagePointer(id, body.position());
		entries.put(p.getId(), p);
		
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
		
		if(rawPage.buffer().capacity() - header.position() - bodyUsed().capacity() > size)
			header.limit(header.position() + size);
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.btree.DataPage#remove(int)
	 */
	public void remove(int id) throws ElementNotFoundException {
		
		PagePointer p = entries.remove(id);
		if(p == null)
			throw new ElementNotFoundException();
		
		int pos = body.position();
		
		// move all body elements
		int size = sizeOfEntry(p);
		System.arraycopy(rawPage.buffer().array(), body.position(), rawPage.buffer().array(), body.position() + size, p.getOffset() - body.position() );
		
		// adjust the entries in the entries array
		for(PagePointer c : entries.values()){
			if(c.getOffset() < p.getOffset())
				c.setOffset(c.getOffset() + size);
		}
		
		
		body.position(pos + size);
		
		// write the adjustments to byte array
		writeAndAdjustHeader();
	}

	/**
	 * Creates a valid header by writing the entries in memory to the header and adjusts the header limit.
	 */
	private void writeAndAdjustHeader() {
		header.position(0);
		header.putInt(entries.size() == 0 ? NO_ENTRIES_INT : entries.size());
		
		int size = pointSerializer.serializedLength(PagePointer.class);
		for(PagePointer p : entries.values()){
			header.put(pointSerializer.serialize(p));
		}
		
		header.limit(header.position() + size);
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.btree.DataPage#get(int)
	 */
	public T get(long id) throws Exception {
		ensureValid();
		
		PagePointer p = entries.get(id);
		if( p == null){
			throw new ElementNotFoundException();
		}
		int pos = body.position();
		body.position(p.getOffset());
		int size = sizeOfEntry(p);
		byte[] bytes = new byte[size];
		body.get(bytes);
		body.position(pos);
		
		return entrySerializer.deserialize(bytes);
	}
	
	private void ensureValid() throws InvalidPageException {
		if( !isValid() )
			throw new InvalidPageException(this);
	}
	
	private int sizeOfEntry(PagePointer p){
		PagePointer next = nextEntry(p);
		int capacity =body.capacity();
		int pos = p.getOffset();
		return next == null ? capacity - pos : next.getOffset() - p.getOffset();
	}
	
	private PagePointer nextEntry(PagePointer p){
		PagePointer next = null;
		for(PagePointer current : entries.values()){
			if(current.getOffset() > p.getOffset()){
				if(next == null || next.getOffset() > current.getOffset())
					next = current;
			}
		}
		return next;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.btree.DataPage#bodyUsed()
	 */
	public ByteBuffer bodyUsed() {
		return body.slice();
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.btree.DataPage#remaining()
	 */
	public int remaining() {
		return rawPage.buffer().capacity() - header.limit() - bodyUsed().capacity();
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
			entries.put(p.getId(), p);
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
