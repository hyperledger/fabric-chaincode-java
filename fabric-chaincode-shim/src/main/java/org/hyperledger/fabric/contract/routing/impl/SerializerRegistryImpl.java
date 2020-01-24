/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.contract.routing.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hyperledger.fabric.Logger;
import org.hyperledger.fabric.contract.annotation.Serializer;
import org.hyperledger.fabric.contract.execution.SerializerInterface;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;

/**
 * Registry to hold permit access to the serializer implementations.
 *
 * It holds the serializers that have been defined. JSONTransactionSerializer
 * is the default.
 */
public class SerializerRegistryImpl {
    private static Logger logger = Logger.getLogger(SerializerRegistryImpl.class);

    private final Class<Serializer> annotationClass = Serializer.class;

    /**
     *
     */
    public SerializerRegistryImpl() {
    }

    // Could index these by name and or type.
    private final Map<String, SerializerInterface> contents = new HashMap<>();

    /**
     * Get a Serializer for the matching fully qualified classname, and the Target.
     *
     * @param name   fully qualified classname
     * @param target the intended target of the serializer
     * @return Serializer instance
     */
    public SerializerInterface getSerializer(final String name, final Serializer.TARGET target) {
        final String key = name + ":" + target;
        return contents.get(key);
    }

    private SerializerInterface add(final String name, final Serializer.TARGET target, final Class<SerializerInterface> clazz) {
        logger.debug(() -> "Adding new Class " + clazz.getCanonicalName() + " for " + target);
        try {
            final String key = name + ":" + target;
            final SerializerInterface newObj = clazz.newInstance();
            this.contents.put(key, newObj);

            return newObj;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Find all the serializers that have been defined.
     *
     * @see org.hyperledger.fabric.contract.routing.RoutingRegistry#findAndSetContracts()
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public void findAndSetContents() throws InstantiationException, IllegalAccessException {

        final ClassGraph classGraph = new ClassGraph().enableClassInfo().enableAnnotationInfo();

        // set to ensure that we don't scan the same class twice
        final Set<String> seenClass = new HashSet<>();

        try (ScanResult scanResult = classGraph.scan()) {
            for (final ClassInfo classInfo : scanResult.getClassesWithAnnotation(this.annotationClass.getCanonicalName())) {
                logger.debug("Found class with contract annotation: " + classInfo.getName());
                try {
                    final Class<SerializerInterface> cls = (Class<SerializerInterface>) classInfo.loadClass();
                    logger.debug("Loaded class");

                    final String className = cls.getCanonicalName();
                    if (!seenClass.contains(className)) {
                        seenClass.add(className);
                        this.add(className, Serializer.TARGET.TRANSACTION, cls);
                    }

                } catch (final IllegalArgumentException e) {
                    logger.debug("Failed to load class: " + e);
                }
            }

        }

    }

}
