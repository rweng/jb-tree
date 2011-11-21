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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Very generic Range object for getting values form the BTree
 *
 * @param <T>
 */
public class Range<T> {

	public static <K> void merge(List<Range<K>> ranges, final Comparator<K> comparator) {
		checkNotNull(ranges, "range list must not be null");
		checkNotNull(comparator, "comparator must not be null");

		// sort ranges after from key
		Collections.sort(ranges, new Comparator<Range<K>>() {
			@Override
			public int compare(final Range<K> kRange, final Range<K> kRange1) {
				if (kRange.getFrom() == null) {
					if (kRange1.getFrom() == null)
						return 0;
					else
						return -1;
				}

				if (kRange1.getFrom() == null)
					return 1;

				return comparator.compare(kRange.getFrom(), kRange1.getFrom());
			}
		});

		Range<K> last = null;
		List<Range<K>> toRemove = Lists.newArrayList();
		for (final Range<K> r : ranges) {
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

					toRemove.add(r);
				} else { // separate ranges
					last = r;
				}
			} else {
				toRemove.add(r);
			}
		}

		ranges.removeAll(toRemove);
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

	public Range(final T from, final T to, final Comparator<T> comparator){
		this.from = from;
		this.to = to;
		this.comparator = comparator;
	}

	public boolean contains(T obj){
		return contains(obj, comparator);
	}

	public boolean contains(T obj, Comparator<T> comparator){
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

