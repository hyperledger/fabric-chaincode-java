/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.contract.routing.impl;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.hyperledger.fabric.Logger;
import org.hyperledger.fabric.contract.annotation.Serializer;
import org.hyperledger.fabric.contract.execution.SerializerInterface;

/**
 * Registry to hold permit access to the serializer implementations.
 *
 * <p>It holds the serializers that have been defined. JSONTransactionSerializer is the default.
 */
public class SerializerRegistryImpl {
    private static final Logger LOGGER = Logger.getLogger(SerializerRegistryImpl.class);

    private static final Class<Serializer> ANNOTATION_CLASS = Serializer.class;

    // Could index these by name and or type.
    private final Map<String, SerializerInterface> contents = new HashMap<>();

    /**
     * Get a Serializer for the matching fully qualified classname, and the Target.
     *
     * @param name fully qualified classname
     * @param target the intended target of the serializer
     * @return Serializer instance
     */
    public SerializerInterface getSerializer(final String name, final Serializer.TARGET target) {
        final String key = name + ":" + target;
        return contents.get(key);
    }

    private SerializerInterface add(
            final String name, final Serializer.TARGET target, final Class<SerializerInterface> clazz)
            throws InstantiationException, IllegalAccessException {
        LOGGER.debug(() -> "Adding new Class " + clazz.getCanonicalName() + " for " + target);
        final String key = name + ":" + target;
        final SerializerInterface newObj = clazz.newInstance();
        this.contents.put(key, newObj);

        return newObj;
    }

    /**
     * Find all the serializers that have been defined.
     *
     * @see org.hyperledger.fabric.contract.routing.RoutingRegistry#findAndSetContracts(TypeRegistry)
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public void findAndSetContents() throws InstantiationException, IllegalAccessException {

        final ClassGraph classGraph = new ClassGraph().enableClassInfo().enableAnnotationInfo();

        // set to ensure that we don't scan the same class twice
        final Set<String> seenClass = new HashSet<>();

        try (ScanResult scanResult = classGraph.scan()) {
            for (final ClassInfo classInfo :
                    scanResult.getClassesWithAnnotation(this.ANNOTATION_CLASS.getCanonicalName())) {
                LOGGER.debug(() -> "Found class with contract annotation: " + classInfo.getName());
                final Class<SerializerInterface> cls = (Class<SerializerInterface>) classInfo.loadClass();
                LOGGER.debug("Loaded class");

                final String className = cls.getCanonicalName();
                if (!seenClass.contains(className)) {
                    seenClass.add(className);
                    this.add(className, Serializer.TARGET.TRANSACTION, cls);
                }
            }
        }
    }
}
