/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.LogManager;

/**
 * Logger class to use throughout the Contract Implementation
 *
 * Only available within the 1.4.3 release, this class was a helper class that wrapped
 * the standard java.util.logging.
 * 
 * This being deprecated as it doesn't enough extra function and also
 * log calls made via this can loose their 'caller method info'
 * 
 * For chaincode/contract implementations please use java.util.logging
 * or your own framework. All the Hyperledger Fabric code here is logged
 * in loggers with names starting org.hyperledger
 *  
 * @deprecated
 */
public class Logger extends java.util.logging.Logger {

    protected Logger(String name) {
        super(name, null);

        // ensure that the parent logger is set
        this.setParent(java.util.logging.Logger.getLogger("org.hyperledger.fabric"));
    }

    public static Logger getLogger(String name) {
        return new Logger(name);
    }

    /**
     * @deprecated
     */
    public void debug(Supplier<String> msgSupplier) {
        log(Level.FINEST, msgSupplier);
    }

    /**
     * @deprecated
     */
    public void debug(String msg) {
        log(Level.FINEST, msg);
    }

    public static Logger getLogger(Class<?> class1) {
        // important to add the logger to the log manager
        Logger l = Logger.getLogger(class1.getName());
        LogManager.getLogManager().addLogger(l);
        return l;
    }

    /**
     * @deprecated
     */
    public void error(String message) {
        log(Level.SEVERE, message);
    }

    /**
     * @deprecated
     */
    public void error(Supplier<String> msgSupplier) {
        log(Level.SEVERE, msgSupplier);
    }

    /**
     * @deprecated
     */
    public String formatError(Throwable throwable) {
        if (throwable == null)
            return null;
        final StringWriter buffer = new StringWriter();
        buffer.append(throwable.getMessage());
        throwable.printStackTrace(new PrintWriter(buffer));

        Throwable cause = throwable.getCause();
        if (cause != null) {
            buffer.append(".. caused by ..");
            buffer.append(this.formatError(cause));
        }

        return buffer.toString();
    }

}