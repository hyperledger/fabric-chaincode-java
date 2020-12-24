/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

/**
 * <p>
 * Supports collection of traces
 * <p>
 * This creates traces at the root level of chaincode calls.
 *
 *
 * To enable traces ensure that there is a standard format Java properties file
 * called `config.props` in the root of your contract code. For example this
 * path
 *
 * <pre>
 * myjava - contract - project / java / src / main / resources / config.props
 * </pre>
 *
 * This should contain the following
 *
 * <pre>
 * CHAINCODE_TRACES_ENABLED=true
 * </pre>
 *
 * The traces enabled flag will turn on default traces logging. (it's off by
 * default).
 *
 * If no file is supplied traces are not enabled, the values shown for the
 * thread pool are used.
 *
 * <p>Open Telemetry</p>
 *
 * To use Open Telemetry, set the following properties:
 *
 * <pre>
 * CHAINCODE_TRACES_ENABLED=true
 * CHAINCODE_TRACES_PROVIDER=org.hyperledger.fabric.traces.impl.OpenTelemetryTracesProvider
 * </pre>
 *
 * Additionally, you can set properties after the specification:
 * https://github.com/open-telemetry/opentelemetry-specification/blob/master/specification/sdk-environment-variables.md
 *
 * Example:
 * <pre>
 * OTEL_EXPORTER_OTLP_ENDPOINT=otelcollector:4317
 * OTEL_EXPORTER_OTLP_INSECURE=true
 * </pre>
 */
package org.hyperledger.fabric.traces;
