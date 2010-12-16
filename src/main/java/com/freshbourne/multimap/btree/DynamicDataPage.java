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
 * @param <T>
 */
public class DynamicDataPage implements DataPage{
	
	private static final int intSize = 4;
	
	private final ByteBuffer header;
	private final ByteBuffer body;
	private final ByteBuffer buffer;
	
	private final FixLengthSerializer<PagePointer, byte[]> pointSerializer;
	private boolean valid = true;
	
	private Map<Integer, PagePointer> entries;
	
	
	DynamicDataPage(byte[] p, FixLengthSerializer<PagePointer, byte[]> pointSerializer){
		this.buffer = ByteBuffer.wrap(p);
		this.pointSerializer = pointSerializer;
		this.entries = new TreeMap<Integer, PagePointer>();
		
		this.header = buffer.duplicate();
		this.body = buffer.duplicate();
		
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
		header.putInt(0);
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.Page#buffer()
	 */
	@Override
	public byte[] buffer() {
		return buffer.array();
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.Page#body()
	 */
	@Override
	public byte[] body() {
		body.reset();
		return body.slice().array();
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
			for (int i = 0; i < elements; i++) {
				byte[] bytes = new byte[pointSerializer
						.serializedLength(PagePointer.class)];
				header.get(bytes);
				PagePointer p = pointSerializer.deserialize(bytes);
				entries.put(p.getId(), p);
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
		body.reset();
		int remaining = buffer.capacity() - header.limit() - body.position();
		if(bytes.length > remaining)
			throw new NoSpaceException();
		
		body.position(body.position() - bytes.length);
		body.mark();
		body.put(bytes);
		
		body.reset();
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
		
		if(buffer.capacity() - header.position() - body().length > size)
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
		
		
		int size = sizeOfEntry(p);
		body.reset();
		System.arraycopy(buffer.array(), body.position(), buffer.array(), body.position() + size, size);
		
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
		header.putInt(entries.size());
		
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
		
		body.position(p.getOffset());
		int size = sizeOfEntry(p);
		byte[] bytes = new byte[size];
		body.get(bytes);
		return bytes;
	}
	
	private int sizeOfEntry(PagePointer p){
		return nextEntry(p).getOffset() - p.getOffset();
	}
	
	private PagePointer nextEntry(PagePointer p){
		PagePointer next = null;
		for(PagePointer current : entries.values()){
			if(current.getOffset() > p.getOffset()){
				if(next == null || next.getOffset() < current.getOffset())
					next = current;
			}
		}
		return next;
	}
}
