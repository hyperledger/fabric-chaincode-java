/*
Copyright IBM Corp., DTCC All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.contract.execution.impl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.execution.InvocationRequest;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ContractInvocationRequest implements InvocationRequest {
    String namespace;
    String method;
    List<byte[]> args = Collections.emptyList();

    private static Log logger = LogFactory.getLog(ContractInvocationRequest.class);
    public ContractInvocationRequest(ChaincodeStub context) {
        String func = context.getStringArgs().size() > 0 ? context.getStringArgs().get(0) : null;
        String funcParts[] = func.split(":");
        logger.debug(func);
        if (funcParts.length == 2) {
            namespace = funcParts[0];
            method = funcParts[1];
        } else {
            namespace = DEFAULT_NAMESPACE;
            method = funcParts[0];
        }

        args = context.getArgs().stream().skip(1).collect(Collectors.toList());
        logger.debug(namespace+" "+method+" "+args);
    }

    @Override
    public String getNamespace() {
        return namespace;
    }



    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public List<byte[]> getArgs() {
        return args;
    }

    @Override
    public String getRequestName() {
        return namespace + ":" + method;
    }

    @Override
    public String toString() {
    	return namespace + ":" + method+" @"+Integer.toHexString(System.identityHashCode(this));
    }

}
