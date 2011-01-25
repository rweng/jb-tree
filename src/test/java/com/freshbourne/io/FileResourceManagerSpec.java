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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class FileResourceManagerSpec extends ResourceManagerSpec {
	
	private final File file = new File("/tmp/frm_test");
	private RawPage page;
	private FileResourceManager rm;

	/* (non-Javadoc)
	 * @see com.freshbourne.io.ResourceManagerSpec#createResourceManager()
	 */
	@Override
	protected ResourceManager createOpenResourceManager() {
		if(file.exists()){
			file.delete();
		}
		
		rm = new FileResourceManager(file, PageSize.DEFAULT_PAGE_SIZE);
		
		try {
			rm.open();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		return rm;
	}
	
	@Test
	public void firstPageShouldBeWrittenAfterOpen() throws IOException{
		rm.close();
		assertEquals(PageSize.DEFAULT_PAGE_SIZE, file.length());
	}
}
