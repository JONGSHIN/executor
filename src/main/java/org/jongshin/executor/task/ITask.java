package org.jongshin.executor.task;

/**
 * Represents an action that becomes to be executed.
 * 
 * @author Vitalii_Kim
 *
 * @param <K>
 *            the type of key
 * @param <V>
 *            the type of computation result
 */
public interface ITask<K, V> {

	/**
	 * Returns task's key
	 * 
	 * @return unique identifier of task, never returns {@code null}
	 */
	K getKey();
}
