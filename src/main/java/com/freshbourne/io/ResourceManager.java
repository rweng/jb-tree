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
	
	void open() throws IOException;
	Page newPage() throws IOException;
	void writePage(Page page);
	Page readPage(int pageId);
	void close() throws IOException;
	int getPageSize();
	int numberOfPages();

}
