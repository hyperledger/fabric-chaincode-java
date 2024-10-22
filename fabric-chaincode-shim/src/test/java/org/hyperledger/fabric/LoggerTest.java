/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric;

import org.hyperledger.fabric.contract.ContractRuntimeException;
import org.junit.jupiter.api.Test;

public class LoggerTest {
    @Test
    public void logger() {
        Logger.getLogger(LoggerTest.class);
        Logger.getLogger(LoggerTest.class.getName());
    }

    @Test
    public void testContractException() {
        final Logger logger = Logger.getLogger(LoggerTest.class);

        final ContractRuntimeException cre1 = new ContractRuntimeException("");
        logger.formatError(cre1);

        final ContractRuntimeException cre2 = new ContractRuntimeException("", cre1);
        final ContractRuntimeException cre3 = new ContractRuntimeException("", cre2);
        logger.formatError(cre3);

        logger.error("all gone wrong");
        logger.error(() -> "all gone wrong");
    }

    @Test
    public void testDebug() {
        Logger.getLogger(LoggerTest.class).debug("debug message");
    }
}
