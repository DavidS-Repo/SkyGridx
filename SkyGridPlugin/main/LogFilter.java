package main;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

public class LogFilter implements Filter {
    private final String[] suppressedWarnings = {
    		"Plugin is getting a faraway chunk async",
            "[EntityLookup] Entity uuid already exists:",
            "Detected setBlock in a far chunk",
            "skyvoid_worldgen"
    };

    @Override
    public boolean isLoggable(LogRecord record) {
        String message = record.getMessage();
        for (String suppressedWarning : suppressedWarnings) {
            if (message.contains(suppressedWarning)) {
                return false;
            }
        }
        return true;
    }
}
