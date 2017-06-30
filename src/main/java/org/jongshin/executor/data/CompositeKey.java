package org.jongshin.executor.data;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Preconditions;

/**
 * Used for identify the task.
 * 
 * @author Vitalii_Kim
 *
 * @param <K>
 *            the type of elements in this key
 */
public final class CompositeKey<K> {
	private final K major;
	private final List<K> minors;

	/**
	 * 
	 * @param major
	 *            the major part
	 * @param minors
	 *            the minor part
	 * 
	 * @throws NullPointerException
	 *             if
	 *             <li>{@code major} is {@code null}</li>
	 *             <li>{@code minors} is {@code null}</li>
	 */
	public CompositeKey(K major, @SuppressWarnings("unchecked") K... minors) {
		Preconditions.checkNotNull(major, "Illegal major");
		Preconditions.checkNotNull(minors, "Illegal minors");
		this.major = major;
		this.minors = Arrays.asList(minors);
	}

	public K getMajor() {
		return major;
	}

	public List<K> getMinors() {
		return Collections.unmodifiableList(minors);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((major == null) ? 0 : major.hashCode());
		result = prime * result + ((minors == null) ? 0 : minors.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("rawtypes")
		CompositeKey other = (CompositeKey) obj;
		if (major == null) {
			if (other.major != null)
				return false;
		} else if (!major.equals(other.major))
			return false;
		if (minors == null) {
			if (other.minors != null)
				return false;
		} else if (!minors.equals(other.minors))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CompositeKey [major=" + major + ", minors=" + minors + "]";
	}

}
