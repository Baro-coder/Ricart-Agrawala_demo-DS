package sr.wat.edu.pl.core;

import java.time.Instant;

import javafx.application.Platform;
import sr.wat.edu.pl.controllers.PrimaryController;


public class Logger {
    /*
     * Interface for runtime info logging
     * 
     */
    public static enum LogLevel {
        /*
         *  Log level enumeration
         */
        DEBUG,
        INFO,
        WARNING,
        ERROR,
        CRITICAL
    }

    private static String FORMAT_STRING = "[%s]\t[%s]\t: [ %-15s] : %s";

    private static void log(LogLevel level, String subject, String msg) {
        Instant instant = Instant.now();
        String timestamp = instant.toString();
        String text = String.format(FORMAT_STRING, timestamp, level.toString(), subject, msg);
        System.err.println(text);
        Platform.runLater(() -> {
            PrimaryController.getInstance().appendLog(text);
        });
    }

    // Print log DEBUG
    public static void log_debug(String subject, String msg) {
        log(LogLevel.DEBUG, subject, msg);
    }

    // Print log INFO
    public static void log_info(String subject, String msg) {
        log(LogLevel.INFO, subject, msg);
    }

    // Print log WARNING
    public static void log_warn(String subject, String msg) {
        log(LogLevel.WARNING, subject, msg);
    }

    // Print log ERROR
    public static void log_error(String subject, String msg) {
        log(LogLevel.ERROR, subject, msg);
    }

    // Print log CRITICAL
    public static void log_critical(String subject, String msg) {
        log(LogLevel.CRITICAL, subject, msg);
    }
}
