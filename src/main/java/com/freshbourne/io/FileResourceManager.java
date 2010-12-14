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
import java.nio.channels.FileLock;
import java.nio.channels.FileChannel;


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
	private File file;
	private int pageSize;
	private int numberOfPages = 0;
	private FileLock fileLock;
	private FileChannel ioChannel;

	public FileResourceManager(File f, int pageSize){
		this.file = f;
		this.pageSize = pageSize;
	}
	
	/* (non-Javadoc)
	 * @see com.freshbourne.io.ResourceManager#open()
	 */
	@Override
	public void open() throws IOException {
		if(handle != null)
			throw new IOException("File already open");
		
		// if the file does not exist already
		if(!file.exists()){
			file.createNewFile();
		}
		
		handle = new RandomAccessFile(file, "rw");
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.ResourceManager#newPage()
	 */
	@Override
	public Page newPage() throws IOException {
		// TODO Auto-generated method stub
		
		byte[] buffer = new byte[pageSize];
		
		if(file.length() > Integer.MAX_VALUE){
			//TODO: enable this!
			throw new IOException("Cannot write to a file larger than Integer.MAX_VALUE bytes. TODO: enable this. ");
		}
		
		// when creating a new Page, the buffer array provided to the page
		// should be initialized by the Page constructor.
		// The page header should be written to the buffer
		Page page = new Page(buffer, numberOfPages() , this);
		handle.write(buffer, (int)file.length(), pageSize);
		numberOfPages++;
		return page;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.ResourceManager#writePage(com.freshbourne.io.Page)
	 */
	@Override
	public void writePage(Page page) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.ResourceManager#readPage(int)
	 */
	@Override
	public Page readPage(int pageId) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.ResourceManager#close()
	 */
	@Override
	public void close() throws IOException {
		handle.close();
		handle = null;
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
	public int numberOfPages() {
		return numberOfPages;
	}

}
