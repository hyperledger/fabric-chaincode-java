/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Logger class to use throughout the Contract Implementation
 *
 */
public class Logging {

	public static Logger getLogger(Class<?> class1) {
		// important to add the logger to the log manager
		java.util.logging.Logger l = java.util.logging.Logger.getLogger(class1.getName());
		return l;
	}

	@Deprecated
	public static Logger getLogger(String name) {
		// important to add the logger to the log manager
		Logger l = Logger.getLogger(name);
		return l;
	}

	public static String formatError(Throwable throwable) {
		if (throwable == null)
			return null;
		final StringWriter buffer = new StringWriter();
		buffer.append(throwable.getMessage());
		throwable.printStackTrace(new PrintWriter(buffer));

		Throwable cause = throwable.getCause();
		if (cause != null) {
			buffer.append(".. caused by ..");
			buffer.append(Logging.formatError(cause));
		}

		return buffer.toString();

	}
}
