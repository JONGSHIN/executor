package org.jongshin.executor.data;

import org.jongshin.executor.task.ITask;

/**
 * Wrapper object, which is passed to observer manager.
 * 
 * @author Vitalii_Kim
 *
 * @param <V>
 *            The result of task computation
 * 
 */
public class TaskResult<V> {
	@SuppressWarnings("rawtypes")
	private final ITask task;
	private final V data;
	private final Execution execution;

	public TaskResult(@SuppressWarnings("rawtypes") ITask task, V data, Execution execution) {
		this.task = task;
		this.data = data;
		this.execution = execution;
	}

	@SuppressWarnings("rawtypes")
	public ITask getTask() {
		return task;
	}

	public V getData() {
		return data;
	}

	public Execution getExecution() {
		return execution;
	}

	@Override
	public String toString() {
		return "TaskResult [task=" + task + ", data=" + data + ", execution=" + execution + "]";
	}

}
