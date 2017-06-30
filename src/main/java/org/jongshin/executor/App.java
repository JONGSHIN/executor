package org.jongshin.executor;

import java.util.concurrent.TimeUnit;

import org.jongshin.executor.data.CompositeKey;
import org.jongshin.executor.oberservers.IObserver;
import org.jongshin.executor.service.IProcessorService;
import org.jongshin.executor.service.ProcessorServiceImpl;
import org.jongshin.executor.task.SingleTask;

/**
 * 
 * @author Vitalii_Kim
 *
 */
class DfuCreateTask extends SingleTask<CompositeKey<String>, String> {

	public DfuCreateTask(CompositeKey<String> key) {
		super(key);
	}

	@Override
	public String process() {
		return "DFU Created: " + getKey();
	}

}

class ActionItemCreateTask extends SingleTask<CompositeKey<String>, String> {

	public ActionItemCreateTask(CompositeKey<String> key) {
		super(key);
	}

	@Override
	public String process() {
		return "Action Item Created: " + getKey();
	}
}

class DfuCreateObserver implements IObserver<String> {
	@Override
	public void notifyCompleted(String data) {
		System.out.println(data);
	}

	@Override
	public void notifyCanceled() {
		System.out.println("CANCELED");
	}

	@Override
	public void notifyFailed(Throwable cause) {
		System.out.println("FAILED");
	}
}

class ActionItemCreateObserver implements IObserver<String> {

	@Override
	public void notifyCompleted(String data) {
		System.out.println(data);
	}

	@Override
	public void notifyCanceled() {
		System.out.println("CANCELED");
	}

	@Override
	public void notifyFailed(Throwable cause) {
		System.out.println("FAILED");
	}
}

public class App {

	public static void main(String[] args) {
		IProcessorService ps = new ProcessorServiceImpl();

		DfuCreateTask dfuCreateTask = new DfuCreateTask(new CompositeKey<String>("DfuId"));
		ps.schedule(5, 3, TimeUnit.SECONDS, dfuCreateTask, new DfuCreateObserver());

		ActionItemCreateTask actionItemCreateTaskRP1 = new ActionItemCreateTask(
				new CompositeKey<String>("DfuId", "RP1"));
		ps.schedule(5, 3, TimeUnit.SECONDS, actionItemCreateTaskRP1, new ActionItemCreateObserver());

		ActionItemCreateTask actionItemCreateTaskRP2 = new ActionItemCreateTask(
				new CompositeKey<String>("DfuId", "RP2"));
		ps.schedule(5, 3, TimeUnit.SECONDS, actionItemCreateTaskRP2, new ActionItemCreateObserver());
		
		actionItemCreateTaskRP2.cancel();
	}
}
