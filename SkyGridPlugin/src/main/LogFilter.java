package main;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

class LogFilter implements Filter {
	private final String suppressedWarning = "Plugin is getting a faraway chunk async";

	@Override
	public boolean isLoggable(LogRecord record) {
		// Avoid triggering additional log events within this method
		return !record.getMessage().contains(suppressedWarning);
	}
}