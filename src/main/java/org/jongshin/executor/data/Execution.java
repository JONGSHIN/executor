package org.jongshin.executor.data;

import java.util.concurrent.Future;

/**
 * Represents the result of an asynchronous computation. Methods are provided to
 * check if the computation is canceled, get status of task. Cancellation is
 * performed by the {@code cancel} method.
 * 
 * @author Vitalii_Kim
 *
 */
public class Execution {
	private Execution parentExecution;
	private TaskStatus taskStatus;
	private Future<?> future;
	private boolean canceled;

	public Execution() {
		this.taskStatus = TaskStatus.PENDING;
	}

	public Execution getParentExecution() {
		return parentExecution;
	}

	public void setParentExecution(Execution parentExecution) {
		this.parentExecution = parentExecution;
	}

	public TaskStatus getTaskStatus() {
		return taskStatus;
	}

	public void setTaskStatus(TaskStatus taskStatus) {
		this.taskStatus = taskStatus;
	}

	public Future<?> getFuture() {
		return future;
	}

	public void setFuture(Future<?> future) {
		this.future = future;
	}

	public boolean isCanceled() {
		return canceled;
	}

	public void cancel() {
		this.canceled = true;
	}

	@Override
	public String toString() {
		return "Execution [parentExecution=" + parentExecution + ", taskStatus=" + taskStatus + ", future=" + future
				+ ", canceled=" + canceled + "]";
	}

}
