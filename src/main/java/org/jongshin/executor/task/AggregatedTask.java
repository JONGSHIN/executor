package org.jongshin.executor.task;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Used for process several task as one and join their results.
 * 
 * @author Vitalii_Kim
 *
 * @param <K>
 *            the type of key
 * @param <V>
 *            the type of computation result
 */
public class AggregatedTask<K, V> extends AbstractTask<K, V> {

	private Set<SingleTask<K, V>> tasks;

	public AggregatedTask(K key) {
		super(key);
		tasks = new CopyOnWriteArraySet<>();
	}

	public boolean addTask(SingleTask<K, V> task) {
		return tasks.add(task);
	}

	public boolean removeTask(SingleTask<K, V> task) {
		return tasks.remove(task);
	}

	public Set<SingleTask<K, V>> getTasks() {
		return tasks;
	}
}
