package org.jongshin.executor.service;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

import org.jongshin.executor.data.CompositeKey;
import org.jongshin.executor.data.Execution;
import org.jongshin.executor.data.ProcessorException;
import org.jongshin.executor.data.ScheduledExecution;
import org.jongshin.executor.data.TaskResult;
import org.jongshin.executor.data.TaskStatus;
import org.jongshin.executor.oberservers.IObserver;
import org.jongshin.executor.oberservers.IObserverManager;
import org.jongshin.executor.oberservers.ObserverManagerImpl;
import org.jongshin.executor.task.AbstractTask;
import org.jongshin.executor.task.AggregatedTask;
import org.jongshin.executor.task.SingleTask;

import com.google.common.base.Preconditions;

/**
 * The implementation of {@link IProcessorService}
 * 
 * @author Vitalii_Kim
 *
 */
public class ProcessorServiceImpl implements IProcessorService {

	private static final int DEFAULT_CORE_POOL_SIZE = 10;
	private static final int DEFAULT_PARALLEL_THREADS = 5;
	private static final int DEFAULT_KEEP_ALIVE_TIME_MINUTES = 5;

	private ExecutorService executorService;
	private ScheduledExecutorService scheduledExecutorService;
	private IObserverManager observerManager;
	private Lock lock;

	@SuppressWarnings("rawtypes")
	private Map<AbstractTask, Execution> executedTasks;
	@SuppressWarnings("rawtypes")
	private Map<AbstractTask, ScheduledExecution> scheduledTasks;

	public ProcessorServiceImpl() {
		executorService = Executors.newCachedThreadPool();
		scheduledExecutorService = Executors.newScheduledThreadPool(DEFAULT_CORE_POOL_SIZE);
		executedTasks = new ConcurrentHashMap<>();
		scheduledTasks = new ConcurrentHashMap<>();
		observerManager = new ObserverManagerImpl();
		lock = new ReentrantLock();
	}

	private <K, V> AggregatedTask<K, V> getAppropriateAggregatedTask(CompositeKey<K> key) {
		Preconditions.checkNotNull(key);
		K major = key.getMajor();
		AbstractTask<K, V> task = getTask(major);
		if (task instanceof AggregatedTask) {
			return (AggregatedTask<K, V>) task;
		}
		return new AggregatedTask<K, V>(major);
	}

	@SuppressWarnings("unchecked")
	private <K, V> AbstractTask<K, V> getTask(K key) {
		Preconditions.checkNotNull(key);
		return Stream.concat(executedTasks.keySet().stream(), scheduledTasks.keySet().stream()).filter(task -> {
			return task.getKey().equals(key);
		}).findFirst().orElse(null);
	}

	private <K, V> Execution newExecution(AbstractTask<K, V> task) {
		Preconditions.checkNotNull(task);
		Execution execution = new Execution();
		if (task.isCanceled()) {
			execution.cancel();
		}
		executedTasks.put(task, execution);
		return execution;
	}

	private <K, V> ScheduledExecution newScheduledExecution(AbstractTask<K, V> task, long initialDelay, long period,
			TimeUnit timeUnit) {
		Preconditions.checkNotNull(task);
		ScheduledExecution scheduledExecution = new ScheduledExecution(initialDelay, period, timeUnit);
		if (task.isCanceled()) {
			scheduledExecution.cancel();
		}
		scheduledTasks.put(task, scheduledExecution);
		return scheduledExecution;
	}

