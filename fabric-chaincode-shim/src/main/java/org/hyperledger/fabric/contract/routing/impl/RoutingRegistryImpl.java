/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.contract.routing.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hyperledger.fabric.Logger;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.ContractRuntimeException;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.contract.execution.InvocationRequest;
import org.hyperledger.fabric.contract.routing.ContractDefinition;
import org.hyperledger.fabric.contract.routing.RoutingRegistry;
import org.hyperledger.fabric.contract.routing.TxFunction;
import org.hyperledger.fabric.contract.routing.TypeRegistry;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;

/**
 * Registry to hold permit access to the routing definitions. This is the
 * primary internal data structure to permit access to information about the
 * contracts, and their transaction functions.
 *
 * Contracts are added, and processed. At runtime, this can then be accessed to
 * locate a specific 'Route' that can be handed off to the ExecutionService
 *
 */
public final class RoutingRegistryImpl implements RoutingRegistry {
    private static Logger logger = Logger.getLogger(RoutingRegistryImpl.class);

    private final Map<String, ContractDefinition> contracts = new HashMap<>();

    /*
     * (non-Javadoc)
     *
     * @see
     * org.hyperledger.fabric.contract.routing.RoutingRegistry#addNewContract(java.
     * lang.Class)
     */
    @Override
    public ContractDefinition addNewContract(final Class<ContractInterface> clz) {
        logger.debug(() -> "Adding new Contract Class " + clz.getCanonicalName());
        ContractDefinition contract;
        contract = new ContractDefinitionImpl(clz);

        // index this by the full qualified name
        contracts.put(contract.getName(), contract);
        if (contract.isDefault()) {
            contracts.put(InvocationRequest.DEFAULT_NAMESPACE, contract);
        }

        logger.debug(() -> "Put new contract in under name " + contract.getName());
        return contract;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.hyperledger.fabric.contract.routing.RoutingRegistry#containsRoute(org.
     * hyperledger.fabric.contract.execution.InvocationRequest)
     */
    @Override
    public boolean containsRoute(final InvocationRequest request) {
        if (contracts.containsKey(request.getNamespace())) {
            final ContractDefinition cd = contracts.get(request.getNamespace());

            if (cd.hasTxFunction(request.getMethod())) {
                return true;
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.hyperledger.fabric.contract.routing.RoutingRegistry#getRoute(org.
     * hyperledger.fabric.contract.execution.InvocationRequest)
     */
    @Override
    public TxFunction.Routing getRoute(final InvocationRequest request) {
        final TxFunction txFunction = contracts.get(request.getNamespace()).getTxFunction(request.getMethod());
        return txFunction.getRouting();
    }

    @Override
    public TxFunction getTxFn(final InvocationRequest request) {
        final TxFunction txFunction = contracts.get(request.getNamespace()).getTxFunction(request.getMethod());
        return txFunction;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.hyperledger.fabric.contract.routing.RoutingRegistry#getContract(java.lang
     * .String)
     */
    @Override
    public ContractDefinition getContract(final String namespace) {
        final ContractDefinition contract = contracts.get(namespace);

        if (contract == null) {
            throw new ContractRuntimeException("Undefined contract called");
        }

        return contract;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.hyperledger.fabric.contract.routing.RoutingRegistry#getAllDefinitions()
     */
    @Override
    public Collection<ContractDefinition> getAllDefinitions() {
        return contracts.values();

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.hyperledger.fabric.contract.routing.RoutingRegistry#findAndSetContracts()
     */
    @Override
    @SuppressWarnings("unchecked")
    public void findAndSetContracts(final TypeRegistry typeRegistry) {

        // Find all classes that are valid contract or data type instances.
        final ClassGraph classGraph = new ClassGraph()
                .enableClassInfo()
                .enableAnnotationInfo();
        final List<Class<ContractInterface>> contractClasses = new ArrayList<>();
        final List<Class<?>> dataTypeClasses = new ArrayList<>();
        try (ScanResult scanResult = classGraph.scan()) {
            for (final ClassInfo classInfo : scanResult.getClassesWithAnnotation(Contract.class.getCanonicalName())) {
                logger.debug("Found class with contract annotation: " + classInfo.getName());
                try {
                    final Class<?> contractClass = classInfo.loadClass();
                    logger.debug("Loaded class");
                    final Contract annotation = contractClass.getAnnotation(Contract.class);
                    if (annotation == null) {
                        // Since we check by name above, it makes sense to check it's actually
                        // compatible,
                        // and not some random class with the same name.
                        logger.debug("Class does not have compatible contract annotation");
                    } else if (!ContractInterface.class.isAssignableFrom(contractClass)) {
                        logger.debug("Class is not assignable from ContractInterface");
                    } else {
                        logger.debug("Class is assignable from ContractInterface");
                        contractClasses.add((Class<ContractInterface>) contractClass);
                    }
                } catch (final IllegalArgumentException e) {
                    logger.debug("Failed to load class: " + e);
                }
            }
            for (final ClassInfo classInfo : scanResult.getClassesWithAnnotation(DataType.class.getCanonicalName())) {
                logger.debug("Found class with data type annotation: " + classInfo.getName());
                try {
                    final Class<?> dataTypeClass = classInfo.loadClass();
                    logger.debug("Loaded class");
                    final DataType annotation = dataTypeClass.getAnnotation(DataType.class);
                    if (annotation == null) {
                        // Since we check by name above, it makes sense to check it's actually
                        // compatible,
                        // and not some random class with the same name.
                        logger.debug("Class does not have compatible data type annotation");
                    } else {
                        logger.debug("Class has compatible data type annotation");
                        dataTypeClasses.add(dataTypeClass);
                    }
                } catch (final IllegalArgumentException e) {
                    logger.debug("Failed to load class: " + e);
                }
            }
        }

        // store the contracts in the internal data structures
        addContracts(contractClasses);

        // now need to look for the data types have been set with the
        dataTypeClasses.forEach(typeRegistry::addDataType);

    }

    private void addContracts(final List<Class<ContractInterface>> contractClasses) {
        // set to ensure that we don't scan the same class twice
        final Set<String> seenClass = new HashSet<>();

        // loop over all the classes that have the Contract annotation
        for (final Class<ContractInterface> contractClass : contractClasses) {
            final String className = contractClass.getCanonicalName();
            if (!seenClass.contains(className)) {
                final ContractDefinition contract = addNewContract(contractClass);

                logger.debug("Searching annotated methods");
                for (final Method m : contractClass.getMethods()) {
                    if (m.getAnnotation(Transaction.class) != null) {
                        logger.debug("Found annotated method " + m.getName());

                        contract.addTxFunction(m);

                    }
                }

                seenClass.add(className);
            }
        }
    }

}
