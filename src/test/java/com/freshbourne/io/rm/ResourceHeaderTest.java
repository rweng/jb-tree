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

import com.freshbourne.io.rm.RawPage;
import com.freshbourne.io.rm.ResourceHeader;
import com.freshbourne.io.rm.ResourceManager;
import com.google.common.collect.Lists;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ResourceHeaderTest {
	@Mock private ResourceManager rm;
	private int pageSize = 50;
	private ResourceHeader     header;
	private ArrayList<RawPage> pages;

	@BeforeMethod
	public void setUp() {
		pages = Lists.newArrayList();

		MockitoAnnotations.initMocks(this);

		when(rm.createPage()).thenReturn(newPage());

		this.header = new ResourceHeader(rm, 50);
	}

	private RawPage newPage() {
		pages.add(new RawPage(ByteBuffer.allocate(pageSize), pages.size(), rm));
		return pages.get(pages.size() - 1);
	}

	@Test
	public void initialize() {
		header.initialize();
		verify(rm).createPage();
		verify(rm).writePage(pages.get(0));
		verifyNoMoreInteractions(rm);

		RawPage firstPage = pages.get(0);
		assertThat(firstPage.bufferForReading(ResourceHeader.Header.FREE_PAGES_NUM.offset).getInt()).isEqualTo(0);
		assertThat(firstPage.bufferForReading(ResourceHeader.Header.FREE_PAGES_TOTAL.offset).getInt()).isEqualTo(0);
				assertThat(firstPage.bufferForReading(ResourceHeader.Header.LAST_ID.offset).getInt()).isEqualTo(0);
		assertThat(firstPage.bufferForReading(ResourceHeader.Header.PAGE_SIZE.offset).getInt()).isEqualTo(pageSize);
		assertThat(firstPage.bufferForReading(ResourceHeader.Header.NEXT_PAGE.offset).getInt()).isEqualTo(0);

		// for other tests depending on this one
		reset(rm);
	}

	@Test(dependsOnMethods = "initialize")
	public void load() throws IOException {
		initialize();

		ResourceHeader header2 = new ResourceHeader(rm, null);
		when(rm.getPage(0)).thenReturn(pages.get(0));
		header2.load();
		verify(rm).getPage(0);
		verifyNoMoreInteractions(rm);

		assertThat(header2.getNumberOfPages()).isEqualTo(0);
		assertThat(header2.getPageSize()).isEqualTo(pageSize);
	}

	@Test(dependsOnMethods = "initialize")
	public void generateId(){
		initialize();
		
		header.generateId();
		assertThat(pages.get(0).bufferForReading(ResourceHeader.Header.LAST_ID.offset).getInt()).isEqualTo(1);
		assertThat(header.getNumberOfPages()).isEqualTo(1);
		
		verify(rm).writePage(pages.get(0));
		verifyNoMoreInteractions(rm);
	}

	@Test(dependsOnMethods = "initialize")
	public void removeWithoutOverflow(){
		initialize();

		int[] ids = new int[]{header.generateId(), header.generateId(), header.generateId()};

		assertThat(header.getNumberOfPages()).isEqualTo(3);
		assertThat(header.contains(ids[1])).isTrue();
		
		header.removePage(ids[1]);
		assertThat(header.getNumberOfPages()).isEqualTo(2);
		assertThat(header.contains(ids[1])).isFalse();

		assertThat(header.generateId()).isEqualTo(ids[1]);
		assertThat(header.getNumberOfPages()).isEqualTo(3);
		assertThat(header.contains(ids[1])).isTrue();
	}
}
