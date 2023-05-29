package com.ringpublishing.gdpr;

/**
 * Interface used to implement custom log listener class
 *
 * To add custom logger class to Logger use method 'Logger.get().addListener(LogListener logListener)'
 * To remove custom logger class from Logger use method 'Logger.get().removeListener(LogListener logListener)'
 *
 * Example:
 * Logger.addListener(ApplicationCustomLogger())
 */
public interface LogListener
{

    /**
     * Debug log
     * @param message is debug log message from module
     */
    void debug(String message);

    /**
     * Info log
     * @param message is info log message from module
     */
    void info(String message);

    /**
     * Warn log
     * @param message is warn log message from module
     */
    void warn(String message);

    /**
     * Error log
     * @param message is error log message from module
     */
    void error(String message);
}
