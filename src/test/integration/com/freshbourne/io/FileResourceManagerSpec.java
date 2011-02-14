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

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import static org.junit.Assert.assertEquals;

public class FileResourceManagerSpec extends ResourceManagerSpec {
	
	private final File file = new File("/tmp/frm_test");
	private RawPage page;
	private FileResourceManager rm;

	/* (non-Javadoc)
	 * @see com.freshbourne.io.ResourceManagerSpec#createResourceManager()
	 */
	@Override
	protected ResourceManager createNewOpenResourceManager() {
		if(file.exists()){
			file.delete();
		}
		
		return createOpenResourceManager();
	}
	
	@Override
	protected ResourceManager createOpenResourceManager(){
		rm = new FileResourceManager(file, PageSize.DEFAULT_PAGE_SIZE);
		
		try {
			rm.open();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		return rm;
	}
	
	@Test 
	public void shouldWriteOutHeaderCorrectly() throws IOException{
		rm = (FileResourceManager) createNewOpenResourceManager();
		rm.createPage();
		rm.close();
		
		RandomAccessFile rFile = new RandomAccessFile(file, "rw");
		assertEquals(1, rFile.readInt());
		assertEquals(0, rFile.readInt());
		rFile.close();
	}
}
