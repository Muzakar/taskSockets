package com.mk.task.util;

import com.mk.task.player.InitiatorTest;

import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Adds the logs into a list for log assertions.
 * Eg: {@link InitiatorTest}
 */
public class LogHandler extends Handler {

    private final List<String> logList;

    public LogHandler(List<String> logList) {
        this.logList = logList;
    }

    @Override
    public void publish(LogRecord record) {
        logList.add(record.getLevel() + " - " + record.getMessage());
    }

    @Override
    public void flush() {
        // not required
    }

    @Override
    public void close() throws SecurityException {
        // not required
    }
}
