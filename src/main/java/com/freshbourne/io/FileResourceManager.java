/*
 * Copyright (c) 2011 Robin Wenglewski <robin@wenglewski.de>
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
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
	private ResourceHeader header;
	
	
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
			throw new IllegalStateException("Resource already open");
		
		// if the file does not exist already
		if(!file.exists()){
			file.createNewFile();
		}
		
		initIOChannel(file);
		this.header = new ResourceHeader(ioChannel, pageSize);
		
		
		if(handle.length() == 0){
			header.initialize();
		} else {
			// load header if file existed
			header.load();
		}
	}
	
	@Override
	public void writePage(RawPage page) {

        ensureOpen();
        ensurePageExists(page.id());
        
        ByteBuffer buffer = page.bufferAtZero();

		try{
			Long offset = header.getPageOffset(page.id());
			ioChannel.write(buffer, offset);
		} catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.ResourceManager#readPage(int)
	 */
	@Override
	public RawPage readPage(long pageId) {
		
		ensureOpen();
		ensurePageExists(pageId);

		ByteBuffer buf = ByteBuffer.allocate(pageSize);
		
		try{
			ioChannel.read(buf, header.getPageOffset(pageId));
		} catch(IOException e){
			e.printStackTrace();
			System.exit(1);
		}
		
		return new RawPage(buf, pageId, this);
	}

	/**
	 * @param pageId
	 * @throws PageNotFoundException 
	 */
	private void ensurePageExists(long pageId) throws PageNotFoundException {
		if(!header.contains(pageId))
            throw new PageNotFoundException(this, pageId);
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.ResourceManager#close()
	 */
	@Override
	public void close() throws IOException {
		if(header != null){
			header.writeToFile();
			header = null;
		}
		
		try{
			if (fileLock != null && fileLock.isValid()) {
				fileLock.release();
				fileLock = null;
			}
			
			if(ioChannel != null){
				ioChannel.close();
				ioChannel = null;
			}

			if (handle != null) {
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
	private void initIOChannel(File file)
	throws IOException {
		handle = new RandomAccessFile(file, "rw");
		
		// Open the channel. If anything fails, make sure we close it again
		try {
			ioChannel = handle.getChannel();
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
	
	@Override
	public boolean isOpen() {
		return !(ioChannel == null || !ioChannel.isOpen());
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString(){
		return "Resource: " + file.getAbsolutePath();
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.ResourceManager#addPage(com.freshbourne.io.HashPage)
	 */
	@Override
	public RawPage addPage(RawPage page) {
		ensureOpen();
		ensureCorrectPageSize(page);
		
		RawPage result = new RawPage(page.buffer(), RawPage.generateId(), this);
		page.buffer().position(0);
		
		try {
			ioChannel.write(page.buffer(), ioChannel.size());
			header.add(result.id());
		} catch (DuplicatePageIdException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		return result;
	}
	
	/**
	 * @param page
	 * @throws WrongPageSizeException 
	 */
	private void ensureCorrectPageSize(RawPage page) {
		if(page.buffer().limit() != pageSize)
			throw new WrongPageSizeException(page, pageSize);
	}

	private void ensureOpen() {
		if(!isOpen())
			throw new IllegalStateException("Resource is not open: " + toString());
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.ResourceManager#numberOfPages()
	 */
	@Override
	public int numberOfPages() {
		return header.getNumberOfPages();
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.ResourceManager#createPage()
	 */
	@Override
	public RawPage createPage() {
		ensureOpen();
		
		ByteBuffer buf = ByteBuffer.allocate(pageSize);
		RawPage result = new RawPage(buf, RawPage.generateId(), this);
		try {
			header.add(result.id());
		} catch (DuplicatePageIdException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		return result;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.ResourceManager#removePage(long)
	 */
	@Override
	public void removePage(long pageId) {
		Long lastPageId = header.getLastPageId();
		
		
		// copy the last entry to this position
		try {
			if (pageId == lastPageId) { // last page?
				header.removeLastId();
				ioChannel.truncate(header.getNumberOfPages() * pageSize);
			} else {
				RawPage last = readPage(lastPageId);
				header.replaceId(pageId, lastPageId);
				header.removeLastId();
				writePage(last);

				ioChannel.truncate(header.getNumberOfPages() * pageSize);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
