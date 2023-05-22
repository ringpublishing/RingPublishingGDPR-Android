package com.ringpublishing.gdpr.internal.log;

import com.ringpublishing.gdpr.LogListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Module logger object
 *
 * This class log by default all module logs on console.
 * You can add your logger to additional handle logs.
 * By default logger have enable log levels: info, warn, error
 *
 * Enable debug logs using method 'debugLogEnabled(enabled: Boolean)'
 * Add own log listener using method 'addLogListener(logListener: LogListener)'
 *
 * Example:
 * Logger.info("My message")
 */
public class Logger
{
    private List<LogListener> logListeners = new ArrayList<>();

    private boolean debugLogEnabled;

    private static Logger instance;

    private Logger()
    {
    }

    public static Logger get()
    {
        if(instance == null)
        {
            instance = new Logger();
            instance.addLogListener(new ConsoleLogger());
        }
        return instance;
    }
    /**
     * Enable or disable debug logs
     *
     * @param enabled turn on logs when is set to true
     */
    public void debugLogEnabled(boolean enabled)
    {
        debugLogEnabled = enabled;
        info("Set debug log enabled: $enabled");
    }

    /**
     * Add log listener to print logs also by application
     * Is possible to add many loggers that implement 'LogListener'
     * When the same logger will be added twice, then second one will be ignored
     * To remove logger use method 'removeLogListener(logListener: LogListener)'
     */
    public void addLogListener(LogListener logListener)
    {
        logListeners.add(logListener);
    }

    /**
     * Remove application "LogListener" implementation
     */
    public void removeLogListener(LogListener logListener)
    {
        logListeners.remove(logListener);
    }

    /**
     * Debug log
     *
     * @param message will be printed by default on console and also delivered to all added LogListeners
     */
    public void debug(String message)
    {
        if (!debugLogEnabled) return;

        for(LogListener listener: logListeners)
        {
            listener.debug(message);
        }
    }

    /**
     * Info log
     *
     * @param message will be printed by default on console and also delivered to all added LogListeners
     */
    public void info(String message)
    {
        for(LogListener listener: logListeners)
        {
            listener.info(message);
        }
    }

    /**
     * Warn log
     *
     * @param message will be printed by default on console and also delivered to all added LogListeners
     */
    public void warn(String message)
    {
        for(LogListener listener: logListeners)
        {
            listener.warn(message);
        }
    }

    /**
     * Error log
     *
     * @param message will be printed by default on console and also delivered to all added LogListeners
     */
    public void error(String message)
    {
        for(LogListener listener: logListeners)
        {
            listener.error(message);
        }
    }

}
