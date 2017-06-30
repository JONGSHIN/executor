package org.jongshin.executor.task;

/**
 * The implementation of {@link AbstractTask}.
 * 
 * @author Vitalii_Kim
 *
 * @param <K>
 *            the type of key
 * @param <V>
 *            the type of computation result
 */
public abstract class SingleTask<K, V> extends AbstractTask<K, V> {

	protected SingleTask(K key) {
		super(key);
	}

	public abstract V process();

}
