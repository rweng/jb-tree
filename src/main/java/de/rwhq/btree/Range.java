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

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Very generic Range object for getting values form the BTree
 *
 * @param <T>
 */
public class Range<T> {
	public static <K> Comparator<Range<K>> createRangeComparator(final Comparator<K> comparator){
		return new Comparator<Range<K>>() {
			private int compareWithNull(K k1, K k2, boolean nullIsSmallest) {
				if (k1 == null) {
					if (k2 == null) {
						return 0;
					} else {
						return nullIsSmallest ? -1 : 1;
					}
				} else if (k2 == null) {
					return nullIsSmallest ? 1 : -1;
				}

				return comparator.compare(k1, k2);
			}

			@Override
			public int compare(final Range<K> r1, final Range<K> r2) {
				int compareResult = compareWithNull(r1.getFrom(), r2.getFrom(), true);

				if (compareResult != 0)
					return compareResult;

				return compareWithNull(r1.getTo(), r2.getTo(), false);
			}
		};
	}

	/**
	 * merges the given Collection of Ranges by using the provided comparator.
	 * 
	 * @param ranges
	 * @param comparator
	 * @param <K>
	 * @return
	 */
	public static <K> TreeSet<Range<K>> merge(final Collection<Range<K>> ranges, final Comparator<K> comparator) {
		checkNotNull(ranges, "range list must not be null");
		checkNotNull(comparator, "comparator must not be null");

		TreeSet<Range<K>> tmpSet = Sets.newTreeSet(createRangeComparator(comparator));

		tmpSet.addAll(ranges);
		Range<K> last = null;

		Iterator<Range<K>> iterator = tmpSet.iterator();
		while (iterator.hasNext()) {
			Range<K> r = iterator.next();

			if (last == null) {
				last = r;
				continue;
			}

			// only if this to() is larger than last to(), extend to()
			if (last.getTo() != null) {
				if ((r.getFrom() == null || comparator.compare(last.getTo(), r.getFrom()) >= 0)) {
					if (r.getTo() == null)
						last.setTo(null);
					else if (comparator.compare(last.getTo(), r.getTo()) < 0) {
						last.setTo(r.getTo());
					}

					iterator.remove();
				} else { // separate ranges
					last = r;
				}
			} else {
				iterator.remove();
			}
		}

		return tmpSet;
	}

	private T from;
	private T to;

	private Comparator<T> comparator;

	public Comparator<T> getComparator() {
		return comparator;
	}

	public void setComparator(Comparator<T> comparator) {
		this.comparator = comparator;
	}

	public Range() {
	}

	public Range(final T from, final T to) {
		this(from, to, null);
	}

	public Range(final T from, final T to, final Comparator<T> comparator) {
		this.from = from;
		this.to = to;
		this.comparator = comparator;
	}

	public boolean contains(T obj) {
		return contains(obj, comparator);
	}

	public boolean contains(T obj, Comparator<T> comparator) {
		checkNotNull(obj, "can't check contains on null. Check from/to directly.");
		checkNotNull(comparator, "comparator must not be null for contains() to work");

		return (from == null || comparator.compare(from, obj) <= 0) &&
				(to == null || comparator.compare(obj, to) <= 0);
	}

	public T getTo() {
		return to;
	}

	public void setTo(final T to) {
		this.to = to;
	}

	public T getFrom() {
		return from;
	}

	public void setFrom(final T from) {
		this.from = from;
	}

	public String toString() {
		return Objects.toStringHelper(this)
				.add("from", from)
				.add("to", to)
				.toString();
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(from, to);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof Range) {
			final Range other = (Range) obj;
			return Objects.equal(from, other.from) &&
					Objects.equal(to, other.to);
		} else {
			return false;
		}
	}
}

