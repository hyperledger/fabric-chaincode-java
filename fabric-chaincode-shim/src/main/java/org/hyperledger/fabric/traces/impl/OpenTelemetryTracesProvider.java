/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.traces.impl;

import io.grpc.ClientInterceptor;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.grpc.v1_6.GrpcTracing;
import io.opentelemetry.sdk.autoconfigure.OpenTelemetrySdkAutoConfiguration;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.traces.TracesProvider;

import java.util.Properties;

public final class OpenTelemetryTracesProvider implements TracesProvider {

    private static final String TX_ID = "transaction_id";
    private static final String CHANNEL_ID = "channel_id";
    private static final String CORE_CHAINCODE_ID_NAME = "CORE_CHAINCODE_ID_NAME";

    private Tracer tracer;
    private GrpcTracing grpcTracer;

    @Override
    public void initialize(final Properties props) {
        String serviceName = props.getProperty(CORE_CHAINCODE_ID_NAME, "unknown");
        props.setProperty(ResourceAttributes.SERVICE_NAME.getKey(), serviceName);

        OpenTelemetry openTelemetry = OpenTelemetrySdkAutoConfiguration.initialize(false,
                new OpenTelemetryProperties(System.getenv(), System.getProperties(), props));
        tracer = openTelemetry.getTracerProvider().get("org.hyperledger.traces");
        grpcTracer = GrpcTracing.newBuilder(openTelemetry).build();
    }

    @Override
    public Span createSpan(final ChaincodeStub stub) {
        Context parentContext = Context.current();
        return tracer.spanBuilder(stub.getFunction())
                .setSpanKind(SpanKind.INTERNAL)
                .setAttribute(TX_ID, stub.getTxId())
                .setAttribute(CHANNEL_ID, stub.getChannelId())
                .setParent(parentContext)
                .startSpan();
    }

    @Override
    public ClientInterceptor createInterceptor() {
        return grpcTracer.newClientInterceptor();
    }
}
