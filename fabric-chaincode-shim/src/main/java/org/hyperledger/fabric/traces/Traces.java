/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.traces;

import org.hyperledger.fabric.traces.impl.DefaultTracesProvider;
import org.hyperledger.fabric.traces.impl.NullProvider;

import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Traces Interface.
 *
 * Traces setups up the provider in use from the configuration supplied
 *
 * If not enabled, nothing happens.
 */
public final class Traces {

    private static final String CHAINCODE_TRACES_ENABLED = "CHAINCODE_TRACES_ENABLED";
    private static final String CHAINCODE_TRACES_PROVIDER = "CHAINCODE_TRACES_PROVIDER";

    private static Logger logger = Logger.getLogger(Traces.class.getName());

    private static TracesProvider provider;


    private Traces() {

    }

    /**
     *
     * @param props the configuration of the chaincode
     * @return The traces provider
     */
    public static TracesProvider initialize(final Properties props) {
        if (Boolean.parseBoolean((String) props.get(CHAINCODE_TRACES_ENABLED))) {
            try {
                logger.info("Traces enabled");
                if (props.containsKey(CHAINCODE_TRACES_PROVIDER)) {
                    final String providerClass = (String) props.get(CHAINCODE_TRACES_PROVIDER);

                    @SuppressWarnings("unchecked") // it must be this type otherwise an error
                    final
                    Class<TracesProvider> clazz = (Class<TracesProvider>) Class.forName(providerClass);
                    provider = clazz.getConstructor().newInstance();
                } else {
                    logger.info("Using default traces provider");
                    provider = new DefaultTracesProvider();
                }
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                    | NoSuchMethodException | SecurityException e) {
                throw new RuntimeException("Unable to start traces", e);
            }
        } else {
            // return a 'null' provider
            logger.info("Traces disabled");
            provider = new NullProvider();

        }

        provider.initialize(props);
        return provider;
    }

    /**
     *
     * @return TracesProvider
     */
    public static TracesProvider getProvider() {
        if (provider == null) {
            throw new IllegalStateException("No provider set, this should have been set");
        }
        return provider;
    }
}
