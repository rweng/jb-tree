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

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Very generic Range object for getting values form the BTree
 *
 * @param <T>
 */
public class Range<T> {
	private T from;
	private T to;

	public Range() {
	}

	public Range(final T from, final T to) {
		this.from = from;
		this.to = to;
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

					ranges.remove(r);
				} else { // separate ranges
					last = r;
				}
			}
		}
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

