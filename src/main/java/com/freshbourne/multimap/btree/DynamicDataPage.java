/**
 * Copyright (C) 2010 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author. 
 */
package com.freshbourne.multimap.btree;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import com.freshbourne.io.FixLengthSerializer;
import com.freshbourne.io.Page;
import com.freshbourne.io.PagePointer;

/**
 * Wraps around a <code>byte[]</code> and can hold values.
 * 
 * Since its a dynamic data page, values are written first at the end of the body to allow
 * the header to grow.
 * 
 * The PagePointer of the header are kept in Memory
 * 
 * @author Robin Wenglewski <robin@wenglewski.de>
 *
 */
public class DynamicDataPage implements DataPage{
	
	private static final int intSize = 4;
	
	private final ByteBuffer header;
	private final ByteBuffer body;
	private final ByteBuffer buffer;
	
	private final FixLengthSerializer<PagePointer, byte[]> pointSerializer;
	
	/**
	 * ByteBuffer.getInt returns 0 if no int could be read. To avoid thinking we already initialized the buffer,
	 * we write down this number instead of 0 if we have no entries.
	 */
	private final int NO_ENTRIES_INT = 345234345;
	
	private boolean valid = false;
	
	private Map<Integer, PagePointer> entries;
	
	DynamicDataPage(byte[] p, FixLengthSerializer<PagePointer, byte[]> pointSerializer){
		this(ByteBuffer.wrap(p), pointSerializer);
	}
	DynamicDataPage(ByteBuffer p, FixLengthSerializer<PagePointer, byte[]> pointSerializer){
		this.buffer = p;
		this.pointSerializer = pointSerializer;
		this.entries = new TreeMap<Integer, PagePointer>();
		
		this.header = buffer.duplicate();
		this.body = buffer.duplicate();
		
		body.position(body.capacity());
		
		adjustHeader();
		
	}
	
	private void adjustHeader(){
		// we always (except the buffer is full) reserve space for one element
		// + one element right on start for the number of elements in the page
		this.header.limit(pointSerializer.serializedLength(PagePointer.class) * (entries.size() + 1) + intSize );
	}
	
	/* (non-Javadoc)
	 * @see com.freshbourne.io.Page#initialize()
	 */
	@Override
	public void initialize() {
		header.position(0);
		header.putInt(NO_ENTRIES_INT);
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.Page#buffer()
	 */
	@Override
	public ByteBuffer buffer() {
		return buffer.asReadOnlyBuffer();
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.Page#body()
	 */
	@Override
	public ByteBuffer body() {
		int pos = body.position();
		body.position(header.limit());
		ByteBuffer result = body.slice();
		body.position(pos);
		return result;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.Page#valid()
	 */
	@Override
	public boolean valid(){
		return valid(false);
	}
	
	public boolean valid(boolean force) {
		if(valid && !force)
			return true;
		
		try {
			entries.clear();
			header.position(0);

			int elements = header.getInt();
			
			// 0 is never written
			if(elements == 0)
				return valid = false;
			if(elements == NO_ENTRIES_INT)
				return valid = true;
			
			for (int i = 0; i < elements; i++) {
				byte[] bytes = new byte[pointSerializer
						.serializedLength(PagePointer.class)];
				header.get(bytes);
				PagePointer p = pointSerializer.deserialize(bytes);
				entries.put(p.getId(), p);
				header.limit(header.position() + pointSerializer
						.serializedLength(PagePointer.class));
				
			}
		} catch (Exception e) {
			return valid = false;
		}
		
		return valid = true;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.btree.DataPage#add(byte[])
	 */
	@Override
	public int add(byte[] bytes) throws NoSpaceException {
		if(bytes.length > remaining())
			throw new NoSpaceException();
		
		body.position(body.position() - bytes.length);
		int pos = body.position();
		body.put(bytes);
		
		body.position(pos);
		int id = generateId();
		PagePointer p = new PagePointer(id, body.position());
		entries.put(p.getId(), p);
		addToHeader(p);
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
		
		if(buffer.capacity() - header.position() - bodyUsed().capacity() > size)
			header.limit(header.position() + size);
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.btree.DataPage#remove(int)
	 */
	@Override
	public void remove(int id) throws ElementNotFoundException {
		
		PagePointer p = entries.remove(id);
		if(p == null)
			throw new ElementNotFoundException();
		
		// move all body elements
		int size = sizeOfEntry(p);
		System.arraycopy(buffer.array(), body.position(), buffer.array(), body.position() + size, p.getOffset() - body.position() );
		
		// adjust the entries in the entries array
		for(PagePointer c : entries.values()){
			if(c.getOffset() < p.getOffset())
				c.setOffset(c.getOffset() + size);
		}
		
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
	@Override
	public byte[] get(int id) throws Exception {
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
		return bytes;
	}
	
	private int sizeOfEntry(PagePointer p){
		PagePointer next = nextEntry(p);
		int capacity =body.capacity();
		int pos = body.position();
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
	@Override
	public ByteBuffer bodyUsed() {
		return body.slice();
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.multimap.btree.DataPage#remaining()
	 */
	@Override
	public int remaining() {
		return buffer.capacity() - header.limit() - bodyUsed().capacity();
	}
}
