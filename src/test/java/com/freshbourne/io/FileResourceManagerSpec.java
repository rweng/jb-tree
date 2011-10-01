/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */

package com.freshbourne.io;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import static org.junit.Assert.*;

public class FileResourceManagerSpec extends ResourceManagerSpec {
	
	private final static String filePath = "/tmp/frm_test";
	private final File file = new File(filePath);
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
		rm = new FileResourceManager(file, PageSize.DEFAULT_PAGE_SIZE, false);
		
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
		assertEquals(PageSize.DEFAULT_PAGE_SIZE, rFile.readInt());
		assertEquals(1, rFile.readInt());
		rFile.close();
	}

	/* (non-Javadoc)
	 * @see com.freshbourne.io.ResourceManagerSpec#testSync()
	 */
	@Override @Test
	public void testSync() {
		rm = (FileResourceManager) createNewOpenResourceManager();
		RawPage p = rm.createPage();
		int testInt = 5343;
		p.bufferForWriting(0).putInt(testInt);
		
		RandomAccessFile handle = rm.getHandle();
		try {
			handle.seek(rm.pageSize());
		
			assertFalse(testInt == handle.readInt());
		} catch (IOException ignored) { // ignore, we dont care whether the empty page has been written or not
		}
		
		
		rm.sync();
		
		try {
			handle.seek(rm.pageSize());
			assertEquals(testInt, handle.readInt());
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
		
		
		
	}
}
