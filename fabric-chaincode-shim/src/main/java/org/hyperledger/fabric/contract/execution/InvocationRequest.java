/*
 * Copyright 2019 IBM DTCC All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.contract.execution;

import java.util.List;

/**
 * Invocation Request.
 *
 * All information needed to find
 * {@link org.hyperledger.fabric.contract.annotation.Contract} and invoke the
 * request.
 */
public interface InvocationRequest {
    /**
     *
     */
    String DEFAULT_NAMESPACE = "default";

    /**
     * @return Namespace
     */
    String getNamespace();

    /**
     * @return Method
     */
    String getMethod();

    /**
     * @return Args as byte array
     */
    List<byte[]> getArgs();

    /**
     * @return Request
     */
    String getRequestName();
}
