/*
Copyright IBM Corp., DTCC All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.contract.execution;

import java.util.List;

/**
 * All information needed to find {@link org.hyperledger.fabric.contract.annotation.Contract} and invoke the request
 */
public interface InvocationRequest {
    String DEFAULT_NAMESPACE = "default";

    String getNamespace();
    String getMethod();
    List<byte[]> getArgs();
    String getRequestName();
}
