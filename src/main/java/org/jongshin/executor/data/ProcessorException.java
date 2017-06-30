package org.jongshin.executor.data;

/**
 * The main exception of processor.
 * 
 * @author Vitalii_Kim
 *
 */
public class ProcessorException extends RuntimeException {
	private static final long serialVersionUID = 7215407476137050400L;

	public ProcessorException(String message, Throwable cause) {
		super(message, cause);
	}

	public ProcessorException(String message) {
		super(message);
	}

	public ProcessorException(Throwable cause) {
		super(cause);
	}
}
