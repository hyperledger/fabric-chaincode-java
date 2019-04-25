/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric;

import org.hyperledger.fabric.contract.ContractRuntimeException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class LoggerTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();


    @Test
    public void logger() {
    	Logger.getLogger(LoggerTest.class);
    	Logger.getLogger(LoggerTest.class.getName());
    }

    @Test
    public void testContractException() {
    	Logger logger = Logger.getLogger(LoggerTest.class);

    	ContractRuntimeException cre1 = new ContractRuntimeException("");
    	logger.formatError(cre1);

    	ContractRuntimeException cre2 = new ContractRuntimeException("",cre1);
    	ContractRuntimeException cre3 = new ContractRuntimeException("",cre2);
    	logger.formatError(cre3);

    	logger.error("all gone wrong");
    	logger.error(()->"all gone wrong");

    }
}
