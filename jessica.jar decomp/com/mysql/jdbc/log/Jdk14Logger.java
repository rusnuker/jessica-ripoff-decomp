/*
 * Decompiled with CFR 0.152.
 */
package com.mysql.jdbc.log;

import com.mysql.jdbc.log.Log;
import com.mysql.jdbc.log.LogUtils;
import com.mysql.jdbc.profiler.ProfilerEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Jdk14Logger
implements Log {
    private static final Level DEBUG = Level.FINE;
    private static final Level ERROR = Level.SEVERE;
    private static final Level FATAL = Level.SEVERE;
    private static final Level INFO = Level.INFO;
    private static final Level TRACE = Level.FINEST;
    private static final Level WARN = Level.WARNING;
    protected Logger jdkLogger = null;

    public Jdk14Logger(String name) {
        this.jdkLogger = Logger.getLogger(name);
    }

    public boolean isDebugEnabled() {
        return this.jdkLogger.isLoggable(Level.FINE);
    }

    public boolean isErrorEnabled() {
        return this.jdkLogger.isLoggable(Level.SEVERE);
    }

    public boolean isFatalEnabled() {
        return this.jdkLogger.isLoggable(Level.SEVERE);
    }

    public boolean isInfoEnabled() {
        return this.jdkLogger.isLoggable(Level.INFO);
    }

    public boolean isTraceEnabled() {
        return this.jdkLogger.isLoggable(Level.FINEST);
    }

    public boolean isWarnEnabled() {
        return this.jdkLogger.isLoggable(Level.WARNING);
    }

    public void logDebug(Object message) {
        this.logInternal(DEBUG, message, null);
    }

    public void logDebug(Object message, Throwable exception) {
        this.logInternal(DEBUG, message, exception);
    }

    public void logError(Object message) {
        this.logInternal(ERROR, message, null);
    }

    public void logError(Object message, Throwable exception) {
        this.logInternal(ERROR, message, exception);
    }

    public void logFatal(Object message) {
        this.logInternal(FATAL, message, null);
    }

    public void logFatal(Object message, Throwable exception) {
        this.logInternal(FATAL, message, exception);
    }

    public void logInfo(Object message) {
        this.logInternal(INFO, message, null);
    }

    public void logInfo(Object message, Throwable exception) {
        this.logInternal(INFO, message, exception);
    }

    public void logTrace(Object message) {
        this.logInternal(TRACE, message, null);
    }

    public void logTrace(Object message, Throwable exception) {
        this.logInternal(TRACE, message, exception);
    }

    public void logWarn(Object message) {
        this.logInternal(WARN, message, null);
    }

    public void logWarn(Object message, Throwable exception) {
        this.logInternal(WARN, message, exception);
    }

    private static final int findCallerStackDepth(StackTraceElement[] stackTrace) {
        int numFrames = stackTrace.length;
        for (int i = 0; i < numFrames; ++i) {
            String callerClassName = stackTrace[i].getClassName();
            if (callerClassName.startsWith("com.mysql.jdbc") && !callerClassName.startsWith("com.mysql.jdbc.compliance")) continue;
            return i;
        }
        return 0;
    }

    private void logInternal(Level level, Object msg, Throwable exception) {
        if (this.jdkLogger.isLoggable(level)) {
            String messageAsString = null;
            String callerMethodName = "N/A";
            String callerClassName = "N/A";
            if (msg instanceof ProfilerEvent) {
                messageAsString = LogUtils.expandProfilerEventIfNecessary(msg).toString();
            } else {
                Throwable locationException = new Throwable();
                StackTraceElement[] locations = locationException.getStackTrace();
                int frameIdx = Jdk14Logger.findCallerStackDepth(locations);
                if (frameIdx != 0) {
                    callerClassName = locations[frameIdx].getClassName();
                    callerMethodName = locations[frameIdx].getMethodName();
                }
                messageAsString = String.valueOf(msg);
            }
            if (exception == null) {
                this.jdkLogger.logp(level, callerClassName, callerMethodName, messageAsString);
            } else {
                this.jdkLogger.logp(level, callerClassName, callerMethodName, messageAsString, exception);
            }
        }
    }
}

