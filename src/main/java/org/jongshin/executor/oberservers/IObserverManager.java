package org.jongshin.executor.oberservers;

import org.jongshin.executor.data.TaskResult;
import org.jongshin.executor.task.ITask;

/**
 * The interface responsible for managing observers and notifying them of any
 * change in dependent task object.
 * 
 * @author Vitalii_Kim
 *
 */
public interface IObserverManager {

	/**
	 * Associates the specified observer with the specified task.
	 * 
	 * @param <K>
	 *            the type of task's key
	 * @param <V>
	 *            the type of task's computation result
	 * 
	 * @param task
	 *            the task, with which the specified observer is to be
	 *            associated
	 * @param observer
	 *            the observer to be associated with the specified task
	 * @throws NullPointerException
	 *             if
	 *             <li>{@code task} is {@code null}</li>
	 *             <li>{@code observer} is {@code null}</li>
	 */
	<K, V> void add(ITask<K, V> task, IObserver<V> observer);

	/**
	 * Removes all observers associated with specified task.
	 * 
	 * @param <K>
	 *            the type of task's key
	 * @param <V>
	 *            the type of task's computation result
	 *
	 * @param task
	 *            the task whose mapping is to be removed from observers
	 * 
	 * @throws NullPointerException
	 *             if {@code task} is {@code null}
	 */
	<K, V> void removeAll(ITask<K, V> task);

	/**
	 * Removes specified observer associated with specified task.
	 * 
	 * @param <K>
	 *            the type of task's key
	 * @param <V>
	 *            the type of task's computation result
	 * @param task
	 *            the task whose mapping is to be removed from observers
	 * @param observer
	 *            the observer to be removed
	 * 
	 * @throws NullPointerException
	 *             if
	 *             <li>{@code task} is {@code null}</li>
	 *             <li>{@code observer} is {@code null}</li>
	 */
	<K, V> void remove(ITask<K, V> task, IObserver<V> observer);

	/**
	 * This method is called whenever the {@code ITask} is changed. An
	 * application calls an Observable object's notifyObservers method to have
	 * all the object's observers notified of the change.
	 * 
	 * @param <V>
	 *            the type of task's computation result
	 * @param taskResult
	 *            The result of task computation
	 * @throws NullPointerException
	 *             if {@code taskResult} has is {@code null}
	 */
	<V> void notifyObservers(TaskResult<V> taskResult);
}
