/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.LogManager;

/**
 * Logger class to use throughout the Contract Implementation.
 *
 */
public class Logger extends java.util.logging.Logger {

    protected Logger(final String name) {
        super(name, null);

        // ensure that the parent logger is set
        this.setParent(java.util.logging.Logger.getLogger("org.hyperledger.fabric"));
    }

    /**
     * @param name
     * @return Logger
     */
    public static Logger getLogger(final String name) {
        return new Logger(name);
    }

    /**
     * @param msgSupplier
     */
    public void debug(final Supplier<String> msgSupplier) {
        log(Level.FINEST, msgSupplier);
    }

    /**
     * @param msg
     */
    public void debug(final String msg) {
        log(Level.FINEST, msg);
    }

    /**
     * @param class1
     * @return Logger
     */
    public static Logger getLogger(final Class<?> class1) {
        // important to add the logger to the log manager
        final Logger l = Logger.getLogger(class1.getName());
        LogManager.getLogManager().addLogger(l);
        return l;
    }

    /**
     * @param message
     */
    public void error(final String message) {
        log(Level.SEVERE, message);
    }

    /**
     * @param msgSupplier
     */
    public void error(final Supplier<String> msgSupplier) {
        log(Level.SEVERE, msgSupplier);
    }

    /**
     * @param throwable
     * @return Throwable
     */
    public String formatError(final Throwable throwable) {
        if (throwable == null) {
            return null;
        }

        final StringWriter buffer = new StringWriter();
        buffer.append(throwable.getMessage());
        throwable.printStackTrace(new PrintWriter(buffer));

        final Throwable cause = throwable.getCause();
        if (cause != null) {
            buffer.append(".. caused by ..");
            buffer.append(this.formatError(cause));
        }

        return buffer.toString();
    }

}
