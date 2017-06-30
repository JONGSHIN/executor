package org.jongshin.executor.service;

import java.util.concurrent.TimeUnit;

import org.jongshin.executor.data.Execution;
import org.jongshin.executor.data.ScheduledExecution;
import org.jongshin.executor.data.TaskStatus;
import org.jongshin.executor.oberservers.IObserver;
import org.jongshin.executor.task.SingleTask;

/**
 * Service for asynchronous task execution, can execute {@code AggregatedTask}
 * and send notification about task execution
 * 
 * @author Vitalii_Kim
 *
 */
public interface IProcessorService {

	/**
	 * Creates and executes a one-shot action that becomes enabled after the
	 * given delay.
	 * 
	 * @param <K>
	 *            the type of task's key
	 * @param <V>
	 *            the type of task's computation result
	 * 
	 * @param initialDelay
	 *            the time to delay first execution
	 * 
	 * @param timeUnit
	 *            the time unit of the initialDelay and period parameters
	 * @param task
	 *            the task to execute
	 * @param observers
	 *            observer to be notified about task execution
	 * @return {@link ScheduledExecution} never returns {@code null}
	 * 
	 * @throws IllegalArgumentException
	 *             if
	 *             <li>{@code initialDelay} <= 0</li>
	 *             <li>or no one observer has been provided</li>
	 * @throws NullPointerException
	 *             if
	 *             <li>{@code task} is {@code null}</li>
	 *             <li>{@code timeUnit} is {@code null}</li>
	 */
	<K, V> ScheduledExecution schedule(long initialDelay, TimeUnit timeUnit, SingleTask<K, V> task,
			@SuppressWarnings("rawtypes") IObserver... observers);

	/**
	 * Creates and executes a periodic action that becomes enabled first after
	 * the given initial delay, and subsequently with the given delay between
	 * the termination of one execution and the commencement of the next. If any
	 * execution of the task encounters an exception, subsequent executions are
	 * suppressed. Otherwise, the task will only terminate via cancellation or
	 * termination of the executor.
	 * 
	 * @param <K>
	 *            the type of task's key
	 * @param <V>
	 *            the type of task's computation result
	 * 
	 * @param initialDelay
	 *            the time to delay first execution
	 * @param period
	 *            the delay between the termination of one execution and the
	 *            commencement of the next
	 * @param timeUnit
	 *            the time unit of the initialDelay and period parameters
	 * @param task
	 *            the task to execute
	 * @param observers
	 *            observer to be notified about task execution
	 * @return {@link ScheduledExecution} never returns {@code null}
	 * 
	 * @throws IllegalArgumentException
	 *             if
	 *             <li>{@code initialDelay} <= 0</li>
	 *             <li>{@code period} < 0</li>
	 *             <li>or no one observer has been provided</li>
	 * @throws NullPointerException
	 *             if
	 *             <li>{@code task} is {@code null}</li>
	 *             <li>{@code timeUnit} is {@code null}</li>
	 */
	<K, V> ScheduledExecution schedule(long initialDelay, long period, TimeUnit timeUnit, SingleTask<K, V> task,
			@SuppressWarnings("rawtypes") IObserver... observers);

	/**
	 * Executes specified task and returns an {@link Execution}.
	 * 
	 * @param <K>
	 *            the type of task's key
	 * @param <V>
	 *            the type of task's computation result
	 * 
	 * @param task
	 *            the task to execute
	 * @param observers
	 *            observer to be notified about task execution
	 * 
	 * @return {@link Execution} never returns {@code null}
	 *
	 * @throws IllegalArgumentException
	 *             if no one observer has been provided
	 * @throws NullPointerException
	 *             if the task is {@code null}
	 */
	<K, V> Execution execute(SingleTask<K, V> task, @SuppressWarnings("rawtypes") IObserver... observers);

	/**
	 * Returns {@link Execution} of task with specified {@code key}.
	 * 
	 * @param <K>
	 *            the type of task's key
	 * @param <V>
	 *            the type of task's computation result
	 * 
	 * @param key
	 *            unique identifier of task
	 * 
	 * @return {@link Execution} dependent to task by specified {@code key} or
	 *         {@code null} if there is no executed task with specified
	 *         {@code key}
	 * 
	 * @throws NullPointerException
	 *             if {@code key} is {@code null}
	 */
	<K, V> Execution getExecution(K key);

	/**
	 * Attempts to cancel execution of this task. This attempt will fail if the
	 * task has already completed, has already been cancelled, or could not be
	 * cancelled for some other reason.
	 * 
	 * @param <K>
	 *            the type of task's key
	 * @param <V>
	 *            the type of task's computation result
	 * @param key
	 *            unique identifier of task
	 * 
	 * @throws NullPointerException
	 *             if {@code key} is {@code null}
	 */
	<K, V> void cancel(K key);

	/**
	 * Returns status of task with specified {@code key}.
	 * 
	 * @param <K>
	 *            the type of task's key
	 * @param <V>
	 *            the type of task's computation result
	 * @param key
	 *            unique identifier of task
	 * 
	 * @return {@link TaskStatus} never returns {@code null}
	 * 
	 * @throws NullPointerException
	 *             if {@code key} is {@code null}
	 */
	<K, V> TaskStatus getTaskStatus(K key);

	/**
	 * Check, whether specified task has been canceled.
	 * 
	 * @param <K>
	 *            the type of task's key
	 * @param <V>
	 *            the type of task's computation result
	 * @param key
	 *            unique identifier of task
	 * 
	 * @return {@code true} if specified task has been canceled, {@code false}
	 *         otherwise
	 * 
	 * @throws NullPointerException
	 *             if {@code key} is {@code null}
	 */
	<K, V> boolean isCanceled(K key);

	/**
	 * Check, whether specified task is done.
	 * 
	 * @param <K>
	 *            the type of task's key
	 * @param <V>
	 *            the type of task's computation result
	 * @param key
	 *            unique identifier of task
	 * @return {@code true} if specified task is done, {@code false} otherwise
	 * 
	 * @throws NullPointerException
	 *             if {@code key} is {@code null}
	 */
	<K, V> boolean isDone(K key);
}
