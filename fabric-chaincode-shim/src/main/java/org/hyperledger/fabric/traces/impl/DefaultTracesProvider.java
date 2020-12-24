/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.traces.impl;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.traces.TracesProvider;

public final class DefaultTracesProvider implements TracesProvider {

    @Override
    public Span createSpan(final ChaincodeStub stub) {
        return Tracer.getDefault().spanBuilder(stub.getFunction()).startSpan();
    }
}
