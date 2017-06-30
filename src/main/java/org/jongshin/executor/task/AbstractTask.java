package org.jongshin.executor.task;

import com.google.common.base.Preconditions;

/**
 * The implementation of {@link ITask}
 * 
 * @author Vitalii_Kim
 *
 * @param <K>
 *            the type of key
 * @param <V>
 *            the type of computation result
 */
public abstract class AbstractTask<K, V> implements ITask<K, V> {

	private final K key;
	private boolean canceled;

	/**
	 * @param key
	 *            Unique identifier of task
	 * 
	 * @throws NullPointerException
	 *             if {@code key} has {@code null} value
	 */
	AbstractTask(K key) {
		Preconditions.checkNotNull(key, "Illegal key");
		this.key = key;
	}

	@Override
	public K getKey() {
		return key;
	}

	public boolean isCanceled() {
		return canceled;
	}

	public void cancel() {
		this.canceled = true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
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
		AbstractTask other = (AbstractTask) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AbstractTask [key=" + key + "]";
	}

}
