/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.traces.impl;

import io.grpc.ClientInterceptor;
import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.extension.trace.propagation.B3Propagator;
import io.opentelemetry.extension.trace.propagation.JaegerPropagator;
import io.opentelemetry.instrumentation.grpc.v1_5.client.TracingClientInterceptor;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.ResourceAttributes;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.traces.TracesProvider;

import java.util.Properties;

public final class OpenTelemetryTracesProvider implements TracesProvider {

    private static final String TX_ID = "transaction_id";
    private static final String CHANNEL_ID = "channel_id";
    private static final String CORE_CHAINCODE_ID_NAME = "CORE_CHAINCODE_ID_NAME";

    private Tracer tracer;

    @Override
    public void initialize(final Properties props) {
        ContextPropagators propagators = ContextPropagators.create(
                TextMapPropagator.composite(
                        W3CTraceContextPropagator.getInstance(),
                        W3CBaggagePropagator.getInstance(),
                        B3Propagator.getInstance(),
                        JaegerPropagator.getInstance()
                ));


        String serviceName = props.getProperty(CORE_CHAINCODE_ID_NAME, "unknown");
        BatchSpanProcessor spanProcessor =
                BatchSpanProcessor.builder(
                        OtlpGrpcSpanExporter.builder()
                                .readProperties(props)
                                .readSystemProperties()
                                .readEnvironmentVariables()
                                .build()).readProperties(props).readSystemProperties().readEnvironmentVariables()
                        .build();
        Resource resource =
                Resource.getDefault()
                        .merge(
                                Resource.create(
                                        Attributes.builder().put(ResourceAttributes.SERVICE_NAME, serviceName).build()));
        SdkTracerProvider provider = SdkTracerProvider.builder().setResource(resource).addSpanProcessor(spanProcessor).build();
        OpenTelemetrySdk.builder().setPropagators(propagators).setTracerProvider(provider).buildAndRegisterGlobal();
        tracer = provider.get("org.hyperledger.traces");
    }

    @Override
    public Span createSpan(final ChaincodeStub stub) {
        Context parentContext = Context.current();
        return tracer.spanBuilder(stub.getFunction())
                .setSpanKind(Span.Kind.INTERNAL)
                .setAttribute(TX_ID, stub.getTxId())
                .setAttribute(CHANNEL_ID, stub.getChannelId())
                .setParent(parentContext)
                .startSpan();
    }

    @Override
    public ClientInterceptor createInterceptor() {
        return TracingClientInterceptor.newInterceptor(tracer);
    }
}
