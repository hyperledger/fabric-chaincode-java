/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.metrics;

import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.logging.Logger;

import org.hyperledger.fabric.metrics.impl.DefaultProvider;
import org.hyperledger.fabric.metrics.impl.NullProvider;

/**
 * Metrics Interface.
 *
 * Metrics setups up the provider in use from the configuration supplied If not
 * enabled, nothing happens, but if enabled but no specific logger default is
 * used that uses the org.hyperledger.Performance logger
 */
public final class Metrics {

    private static final String CHAINCODE_METRICS_ENABLED = "CHAINCODE_METRICS_ENABLED";
    private static final String CHAINCODE_METRICS_PROVIDER = "CHAINCODE_METRICS_PROVIDER";

    private static Logger logger = Logger.getLogger(Metrics.class.getName());

    private static MetricsProvider provider;


    private Metrics() {

    }

    /**
     *
     * @param props
     * @return The metrics provide
     */
    public static MetricsProvider initialize(final Properties props) {
        if (Boolean.parseBoolean((String) props.get(CHAINCODE_METRICS_ENABLED))) {
            try {
                logger.info("Metrics enabled");
                if (props.containsKey(CHAINCODE_METRICS_PROVIDER)) {
                    final String providerClass = (String) props.get(CHAINCODE_METRICS_PROVIDER);

                    @SuppressWarnings("unchecked") // it must be this type otherwise an error
                    final
                    Class<MetricsProvider> clazz = (Class<MetricsProvider>) Class.forName(providerClass);
                    provider = clazz.getConstructor().newInstance();
                } else {
                    logger.info("Using default metrics provider (logs to org.hyperledger.Performance)");
                    provider = new DefaultProvider();
                }
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                    | NoSuchMethodException | SecurityException e) {
                throw new RuntimeException("Unable to start metrics", e);
            }
        } else {
            // return a 'null' provider
            logger.info("Metrics disabled");
            provider = new NullProvider();

        }

        provider.initialize(props);
        return provider;
    }

    /**
     *
     * @return MetricsProvider
     */
    public static MetricsProvider getProvider() {
        if (provider == null) {
            throw new IllegalStateException("No provider set, this should have been set");
        }
        return provider;
    }

}
