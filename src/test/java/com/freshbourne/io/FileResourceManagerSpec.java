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
import java.nio.ByteBuffer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.freshbourne.io.FileResourceManager;
import com.freshbourne.io.ResourceManager;

import  static org.junit.Assert.*;

public class FileResourceManagerSpec {
	
	private ResourceManager rm;
	private final File file;
	
	public FileResourceManagerSpec(){
		super();
		System.out.println("creating FileResourceManagerSpec");
		file = new File("/tmp/frm_test");
	}
	
	@Before
	public void setUp() throws IOException {
		if(file.exists()){
			file.delete();
		}
		
		rm = new FileResourceManager(file, PageSize.DEFAULT_PAGE_SIZE);
		rm.open();
	}
	
	@After
	public void tearDown() throws IOException{
		rm.close();
	}
	
	@Test
	public void shouldBeEmptyAtFirst() throws IOException{
		assertTrue(rm instanceof ResourceManager);
		assertEquals(PageSize.DEFAULT_PAGE_SIZE,rm.pageSize());
		assertEquals(0, file.length());
	}
	
	
	
		
//		// test rm
//		assertEquals(1, rm.getNumberOfPages());
//		
//		// test the page
//		assertFalse(p.valid());
//		p.initialize();
//		assertTrue(p.valid());
//				
//		// test saving the page
//		p.save();
//		assertEquals(PageSize.DEFAULT_PAGE_SIZE, file.length());
//		
//		rm.close();
//		rm.open();
//		p = rm.readPage(1);
//		assertTrue(p.valid());
//		assertEquals(PageSize.DEFAULT_PAGE_SIZE, p.buffer().capacity());
//		assertEquals(PageSize.DEFAULT_PAGE_SIZE - 4, p.body().capacity());
//		
//		// altering the page size should throw an error
//		rm.close();
//		rm = new FileResourceManager(file, PageSize.DEFAULT_PAGE_SIZE * 2);
//		try{
//			rm.open();
//			fail("opening a file with wrong pagesize should throw an error");
//		} catch (Exception e) {
//		}
	
	
	public void testByteBuffer(){
		ByteBuffer b = ByteBuffer.allocate(100);
		int pos = 10;
		b.position(pos);
		b.mark();
		b.position(100);
		b.reset();
		assertEquals(pos, b.position());
		
		b.slice();
		assertEquals(pos, b.position());
		b.reset();
		assertEquals(pos, b.position());
		
		
	}
}
