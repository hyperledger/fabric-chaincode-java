/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.traces.impl;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import java.time.Duration;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

final class OpenTelemetryProperties implements ConfigProperties {
    private final Map<String, String> config;

    @SafeVarargs
    OpenTelemetryProperties(final Map<String, String>... arrayOfProperties) {
        Map<String, String> config = new HashMap<>();
        for (Map<String, String> props : arrayOfProperties) {
            props.forEach(
                    (key, value) -> config.put(key.toLowerCase(Locale.ROOT).replace('-', '.'), value));
        }
        this.config = config;
    }

    @Override
    @Nullable public String getString(final String name) {
        return config.get(name);
    }

    @Override
    @Nullable public Boolean getBoolean(final String name) {
        String value = config.get(name);
        if (value == null || value.isEmpty()) {
            return null;
        }
        return Boolean.valueOf(value);
    }

    @Override
    @Nullable public Integer getInt(final String name) {
        String value = config.get(name);
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            throw newInvalidPropertyException(name, value, "integer", ex);
        }
    }

    @Override
    @Nullable public Long getLong(final String name) {
        String value = config.get(name);
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            throw newInvalidPropertyException(name, value, "long", ex);
        }
    }

    @Override
    @Nullable public Double getDouble(final String name) {
        String value = config.get(name);
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Double.valueOf(value);
        } catch (NumberFormatException ex) {
            throw newInvalidPropertyException(name, value, "double", ex);
        }
    }

    @Override
    @Nullable public Duration getDuration(final String name) {
        String value = config.get(name);
        if (value == null || value.isEmpty()) {
            return null;
        }
        String unitString = getUnitString(value);
        String numberString = value.substring(0, value.length() - unitString.length());
        try {
            long rawNumber = Long.parseLong(numberString.trim());
            TimeUnit unit = getDurationUnit(unitString.trim())
                    .orElseThrow(() -> new ConfigurationException(
                            "Invalid duration property " + name + "=" + value + ". Invalid duration unit."));
            return Duration.ofMillis(TimeUnit.MILLISECONDS.convert(rawNumber, unit));
        } catch (NumberFormatException ex) {
            var e = new ConfigurationException(
                    "Invalid duration property " + name + "=" + value + ". Expected number, found: " + numberString);
            e.addSuppressed(ex);
            throw e;
        }
    }

    @Override
    public List<String> getList(final String name) {
        String value = config.get(name);
        if (value == null) {
            return Collections.emptyList();
        }
        return filterBlanksAndNulls(value.split(","));
    }

    @Override
    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    public Map<String, String> getMap(final String name) {
        return getList(name).stream()
                .map(keyValuePair -> filterBlanksAndNulls(keyValuePair.split("=", 2)))
                .map(splitKeyValuePairs -> {
                    if (splitKeyValuePairs.size() != 2) {
                        throw new ConfigurationException("Invalid map property: " + name + "=" + config.get(name));
                    }
                    return new AbstractMap.SimpleImmutableEntry<>(splitKeyValuePairs.get(0), splitKeyValuePairs.get(1));
                })
                // If duplicate keys, prioritize later ones similar to duplicate system properties on a
                // Java command line.
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue, (first, next) -> next, LinkedHashMap::new));
    }

    private static ConfigurationException newInvalidPropertyException(
            final String name, final String value, final String type, final Exception cause) {
        var e = new ConfigurationException(
                "Invalid value for property " + name + "=" + value + ". Must be a " + type + ".");
        e.addSuppressed(cause);
        throw e;
    }

    private static List<String> filterBlanksAndNulls(final String[] values) {
        return Arrays.stream(values).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
    }

    /**
     * Returns the TimeUnit associated with a unit string. Defaults to milliseconds.
     *
     * @param unitString the time unit as a string
     * @return the parsed TimeUnit
     */
    private static Optional<TimeUnit> getDurationUnit(final String unitString) {
        switch (unitString) {
            case "": // Fallthrough expected
            case "ms":
                return Optional.of(TimeUnit.MILLISECONDS);
            case "s":
                return Optional.of(TimeUnit.SECONDS);
            case "m":
                return Optional.of(TimeUnit.MINUTES);
            case "h":
                return Optional.of(TimeUnit.HOURS);
            case "d":
                return Optional.of(TimeUnit.DAYS);
            default:
                return Optional.empty();
        }
    }

    /**
     * Fragments the 'units' portion of a config value from the 'value' portion.
     *
     * <p>E.g. "1ms" would return the string "ms".
     *
     * @param rawValue the raw value of a unit and value
     * @return the unit string
     */
    private static String getUnitString(final String rawValue) {
        int lastDigitIndex = rawValue.length() - 1;
        while (lastDigitIndex >= 0) {
            char c = rawValue.charAt(lastDigitIndex);
            if (Character.isDigit(c)) {
                break;
            }
            lastDigitIndex -= 1;
        }
        // Pull everything after the last digit.
        return rawValue.substring(lastDigitIndex + 1);
    }
}
