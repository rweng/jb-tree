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
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import static org.fest.assertions.Assertions.assertThat;

public class RangeTest {
	private List<Range<Integer>> list;

	@Before
	public void setUp(){
		list = Lists.newArrayList();
		list.add(new Range(-5, 5, IntegerComparator.INSTANCE));
		list.add(new Range(0, 10));
		list.add(new Range(100, 1000));
	}

	@Test
	public void merge(){
		TreeSet<Range<Integer>> merge = Range.merge(list, IntegerComparator.INSTANCE);
		assertThat(merge).hasSize(2).contains(new Range(-5, 10), new Range(100, 1000));
	}

	@Test
	public void mergeNotOnlyByFrom(){
		ArrayList<Range<Integer>> rangeList = Lists.newArrayList();
		rangeList.add(new Range(50, 55));
		rangeList.add(new Range(52, 53));
		rangeList.add(new Range(49, 53));
		rangeList.add(new Range(52, 56));

		TreeSet<Range<Integer>> merge = Range.merge(rangeList, IntegerComparator.INSTANCE);
		assertThat(merge).hasSize(1).contains(new Range(49, 56));

	}

	@Test
	public void mergeWithNullTo(){
		list.add(new Range(500, null));
		TreeSet<Range<Integer>> merge = Range.merge(list, IntegerComparator.INSTANCE);
		assertThat(merge).contains(new Range(100, null));
	}

	@Test
	public void mergeWithNullFrom(){
		list.add(new Range(null, 0));
		TreeSet<Range<Integer>> merge = Range.merge(list, IntegerComparator.INSTANCE);
		assertThat(merge).contains(new Range(null, 10));
	}

	@Test
	public void mergeWithLowerRangeLater(){
		list.add(new Range(-6,-4));
		Range.merge(list, IntegerComparator.INSTANCE);
		assertThat(list).contains(new Range(-6, 10));
	}

	@Test
	public void mergeWithNulls(){
		list = Lists.newArrayList();
		list.add(new Range(25, null));
		list.add(new Range(null, null));
		list.add(new Range(null, 23));

		TreeSet<Range<Integer>> merge = Range.merge(list, IntegerComparator.INSTANCE);

		assertThat(merge).hasSize(1).contains(new Range(null, null));
	}

	@Test
	public void mergeWithDuplicates(){
		list = Lists.newArrayList();
		list.add(new Range(1,3));
		list.add(new Range(1,3));
		list.add(new Range(9,10));

		TreeSet<Range<Integer>> merged = Range.merge(list, IntegerComparator.INSTANCE);
		assertThat(merged).hasSize(2).contains(new Range(1,3), new Range(9,10));
	}

	@Test
	public void contains(){
		Range<Integer> range = list.get(0);

		assertThat(range.contains(0)).isTrue();
		assertThat(range.contains(-5)).isTrue();
		assertThat(range.contains(5)).isTrue();

		assertThat(range.contains(6)).isFalse();
		assertThat(range.contains(-6)).isFalse();
	}

	@Test(expected = NullPointerException.class)
	public void containsNullShouldThrow(){
		list.get(0).contains(null);
	}
}
