/**
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 * 
 * (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */
package com.freshbourne.io;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileLock;
import java.nio.channels.FileChannel;
import java.nio.channels.OverlappingFileLockException;

import com.google.inject.Inject;
import com.google.inject.name.Named;


/**
 * Provides access to Pages stored in a RandomAccessFile.
 * 
 * The FileResourceManager itself has no Header. The header resides in the Pages. 
 * The ResourceManager just checks is the Page read is valid.
 * 
 * @author "Robin Wenglewski <robin@wenglewski.de>"
 *
 */
public class FileResourceManager implements ResourceManager {
	private RandomAccessFile handle;
	private final File file;
	private final int pageSize;
	private int numberOfPages = 0;
	private FileLock fileLock;
	private FileChannel ioChannel;

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
		if(handle.length() > 0 && !readPage(1).valid()){
			throw new IOException("File exists but Pages are not valid");
		}
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.ResourceManager#newPage()
	 */
	@Override
	@MustBeOpen
	public Page newPage() throws IOException {
		byte[] bytes = new byte[pageSize];
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		
		if(file.length() > Integer.MAX_VALUE){
			//TODO: enable this!
			throw new IOException("Cannot write to a file larger than Integer.MAX_VALUE bytes. TODO: enable this. ");
		}
		
		// when creating a new Page, the buffer array provided to the page
		// should be initialized by the Page constructor.
		// The page header should be written to the buffer
		Page page = new Page(buffer, getNumberOfPages() + 1 , this);
		
		numberOfPages++;
		
		return page;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.ResourceManager#writePage(com.freshbourne.io.Page)
	 */
	@Override
	@MustBeOpen
	public void writePage(Page page) throws IOException {
		ioChannel.write(page.getBuffer(), (page.getId() - 1) * pageSize);
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.ResourceManager#readPage(int)
	 */
	@Override
	@MustBeOpen
	public Page readPage(int pageId) throws IOException {
		ByteBuffer buf = ByteBuffer.wrap(new byte[getPageSize()]);
		ioChannel.read(buf, (pageId - 1) * getPageSize());
		return new Page(buf, pageId, this);
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
	public int getPageSize() {
		return pageSize;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.ResourceManager#numberOfPages()
	 */
	@Override
	@MustBeOpen
	public int getNumberOfPages() throws IOException {
		return numberOfPages;
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
}