	@Override
	public <K, V> ScheduledExecution schedule(long initialDelay, TimeUnit timeUnit, SingleTask<K, V> task,
			@SuppressWarnings("rawtypes") IObserver... observers) {
		return schedule(initialDelay, 0, timeUnit, task, observers);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <K, V> ScheduledExecution schedule(long initialDelay, long period, TimeUnit timeUnit, SingleTask<K, V> task,
			@SuppressWarnings("rawtypes") IObserver... observers) {
		if (initialDelay <= 0) {
			throw new IllegalArgumentException(String.format("Illegal initialDelay=%d", initialDelay));
		}
		if (period < 0) {
			throw new IllegalArgumentException(String.format("Illegal period=%d", period));
		}
		Preconditions.checkNotNull(timeUnit);
		Preconditions.checkNotNull(task);
		if (observers.length == 0) {
			throw new IllegalArgumentException("At least 1 observer should be provided");
		}
		if (task.getKey() instanceof CompositeKey) {
			CompositeKey<K> compositeKey = (CompositeKey<K>) task.getKey();
			AggregatedTask<K, V> aggregatedTask = getAppropriateAggregatedTask(compositeKey);
			aggregatedTask.addTask(task);
			observerManager.removeAll(task);
			Arrays.stream(observers).forEach(observer -> {
				observerManager.add(task, observer);
			});
			ScheduledExecution scheduledExecution = scheduledTasks.get(aggregatedTask);
			if (scheduledExecution == null) {
				lock.lock();
				try {
					scheduledExecution = scheduledTasks.get(aggregatedTask);
					if (scheduledExecution == null) {
						scheduledExecution = scheduleTask(initialDelay, period, timeUnit, aggregatedTask);
					}
				} finally {
					lock.unlock();
				}
			}
			return scheduledExecution;
		}
		ScheduledExecution scheduledExecution = scheduledTasks.get(task);
		if (scheduledExecution != null) {
			scheduledExecution.cancel();
			observerManager.removeAll(task);
		}
		Arrays.stream(observers).forEach(observer -> {
			observerManager.add(task, observer);
		});
		return scheduleTask(initialDelay, period, timeUnit, task);
	}

	private <K, V> ScheduledExecution scheduleTask(long initialDelay, long period, TimeUnit timeUnit,
			AbstractTask<K, V> task) {
		ScheduledExecution scheduledExecution = newScheduledExecution(task, initialDelay, period, timeUnit);
		ScheduledFuture<?> scheduledFuture;
		if (scheduledExecution.isRepeatable()) {
			scheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
				process(task, scheduledExecution);
			}, initialDelay, period, timeUnit);
		} else {
			scheduledFuture = scheduledExecutorService.schedule(() -> {
				process(task, scheduledExecution);
			}, initialDelay, timeUnit);
		}
		scheduledExecution.setFuture(scheduledFuture);
		return scheduledExecution;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <K, V> Execution execute(SingleTask<K, V> task, @SuppressWarnings("rawtypes") IObserver... observers) {
		Preconditions.checkNotNull(task, "task is null");
		if (observers.length == 0) {
			throw new IllegalArgumentException("At least 1 observer should be provided");
		}
		Arrays.stream(observers).forEach(observer -> {
			observerManager.add(task, observer);
		});
		Execution execution = executedTasks.get(task);
		if (execution == null) {
			lock.lock();
			try {
				execution = executedTasks.get(task);
				if (execution == null) {
					execution = executeTask(task);
				}
			} finally {
				lock.unlock();
			}
		}
		return execution;
	}

	private <K, V> Execution executeTask(AbstractTask<K, V> task) {
		Execution execution = newExecution(task);
		Future<?> future = executorService.submit(() -> {
			process(task, execution);
		});
		execution.setFuture(future);
		return execution;
	}

	private <K, V> void process(AbstractTask<K, V> task, Execution execution) {
		try {
			execution.setTaskStatus(TaskStatus.STARTED);
			if (task instanceof AggregatedTask) {
				processAggregatedTask((AggregatedTask<K, V>) task, execution);
			} else {
				processSingleTask((SingleTask<K, V>) task, execution);
			}
		} catch (RuntimeException e) {
			onTaskFailed(task, e, execution);
		} finally {
			executedTasks.remove(task);
			if (execution instanceof ScheduledExecution) {
				if (execution.isCanceled() || !((ScheduledExecution) execution).isRepeatable()) {
					scheduledTasks.remove(task);
				}
			}
		}
	}

