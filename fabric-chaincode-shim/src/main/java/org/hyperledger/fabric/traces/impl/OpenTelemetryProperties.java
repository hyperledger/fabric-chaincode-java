/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.traces.impl;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

final class OpenTelemetryProperties implements ConfigProperties {
    private final Map<String, String> config;

    OpenTelemetryProperties(final Map... arrayOfProperties) {
        Map<String, String> config = new HashMap<>();
        for (Map props : arrayOfProperties) {
            props.forEach((key, value) ->
                    config.put(((String) key).toLowerCase(Locale.ROOT).replace('-', '.'), (String) value));
        }
        this.config = config;
    }

    @Override
    @Nullable
    public String getString(final String name) {
        return config.get(name);
    }

    @Override
    @Nullable
    public Boolean getBoolean(final String name) {
        String value = config.get(name);
        if (value == null || value.isEmpty()) {
            return null;
        }
        return Boolean.parseBoolean(value);
    }

    @Override
    @Nullable
    @SuppressWarnings("UnusedException")
    public Integer getInt(final String name) {
        String value = config.get(name);
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw newInvalidPropertyException(name, value, "integer");
        }
    }

    @Override
    @Nullable
    @SuppressWarnings("UnusedException")
    public Long getLong(final String name) {
        String value = config.get(name);
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            throw newInvalidPropertyException(name, value, "long");
        }
    }

    @Override
    @Nullable
    @SuppressWarnings("UnusedException")
    public Double getDouble(final String name) {
        String value = config.get(name);
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            throw newInvalidPropertyException(name, value, "double");
        }
    }

    @Override
    @Nullable
    @SuppressWarnings("UnusedException")
    public Duration getDuration(final String name) {
        String value = config.get(name);
        if (value == null || value.isEmpty()) {
            return null;
        }
        String unitString = getUnitString(value);
        String numberString = value.substring(0, value.length() - unitString.length());
        try {
            long rawNumber = Long.parseLong(numberString.trim());
            TimeUnit unit = getDurationUnit(unitString.trim());
            return Duration.ofMillis(TimeUnit.MILLISECONDS.convert(rawNumber, unit));
        } catch (NumberFormatException ex) {
            throw new ConfigurationException(
                    "Invalid duration property "
                            + name
                            + "="
                            + value
                            + ". Expected number, found: "
                            + numberString);
        } catch (ConfigurationException ex) {
            throw new ConfigurationException(
                    "Invalid duration property " + name + "=" + value + ". " + ex.getMessage());
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
    public Map<String, String> getMap(final String name) {
        return getList(name).stream()
                .map(keyValuePair -> filterBlanksAndNulls(keyValuePair.split("=", 2)))
                .map(
                        splitKeyValuePairs -> {
                            if (splitKeyValuePairs.size() != 2) {
                                throw new ConfigurationException(
                                        "Invalid map property: " + name + "=" + config.get(name));
                            }
                            return new AbstractMap.SimpleImmutableEntry<>(
                                    splitKeyValuePairs.get(0), splitKeyValuePairs.get(1));
                        })
                // If duplicate keys, prioritize later ones similar to duplicate system properties on a
                // Java command line.
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey, Map.Entry::getValue, (first, next) -> next, LinkedHashMap::new));
    }

    private static ConfigurationException newInvalidPropertyException(
            final String name, final String value, final String type) {
        throw new ConfigurationException(
                "Invalid value for property " + name + "=" + value + ". Must be a " + type + ".");
    }

    private static List<String> filterBlanksAndNulls(final String[] values) {
        return Arrays.stream(values)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Returns the TimeUnit associated with a unit string. Defaults to milliseconds.
     * @param unitString the time unit as a string
     * @return the parsed TimeUnit
     */
    private static TimeUnit getDurationUnit(final String unitString) {
        switch (unitString) {
            case "": // Fallthrough expected
            case "ms":
                return TimeUnit.MILLISECONDS;
            case "s":
                return TimeUnit.SECONDS;
            case "m":
                return TimeUnit.MINUTES;
            case "h":
                return TimeUnit.HOURS;
            case "d":
                return TimeUnit.DAYS;
            default:
                throw new ConfigurationException("Invalid duration string, found: " + unitString);
        }
    }

    /**
     * Fragments the 'units' portion of a config value from the 'value' portion.
     *
     * <p>E.g. "1ms" would return the string "ms".
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
