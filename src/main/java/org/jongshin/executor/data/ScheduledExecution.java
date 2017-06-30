package org.jongshin.executor.data;

import java.util.concurrent.TimeUnit;

/**
 * Represents an action that should be acted upon after a given delay.
 * 
 * @author Vitalii_Kim
 *
 */
public class ScheduledExecution extends Execution {

	private final long initialDelay;
	private final long period;
	private final TimeUnit timeUnit;

	public ScheduledExecution(long initialDelay, long period, TimeUnit timeUnit) {
		this.initialDelay = initialDelay;
		this.period = period;
		this.timeUnit = timeUnit;
	}

	public long getInitialDelay() {
		return initialDelay;
	}

	public long getPeriod() {
		return period;
	}

	public TimeUnit getTimeUnit() {
		return timeUnit;
	}

	public boolean isRepeatable() {
		return period != 0;
	}

	@Override
	public String toString() {
		return "ScheduledExecution [initialDelay=" + initialDelay + ", period=" + period + ", timeUnit=" + timeUnit
				+ "]";
	}

}
