/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 *
 * http://creativecommons.org/licenses/by-nc/3.0/
 *
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2011 "Robin Wenglewski <robin@wenglewski.de>"
 */

package com.freshbourne.io.rm;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Header: NEXT_PAGE | FREE_PAGES_NUM | FREE_PAGE_1 | FREE_PAGE_X
 */
class ResourceHeaderOverflowPage {

	private final RawPage rawPage;
	
	public int getNextPageId() {
		return rawPage.bufferForReading(Header.NEXT_PAGE.offset).getInt();
	}

	public void initialize() throws IOException {
		rawPage.bufferForWriting(Header.NEXT_PAGE.offset).putInt(0);
		rawPage.bufferForWriting(Header.FREE_PAGES_NUM.offset).putInt(0);
	}

	public boolean containsFreePageId(int id) {
		int c = getNumberOfFreeIds();
		if(c == 0)
			return false;

		ByteBuffer buffer = rawPage.bufferForReading(Header.size());
		for(int i = 0; i < c; i++){
			if(buffer.getInt() == id)
				return true;
		}
		return false;
	}

	private int getNumberOfFreeIds() {
		return rawPage.bufferForReading(Header.FREE_PAGES_NUM.offset).getInt();
	}


	static enum Header {
		NEXT_PAGE(0),
		FREE_PAGES_NUM(Integer.SIZE / 8);
		private int offset;

		Header(int offset){
			this.offset = offset;
		}

		public static int size() {
			return 2 * Integer.SIZE / 8;
		}
	}

	ResourceHeaderOverflowPage(RawPage p){
		this.rawPage = p;
	}
}
