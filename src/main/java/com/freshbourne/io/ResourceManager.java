/**
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 * 
 * (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */
package com.freshbourne.io;

import java.io.IOException;

/**
 * ResourceManager reads and writes pages from and to a resource.
 * 
 * @author "Robin Wenglewski <robin@wenglewski.de>"
 *
 */
public interface ResourceManager {
	
	
	/**
	 * write the provided HashPage to the resource. 
	 * 
	 * @param page
	 * @throws IOException if the page id not found
	 */
	public void writePage(HashPage page) throws IOException;
	
	
	/**
	 * create a new Page in the Resource. The method returns
	 * a new HashPage with the same backing buffer, but with id set.
	 * A new Page must be created since the id is immutable.
	 * 
	 * @param page
	 * @throws IOException
	 */
	public HashPage addPage(HashPage page) throws IOException;
	
	/**
	 * read the page with the given id
	 * 
	 * @param pageId
	 * @return Page
	 * @throws IOException
	 */
	public HashPage readPage(int pageId) throws IOException;
	
	/**
	 * @return size of the pages in this resource
	 */
	public int pageSize();
	
	public void open() throws IOException;
	public boolean isOpen();
	public void close();
	
}
