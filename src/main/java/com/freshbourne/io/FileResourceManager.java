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
package com.freshbourne.io;

import com.google.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Random;


/**
 * Provides access to Pages stored in a RandomAccessFile.
 * 
 */
public class FileResourceManager implements ResourceManager {
	private RandomAccessFile handle;
	private final File file;
	private final int pageSize;
	private FileLock fileLock;
	private FileChannel ioChannel;
	private DataPage<Integer> headerPage;
	
    private final HashMap<Long, Long> pageDirectory = new HashMap<Long, Long>();

	@Inject
	FileResourceManager(@ResourceFile File f, @PageSize int pageSize){
		this.file = f;
		this.pageSize = pageSize;
	}
	
	/* (non-Javadoc)
	 * @see com.freshbourne.io.ResourceManager#open()
	 */
	@Override
	public void open() throws IOException {
		if(isOpen())
			throw new IOException("File already open");
		
		// if the file does not exist already
		if(!file.exists()){
			file.createNewFile();
		}
		
		handle = new RandomAccessFile(file, "rw");
		initIOChannel(handle);
		
		// if new file, initialize by writing header
		if(handle.length() == 0){
			ByteBuffer buf = ByteBuffer.allocate(pageSize); // default Integer size
			buf.putInt(0); // 0 pages in this file
			buf.rewind();
			ioChannel.write(buf);
		} else { // if the file already existed
			
		}
	}
	
	@Override
	public void writePage(RawPage page) throws IOException {

        if(page.resourceManager() != this)
            throw new WrongResourceManagerException(this, page);

        if(!pageDirectory.containsKey(page.id()))
            throw new PageNotFoundException(this, page);

        ensureOpen();

        ByteBuffer buffer = page.buffer();
		buffer.rewind();

        ioChannel.write(buffer, pageDirectory.get(page.id()));
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.ResourceManager#readPage(int)
	 */
	@Override
	public RawPage readPage(long pageId) throws IOException {
		ensureOpen();

		ByteBuffer buf = ByteBuffer.allocate(pageSize);
		ioChannel.read(buf, pageDirectory.get(pageId));
		return new RawPage(buf, this, pageId);
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.ResourceManager#close()
	 */
	@Override
	public void close() {
		try{
		if(ioChannel != null){
			ioChannel.close();
			ioChannel = null;
			fileLock = null;
		}
		
		if(fileLock != null && fileLock.isValid()){
			fileLock.release();
			fileLock = null;
		}
		
		if(handle != null){
			handle.close();
			handle = null;
		}
		} catch (Exception ignored) {
		}
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.ResourceManager#getPageSize()
	 */
	@Override
	public int pageSize() {
		return pageSize;
	}

	/**
	 * Generic private initializer that takes the random access file and initializes
	 * the I/O channel and locks it for exclusive use by this instance.
	 * 
	 * from minidb
	 * 
	 * @param fileHandle The random access file representing the index.
	 * @throws IOException Thrown, when the I/O channel could not be opened.
	 */
	private void initIOChannel(RandomAccessFile fileHandle)
	throws IOException
	{
		// Open the channel. If anything fails, make sure we close it again
		try {
			ioChannel = fileHandle.getChannel();
			try {
				fileLock = ioChannel.tryLock();
			}
			catch (OverlappingFileLockException oflex) {
				throw new IOException("Index file locked by other consumer.");
			}
			
			if (fileLock == null) {
				throw new IOException("Could acquire index file handle for exclusive usage. File locked otherwise.");
			}
		}
		catch (Throwable t) {
			// something failed.
			close();
			
			// propagate the exception
			if (t instanceof IOException) {
				throw (IOException) t;
			}
			else {
				throw new IOException("An error occured while opening the index: " + t.getMessage());
			}
		}
	}
	
	public boolean isOpen() {
		return !(ioChannel == null || !ioChannel.isOpen());
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return "Resource: " + file.getAbsolutePath();
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.ResourceManager#addPage(com.freshbourne.io.HashPage)
	 */
	@Override
	public RawPage addPage(RawPage page) throws IOException {
		ensureOpen();
		ensureCorrectPageSize(page);
		
		RawPage result = new RawPage(page.buffer(), this, generateId());
		page.buffer().position(0);
		ioChannel.write(page.buffer(), ioChannel.size());
		pageDirectory.put(result.id(), ioChannel.position());
		
		return result;
	}
	
	/**
	 * @param page
	 * @throws WrongPageSizeException 
	 */
	private void ensureCorrectPageSize(RawPage page) throws WrongPageSizeException {
		if(page.buffer().limit() != pageSize)
			throw new WrongPageSizeException(page, pageSize);
	}

	private long generateId(){
		return (new GregorianCalendar()).getTimeInMillis();
		
	}
	
	private void ensureOpen() throws ResourceNotOpenException{
		if(!isOpen())
			throw new ResourceNotOpenException(this);
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.ResourceManager#numberOfPages()
	 */
	@Override
	public int numberOfPages() {
		return pageDirectory.size();
	}
}
