/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.traces.impl;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import org.junit.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OpenTelemetryPropertiesTest {

    @Test
    public void testOverrideValue() {
        OpenTelemetryProperties props = new OpenTelemetryProperties(Collections.singletonMap("foo", "bar"), Collections.singletonMap("foo", "foobar"));
        assertThat(props.getString("foo")).isEqualTo("foobar");
    }

    @Test
    public void testCanGetDurationDays() {
        OpenTelemetryProperties props = new OpenTelemetryProperties(Collections.singletonMap("foo", "5d"));
        assertThat(props.getDuration("foo")).isEqualTo(Duration.of(5, DAYS));
    }

    @Test
    public void testCanGetDurationHours() {
        OpenTelemetryProperties props = new OpenTelemetryProperties(Collections.singletonMap("foo", "5h"));
        assertThat(props.getDuration("foo")).isEqualTo(Duration.of(5, HOURS));
    }


    @Test
    public void testCanGetDurationMinutes() {
        OpenTelemetryProperties props = new OpenTelemetryProperties(Collections.singletonMap("foo", "5m"));
        assertThat(props.getDuration("foo")).isEqualTo(Duration.of(5, MINUTES));
    }

    @Test
    public void testCanGetDurationSeconds() {
        OpenTelemetryProperties props = new OpenTelemetryProperties(Collections.singletonMap("foo", "5s"));
        assertThat(props.getDuration("foo")).isEqualTo(Duration.of(5, SECONDS));
    }

    @Test
    public void testCanGetDurationMilliSeconds() {
        OpenTelemetryProperties props = new OpenTelemetryProperties(Collections.singletonMap("foo", "5ms"));
        assertThat(props.getDuration("foo")).isEqualTo(Duration.of(5, MILLIS));
    }

    @Test
    public void testCanGetDurationInvalid() {
        OpenTelemetryProperties props = new OpenTelemetryProperties(Collections.singletonMap("foo", "5foo"));
        assertThatThrownBy(() -> props.getDuration("foo")).isInstanceOf(ConfigurationException.class);
    }

    @Test
    public void testGetDouble() {
        OpenTelemetryProperties props = new OpenTelemetryProperties(Collections.singletonMap("foo", "5.23"));
        assertThat(props.getDouble("foo")).isEqualTo(5.23d);
        assertThat(props.getDouble("bar")).isNull();
    }

    @Test
    public void testGetDoubleInvalid() {
        OpenTelemetryProperties props = new OpenTelemetryProperties(Collections.singletonMap("foo", "5foo"));
        assertThatThrownBy(() -> props.getDouble("foo")).isInstanceOf(ConfigurationException.class);
    }

    @Test
    public void testGetLong() {
        OpenTelemetryProperties props = new OpenTelemetryProperties(Collections.singletonMap("foo", "500003"));
        assertThat(props.getLong("foo")).isEqualTo(500003L);
        assertThat(props.getLong("bar")).isNull();
    }


    @Test
    public void testGetInt() {
        OpenTelemetryProperties props = new OpenTelemetryProperties(Collections.singletonMap("foo", "500003"));
        assertThat(props.getInt("foo")).isEqualTo(500003);
        assertThat(props.getInt("bar")).isNull();
    }

    @Test
    public void testGetBoolean() {
        OpenTelemetryProperties props = new OpenTelemetryProperties(Collections.singletonMap("foo", "true"));
        assertThat(props.getBoolean("foo")).isTrue();
        assertThat(props.getBoolean("bar")).isNull();
    }

    @Test
    public void testGetList() {
        OpenTelemetryProperties props = new OpenTelemetryProperties(Collections.singletonMap("foo", "foo,bar,foobar"));
        assertThat(props.getList("foo")).isEqualTo(Arrays.asList("foo", "bar", "foobar"));
        assertThat(props.getList("bar")).isEqualTo(Collections.emptyList());
    }

    @Test
    public void testGetMap() {
        OpenTelemetryProperties props = new OpenTelemetryProperties(Collections.singletonMap("foo", "foo=bar,foobar=noes"));
        Map<String, String> expected = new HashMap<>();
        expected.put("foo", "bar");
        expected.put("foobar", "noes");
        assertThat(props.getMap("foo")).isEqualTo(expected);
    }

    @Test
    public void testGetMapInvalid() {
        OpenTelemetryProperties props = new OpenTelemetryProperties(Collections.singletonMap("foo", "foo/bar,foobar/noes"));
        Map<String, String> expected = new HashMap<>();
        assertThatThrownBy(() -> props.getMap("foo")).isInstanceOf(ConfigurationException.class);
    }
}
