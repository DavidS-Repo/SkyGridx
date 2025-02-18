package main;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorServiceProvider {
	private static final ExecutorService executorService = Executors.newFixedThreadPool(PluginSettings.THREADS());

	private ExecutorServiceProvider() {
		// Private constructor to prevent instantiation
	}

	public static ExecutorService getExecutorService() {
		return executorService;
	}

	public static void shutdown() {
		executorService.shutdown();
	}
}