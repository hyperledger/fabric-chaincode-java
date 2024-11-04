/*
 * Copyright 2019 IBM DTCC All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.contract.execution.impl;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.fabric.contract.execution.InvocationRequest;
import org.hyperledger.fabric.shim.ChaincodeStub;

public final class ContractInvocationRequest implements InvocationRequest {
    @SuppressWarnings("PMD.ProperLogger") // PMD 7.7.0 gives a false positive here
    private static final Log LOGGER = LogFactory.getLog(ContractInvocationRequest.class);

    private static final Pattern NS_REGEX = Pattern.compile(":");

    private final String namespace;
    private final String method;
    private final List<byte[]> args;

    /** @param context */
    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    public ContractInvocationRequest(final ChaincodeStub context) {
        List<byte[]> funcAndArgs = context.getArgs();
        if (funcAndArgs.isEmpty()) {
            throw new IllegalArgumentException("Missing function name");
        }

        final String func = new String(funcAndArgs.get(0), StandardCharsets.UTF_8);
        LOGGER.debug(func);

        final String[] funcParts = NS_REGEX.split(func);
        if (funcParts.length == 2) {
            namespace = funcParts[0];
            method = funcParts[1];
        } else {
            namespace = DEFAULT_NAMESPACE;
            method = funcParts[0];
        }

        args = funcAndArgs.subList(1, funcAndArgs.size());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(namespace + " " + method + " " + args);
        }
    }

    /** */
    @Override
    public String getNamespace() {
        return namespace;
    }

    /** */
    @Override
    public String getMethod() {
        return method;
    }

    /** */
    @Override
    public List<byte[]> getArgs() {
        return args;
    }

    /** */
    @Override
    public String getRequestName() {
        return namespace + ":" + method;
    }

    /** */
    @Override
    public String toString() {
        return namespace + ":" + method + " @" + Integer.toHexString(System.identityHashCode(this));
    }
}
