package org.jongshin.executor.oberservers;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jongshin.executor.data.Execution;
import org.jongshin.executor.data.ProcessorException;
import org.jongshin.executor.data.ScheduledExecution;
import org.jongshin.executor.data.TaskResult;
import org.jongshin.executor.task.ITask;

import com.google.common.base.Preconditions;

/**
 * The implementation of {@link IObserverManager}.
 * 
 * @author Vitalii_Kim
 *
 */
public class ObserverManagerImpl implements IObserverManager {

	@SuppressWarnings("rawtypes")
	private Map<ITask, Collection<IObserver>> observers;
	private Lock lock;

	public ObserverManagerImpl() {
		observers = new ConcurrentHashMap<>();
		lock = new ReentrantLock();
	}

	@Override
	public <K, V> void add(ITask<K, V> task, IObserver<V> observer) {
		Preconditions.checkNotNull(task, "task is null");
		Preconditions.checkNotNull(observer, "observer is null");
		@SuppressWarnings("rawtypes")
		Collection<IObserver> bindedObservers = observers.get(task);
		if (bindedObservers == null) {
			lock.lock();
			try {
				bindedObservers = observers.get(task);
				if (bindedObservers == null) {
					if (bindedObservers == null) {
						bindedObservers = new CopyOnWriteArraySet<>();
						observers.put(task, bindedObservers);
					}
				}
			} finally {
				lock.unlock();
			}
		}
		bindedObservers.add(observer);
	}

	@Override
	public <K, V> void removeAll(ITask<K, V> task) {
		Preconditions.checkNotNull(task);
		observers.remove(task);
	}

	@Override
	public <K, V> void remove(ITask<K, V> task, IObserver<V> observer) {
		Preconditions.checkNotNull(task, "task is null");
		Preconditions.checkNotNull(observer, "observer is null");
		@SuppressWarnings("rawtypes")
		Collection<IObserver> bindedObservers = observers.get(task);
		if (bindedObservers != null) {
			bindedObservers.remove(observer);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V> void notifyObservers(TaskResult<V> taskResult) {
		Preconditions.checkNotNull(taskResult);
		@SuppressWarnings("rawtypes")
		ITask task = taskResult.getTask();
		@SuppressWarnings("rawtypes")
		Collection<IObserver> bindedObservers = observers.get(task);
		if (bindedObservers == null) {
			throw new ProcessorException(String.format("Can't find any observer [task=%s]", task));
		}
		Execution execution = taskResult.getExecution();
		bindedObservers.stream().forEach(observer -> {
			switch (execution.getTaskStatus()) {
			case CANCELED: {
				observer.notifyCanceled();
				break;
			}
			case FAILED: {
				observer.notifyFailed((Throwable) taskResult.getData());
				break;
			}
			case COMPLETED: {
				observer.notifyCompleted(taskResult.getData());
				break;
			}
			default:
				break;
			}
		});
		if (execution.isCanceled()) {
			observers.remove(task);
		}
		Execution parentExecution = execution.getParentExecution();
		if (parentExecution instanceof ScheduledExecution) {
			if (!((ScheduledExecution) parentExecution).isRepeatable()) {
				observers.remove(task);
			}
		} else {
			observers.remove(task);
		}
	}
}
