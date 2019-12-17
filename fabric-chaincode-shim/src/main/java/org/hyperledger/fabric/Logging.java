/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.LogManager;

/**
 * Assistance class to use when logging.
 *
 * For chaincode/contract implementations please use java.util.logging or your
 * own framework. All the Hyperledger Fabric code here is logged in loggers with
 * names starting org.hyperledger
 *
 * Control of this is via the environment variables
 * 'CORE_CHAINCODE_LOGGING_LEVEL' this takes a string that matches the following
 * Java.util.logging levels (case insensitive)
 *
 * CRITICAL, ERROR == Level.SEVERE, WARNING == Level.WARNING, INFO == Level.INFO
 * NOTICE == Level.CONFIG, DEBUG == Level.FINEST
 *
 */
public final class Logging {

    /**
     * Name of the Performance logger.
     */
    public static final String PERFLOGGER = "org.hyperledger.Performance";

    /** Private Constructor.
     *
     */
    private Logging() {

    }

    /**
     * Formats a Throwable to a string with details of all the causes.
     *
     * @param throwable Exception
     * @return String formatted with all the details
     */
    public static String formatError(final Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        final StringWriter buffer = new StringWriter();
        buffer.append(throwable.getMessage()).append(System.lineSeparator());

        throwable.printStackTrace(new PrintWriter(buffer));

        final Throwable cause = throwable.getCause();
        if (cause != null) {
            buffer.append(".. caused by ..").append(System.lineSeparator());
            buffer.append(Logging.formatError(cause));
        }

        return buffer.toString();

    }

    /**
     * Sets the log level to the the.
     *
     * @param newLevel the new logging level
     */
    public static void setLogLevel(final String newLevel) {

        final Level l = mapLevel(newLevel);
        final LogManager logManager = LogManager.getLogManager();
        // slightly cumbersome approach - but the loggers don't have a 'get children'
        // so find those that have the correct stem.
        final ArrayList<String> allLoggers = Collections.list(logManager.getLoggerNames());
        allLoggers.add("org.hyperledger");
        allLoggers.stream().filter(name -> name.startsWith("org.hyperledger")).map(name -> logManager.getLogger(name)).forEach(logger -> {
            if (logger != null) {
                logger.setLevel(l);
            }
        });
    }

    private static Level mapLevel(final String level) {
        if (level != null) {
            switch (level.toUpperCase().trim()) {
            case "ERROR":
            case "CRITICAL":
                return Level.SEVERE;
            case "WARNING":
                return Level.WARNING;
            case "INFO":
                return Level.INFO;
            case "NOTICE":
                return Level.CONFIG;
            case "DEBUG":
                return Level.FINEST;
            default:
                return Level.INFO;
            }
        }
        return Level.INFO;
    }
}
