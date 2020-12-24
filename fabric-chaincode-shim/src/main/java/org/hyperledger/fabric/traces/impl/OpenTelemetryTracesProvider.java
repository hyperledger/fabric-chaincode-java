/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.traces.impl;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.extension.trace.propagation.B3Propagator;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.ResourceAttributes;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.traces.TracesProvider;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Properties;

public final class OpenTelemetryTracesProvider implements TracesProvider {

    private static final String TX_ID = "transaction_id";
    private static final String COMBINED_HEADER = "b3";
    private static final String CORE_CHAINCODE_ID_NAME = "CORE_CHAINCODE_ID_NAME";
    static final TextMapPropagator.Getter<ChaincodeStub> PROPAGATOR = new TextMapPropagator.Getter<ChaincodeStub>() {
        @Override
        public Iterable<String> keys(final ChaincodeStub carrier) {
            return Collections.singleton(COMBINED_HEADER);
        }

        @Nullable
        @Override
        public String get(final @Nullable ChaincodeStub carrier, final String key) {
            if (COMBINED_HEADER.equals(key)) {
                return carrier.getB3Header();
            }
            return null;
        }
    };

    private Tracer tracer;

    @Override
    public void initialize(final Properties props) {
        String serviceName = props.getProperty(CORE_CHAINCODE_ID_NAME, "unknown");
        BatchSpanProcessor spanProcessor =
                BatchSpanProcessor.builder(
                        OtlpGrpcSpanExporter.builder()
                                .readProperties(props)
                                .readSystemProperties()
                                .readEnvironmentVariables()
                                .build())
                        .build();
        Resource resource =
                Resource.getDefault()
                        .merge(
                                Resource.create(
                                        Attributes.builder().put(ResourceAttributes.SERVICE_NAME, serviceName).build()));
        SdkTracerProvider provider = SdkTracerProvider.builder().setResource(resource).build();
        provider.addSpanProcessor(spanProcessor);
        tracer = provider.get("org.hyperledger.traces");
    }

    @Override
    public Span createSpan(final ChaincodeStub stub) {
        Context parentContext = recreateParentContext(stub);
        return tracer.spanBuilder(stub.getFunction()).setSpanKind(Span.Kind.SERVER).setAttribute(TX_ID, stub.getTxId()).setParent(parentContext).startSpan();
    }

    private Context recreateParentContext(final ChaincodeStub stub) {
        return B3Propagator.getInstance().extract(Context.current(), stub, PROPAGATOR);
    }
}
