package service;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A utility class for logging messages in the application.
 * This class provides a simple interface for logging information messages.
 */
public class MyLogger {

    /**
     * The logger instance used for logging messages.
     * It uses the global logger name.
     */
    private final static Logger LOGGER =
            Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    /**
     * Logs an information message.
     * The message is prefixed with "CSC311_Log__" for easy identification.
     *
     * @param msg The message to be logged.
     */
    public static void makeLog(String msg) {
        LOGGER.log(Level.INFO, "CSC311_Log__ " + msg);

    }
}
