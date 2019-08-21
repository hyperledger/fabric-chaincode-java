/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.contract.routing.impl;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.hyperledger.fabric.Logging;
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
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

/**
 * Registry to hold permit access to the routing definitions. This is the
 * primary internal data structure to permit access to information about the
 * contracts, and their transaction functions.
 *
 * Contracts are added, and processed. At runtime, this can then be accessed to
 * locate a specific 'Route' that can be handed off to the ExecutionService
 *
 */
public class RoutingRegistryImpl implements RoutingRegistry {
    private static Logger logger = Logging.getLogger(RoutingRegistryImpl.class);

    private Map<String, ContractDefinition> contracts = new HashMap<>();

    /*
     * (non-Javadoc)
     *
     * @see
     * org.hyperledger.fabric.contract.routing.RoutingRegistry#addNewContract(java.
     * lang.Class)
     */
    @Override
    public ContractDefinition addNewContract(Class<ContractInterface> clz) {
        logger.fine(() -> "Adding new Contract Class " + clz.getCanonicalName());
        ContractDefinition contract;
        contract = new ContractDefinitionImpl(clz);

        // index this by the full qualified name
        contracts.put(contract.getName(), contract);
        if (contract.isDefault()) {
            contracts.put(InvocationRequest.DEFAULT_NAMESPACE, contract);
        }

        logger.fine(() -> "Put new contract in under name " + contract.getName());
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
    public boolean containsRoute(InvocationRequest request) {
        if (contracts.containsKey(request.getNamespace())) {
            ContractDefinition cd = contracts.get(request.getNamespace());

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
    public TxFunction.Routing getRoute(InvocationRequest request) {
        TxFunction txFunction = contracts.get(request.getNamespace()).getTxFunction(request.getMethod());
        return txFunction.getRouting();
    }

    @Override
    public TxFunction getTxFn(InvocationRequest request) {
        TxFunction txFunction = contracts.get(request.getNamespace()).getTxFunction(request.getMethod());
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
    public ContractDefinition getContract(String namespace) {
        ContractDefinition contract = contracts.get(namespace);

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
    @SuppressWarnings("unchecked")
	@Override
    public void findAndSetContracts(TypeRegistry typeRegistry) {
        ArrayList<URL> urls = new ArrayList<>();
        ClassLoader[] classloaders = { getClass().getClassLoader(), Thread.currentThread().getContextClassLoader() };
        for (int i = 0; i < classloaders.length; i++) {
            if (classloaders[i] instanceof URLClassLoader) {
                urls.addAll(Arrays.asList(((URLClassLoader) classloaders[i]).getURLs()));
            } else {
                throw new RuntimeException("classLoader is not an instanceof URLClassLoader");
            }
        }

        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.addUrls(urls);
        configurationBuilder.addUrls(ClasspathHelper.forJavaClassPath());
        Reflections ref = new Reflections(configurationBuilder);

        logger.info("Searching chaincode class in urls: " + urls);

        // set to ensure that we don't scan the same class twice
        Set<String> seenClass = new HashSet<>();

        // loop over all the classes that have the Contract annotation
        for (Class<?> cl : ref.getTypesAnnotatedWith(Contract.class)) {
            logger.info("Found class: " + cl.getCanonicalName());
            if (ContractInterface.class.isAssignableFrom(cl)) {
                logger.fine("Inheritance ok");
                String className = cl.getCanonicalName();

                if (!seenClass.contains(className)) {
                    ContractDefinition contract = addNewContract((Class<ContractInterface>) cl);

                    logger.fine("Searching annotated methods");
                    for (Method m : cl.getMethods()) {
                        if (m.getAnnotation(Transaction.class) != null) {
                            logger.fine("Found annotated method " + m.getName());

                            contract.addTxFunction(m);

                        }
                    }

                    seenClass.add(className);
                }
            } else {
                logger.fine("Class is not assignabled from Contract");
            }
        }

        // now need to look for the data types have been set with the
        logger.info("Looking for the data types");
        Set<Class<?>> czs = ref.getTypesAnnotatedWith(DataType.class);
        logger.info("found " + czs.size());
        czs.forEach(typeRegistry::addDataType);

    }

}
