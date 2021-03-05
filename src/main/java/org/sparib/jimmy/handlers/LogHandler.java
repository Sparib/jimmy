package org.sparib.jimmy.handlers;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogHandler {
    public final Logger LOGGER;

    public LogHandler() {
        LOGGER = LogManager.getLogger(Class.class.getName());
    }

    public void success(String message) {
        LOGGER.log(Level.forName("SUCCESS", 350), message);
    }
}
