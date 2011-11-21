/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License:
 *
 * http://creativecommons.org/licenses/by-nc/3.0/
 *
 * For alternative conditions contact the author.
 *
 * Copyright (c) 2011 "Robin Wenglewski <robin@wenglewski.de>"
 */

package de.rwhq.btree;

import com.google.common.collect.Lists;
import de.rwhq.comparator.IntegerComparator;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public class RangeTest {
	private List<Range<Integer>> list;

	@BeforeMethod
	public void setUp(){
		list = Lists.newArrayList();
		list.add(new Range(-5, 5));
		list.add(new Range(0, 10));
		list.add(new Range(100, 1000));
	}

	@Test
	public void merge(){
		Range.merge(list, IntegerComparator.INSTANCE);
		assertThat(list.size()).isEqualTo(2);
		assertThat(list).contains(new Range(-5, 10), new Range(100, 1000));
	}

	@Test
	public void mergeWithNullTo(){
		list.add(new Range(500, null));
		Range.merge(list, IntegerComparator.INSTANCE);
		assertThat(list).contains(new Range(100, null));
	}

	@Test
	public void mergeWithNullFrom(){
		list.add(new Range(null, 0));
		Range.merge(list, IntegerComparator.INSTANCE);
		assertThat(list).contains(new Range(null, 10));
	}
}
