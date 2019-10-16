/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

/**
 * Logger class to use throughout the Contract Implementation
 *
 */
public class Logging {

    public static final String PERFLOGGER = "org.hyperledger.Performance";

    public static String formatError(final Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        final StringWriter buffer = new StringWriter();
        buffer.append(throwable.getMessage());
        throwable.printStackTrace(new PrintWriter(buffer));

        final Throwable cause = throwable.getCause();
        if (cause != null) {
            buffer.append(".. caused by ..");
            buffer.append(Logging.formatError(cause));
        }

        return buffer.toString();

    }
}
