/**
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 * http://creativecommons.org/licenses/by-nc/3.0/
 * For alternative conditions contact the author.
 * 
 * (c) 2010 "Robin Wenglewski <robin@wenglewski.de>"
 */
package com.freshbourne.io;

/**
 * A Page is a <code>byte[]</code> from a {@link ResourceManager}.
 * 
 * If the 
 * 
 * 
 * 
 * @author "Robin Wenglewski <robin@wenglewski.de>"
 *
 */
public class Page {
	
	private byte[] buffer;
	private int id;
	private ResourceManager resourceManager;
	
	/**
	 * is set to true if the Page is valid.
	 */
	private boolean valid = false;
	
	
	public Page(byte[] buffer, int id, ResourceManager rm){
		this.buffer = buffer;
		this.id = id;
		this.resourceManager = rm;
	}
	
	public void initialize(){
		writeHeader();
	}
	
	public boolean isValid(){
		return valid;
	}
	
	
	private void writeHeader(){}
}
