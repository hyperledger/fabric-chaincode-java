/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.contract.routing.impl;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
 * It holds the serializers that have been defined. JSONTransacationSerializer is the 
 * default.
 */
public class SerializerRegistryImpl {
    private static Logger logger = Logger.getLogger(SerializerRegistryImpl.class);

    private Class<Serializer> annotationClass = Serializer.class;

    public SerializerRegistryImpl() {
    }

    // Could index these by name and or type.
    private Map<String, SerializerInterface> contents = new HashMap<>();

    /**
     * Get a Serializer for the matching fully qualified classname, and the Target
     * 
     * @param name    fully qualified classname
     * @param target  the intended target of the serializer
     */
    public SerializerInterface getSerializer(String name, Serializer.TARGET target) {
        String key = name+":"+target;
        System.out.println("Getting "+key);
        return contents.get(key);
    }

    private SerializerInterface add(String name, Serializer.TARGET target, Class<SerializerInterface> clazz) {
        logger.debug(() -> "Adding new Class " + clazz.getCanonicalName()+" for "+target);
        try{
        	String key = name+":"+target;
            SerializerInterface newObj = clazz.newInstance();
            System.out.println("Addding "+key);
            this.contents.put(key,newObj);

            return newObj;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Find all the serializers that have been defined
     * 
     * @see
     * org.hyperledger.fabric.contract.routing.RoutingRegistry#findAndSetContracts()
     */
    public void findAndSetContents() throws InstantiationException, IllegalAccessException {

        ClassGraph classGraph = new ClassGraph()
            .enableClassInfo()
            .enableAnnotationInfo();

        // set to ensure that we don't scan the same class twice
        Set<String> seenClass = new HashSet<>();

        try (ScanResult scanResult = classGraph.scan()) {
            for (ClassInfo classInfo : scanResult.getClassesWithAnnotation(this.annotationClass.getCanonicalName())) {
                logger.debug("Found class with contract annotation: " + classInfo.getName());
                try {
                    Class<SerializerInterface> cls = (Class<SerializerInterface>)classInfo.loadClass();
                    logger.debug("Loaded class");

                    String className = cls.getCanonicalName();
                    if (!seenClass.contains(className)) {
                        seenClass.add(className);
                        this.add(className, Serializer.TARGET.TRANSACTION, cls);
                    }
                    
                } catch (IllegalArgumentException e) {
                    logger.debug("Failed to load class: " + e);
                }
            }

        }

    }


}
