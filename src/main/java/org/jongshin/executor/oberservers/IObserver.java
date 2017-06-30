package org.jongshin.executor.oberservers;

import org.jongshin.executor.task.ITask;

/**
 * The interface, which is wants to be informed of changes in {@link ITask}
 * objects.
 * 
 * @author Vitalii_Kim
 *
 * @param <V>
 *            The result of task computation
 */
public interface IObserver<V> {
	void notifyCompleted(V data);

	void notifyCanceled();

	void notifyFailed(Throwable cause);
}
