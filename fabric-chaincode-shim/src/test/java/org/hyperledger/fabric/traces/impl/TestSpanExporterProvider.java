/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.traces.impl;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.traces.ConfigurableSpanExporterProvider;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class TestSpanExporterProvider implements ConfigurableSpanExporterProvider {

    public static final List<SpanData> SPANS = new ArrayList<>();
    public static final SpanExporter EXPORTER = new SpanExporter() {

        @Override
        public CompletableResultCode export(final Collection<SpanData> spans) {
            TestSpanExporterProvider.SPANS.addAll(spans);
            return CompletableResultCode.ofSuccess();
        }

        @Override
        public CompletableResultCode flush() {
            return CompletableResultCode.ofSuccess();
        }

        @Override
        public CompletableResultCode shutdown() {
            return CompletableResultCode.ofSuccess();
        }
    };

    @Override
    public SpanExporter createExporter(final ConfigProperties config) {
        return EXPORTER;
    }

    @Override
    public String getName() {
        return "TestSpanExporterProvider";
    }
}
