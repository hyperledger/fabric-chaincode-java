/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.traces;

import io.grpc.ClientInterceptor;
import io.opentelemetry.api.trace.Span;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.util.Properties;

/**
 * Interface to be implemented to send traces on the chaincode to the
 * 'backend-of-choice'.
 *
 * An instance of this will be created, and provided with the resources from
 * which chaincode specific metrics can be collected. (via the no-argument
 * constructor).
 *
 * The choice of when, where and what to collect etc are within the remit of the
 * provider.
 *
 * This is the effective call sequence.
 *
 * MyTracesProvider mmp = new MyTracesProvider()
 * mmp.initialize(props_from_environment); // short while later....
 * mmp.setTaskTracesCollector(taskService);
 */
public interface TracesProvider {

    /**
     * Initialize method that is called immediately after creation.
     *
     * @param props
     */
    default void initialize(final Properties props) {
    };

    /**
     * Creates a span with metadata of the current chaincode execution, possibly linked to the execution arguments.
     * @param stub the context of the chaincode execution
     * @return a new span if traces are enabled, or null.
     * The caller is responsible for closing explicitly the span.
     */
    default Span createSpan(ChaincodeStub stub) {
        return null;
    }

    /**
     * Creates an interceptor of gRPC messages that can be injected in processing incoming messages to extract
     * trace information.
     * @return a new client interceptor, or null if no interceptor is set.
     */
    default ClientInterceptor createInterceptor() {
        return null;
    }
}