	private <K, V> void processAggregatedTask(AggregatedTask<K, V> aggregatedTask, Execution execution) {
		if (execution.isCanceled()) {
			execution.setTaskStatus(TaskStatus.CANCELED);
			execution.getFuture().cancel(false);
			aggregatedTask.getTasks().stream().forEach(innerTask -> {
				innerTask.cancel();
			});
		}
		ExecutorService executorService = Executors.newFixedThreadPool(DEFAULT_PARALLEL_THREADS);
		aggregatedTask.getTasks().forEach(innerTask -> {
			Execution innerExecution = newExecution(innerTask);
			innerExecution.setParentExecution(execution);
			Future<?> future = executorService.submit(() -> {
				try {
					innerExecution.setTaskStatus(TaskStatus.STARTED);
					if (innerExecution.isCanceled()) {
						onTaskCanceled(innerTask, innerExecution);
						aggregatedTask.removeTask(innerTask);
					} else {
						V result = innerTask.process();
						onTaskCompleted(innerTask, result, innerExecution);
					}
				} catch (RuntimeException e) {
					onTaskFailed(innerTask, e, innerExecution);
				} finally {
					executedTasks.remove(innerTask);
				}
			});
			innerExecution.setFuture(future);
		});
		executorService.shutdown();
		try {
			if (!executorService.isShutdown()) {
				boolean terminated = executorService.awaitTermination(DEFAULT_KEEP_ALIVE_TIME_MINUTES,
						TimeUnit.MINUTES);
				if (!terminated) {
					executorService.shutdownNow();
					throw new ProcessorException("Some tasks has been terminated, before completion");
				}
			}
		} catch (InterruptedException e) {
			throw new ProcessorException(e);
		}
	}

	private <K, V> void onTaskCanceled(AbstractTask<K, V> task, Execution execution) {
		execution.setTaskStatus(TaskStatus.CANCELED);
		observerManager.notifyObservers(new TaskResult<>(task, null, execution));
		execution.getFuture().cancel(false);
	}

	private <K, V> void onTaskCompleted(AbstractTask<K, V> task, V data, Execution execution) {
		execution.setTaskStatus(TaskStatus.COMPLETED);
		observerManager.notifyObservers(new TaskResult<>(task, data, execution));
	}

	private <K, V> void onTaskFailed(AbstractTask<K, V> task, Throwable cause, Execution execution) {
		execution.setTaskStatus(TaskStatus.FAILED);
		observerManager.notifyObservers(new TaskResult<>(task, cause, execution));
	}

	private <K, V> void processSingleTask(SingleTask<K, V> singleTask, Execution execution) {
		if (execution.isCanceled()) {
			onTaskCanceled(singleTask, execution);
		} else {
			V result = singleTask.process();
			onTaskCompleted(singleTask, result, execution);
		}
	}

	@Override
	public <K, V> Execution getExecution(K key) {
		Preconditions.checkNotNull(key);
		Execution execution = null;
		AbstractTask<K, V> task = getTask(key);
		if (task != null) {
			execution = executedTasks.get(task);
			if (execution == null) {
				execution = scheduledTasks.get(task);
			}
		}
		return execution;
	}

	@Override
	public <K, V> void cancel(K key) {
		Preconditions.checkNotNull(key);
		Execution execution = getExecution(key);
		if (execution != null) {
			execution.cancel();
		}
	}

	@Override
	public <K, V> TaskStatus getTaskStatus(K key) {
		Preconditions.checkNotNull(key);
		Execution execution = getExecution(key);
		if (execution == null) {
			return TaskStatus.NOT_STARTED;
		}
		return execution.getTaskStatus();
	}

	@Override
	public <K, V> boolean isCanceled(K key) {
		Preconditions.checkNotNull(key);
		Execution execution = getExecution(key);
		return execution != null && execution.isCanceled();
	}

	@Override
	public <K, V> boolean isDone(K key) {
		Preconditions.checkNotNull(key);
		Execution execution = getExecution(key);
		return execution != null && execution.getFuture().isDone();
	}
}
