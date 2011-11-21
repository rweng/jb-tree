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
		list.add(new Range(-5, 5, IntegerComparator.INSTANCE));
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

		Range.merge(list, IntegerComparator.INSTANCE);
		
		assertThat(list).hasSize(1).contains(new Range(null, null));
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

	@Test(expectedExceptions = NullPointerException.class)
	public void containsNullShouldThrow(){
		list.get(0).contains(null);
	}
}
