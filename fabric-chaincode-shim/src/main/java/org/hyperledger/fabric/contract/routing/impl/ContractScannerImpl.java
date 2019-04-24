/*
Copyright IBM Corp., DTCC All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.contract.routing.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Init;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.contract.execution.InvocationRequest;
import org.hyperledger.fabric.contract.execution.impl.ContractExecutionService;
import org.hyperledger.fabric.contract.routing.ContractScanner;
import org.hyperledger.fabric.contract.routing.Routing;
import org.hyperledger.fabric.contract.routing.TransactionType;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ContractScannerImpl implements ContractScanner {
    private static Log logger = LogFactory.getLog(ContractScannerImpl.class);

    Map<String, Routing> routingData = new HashMap<>();
    Map<String, ContractInterface> contracts = new HashMap<>();

    @Override
    public void findAndSetContracts() throws IllegalAccessException, InstantiationException {
        ArrayList<URL> urls = new ArrayList<>();
        ClassLoader[] classloaders = {
                getClass().getClassLoader(),
                Thread.currentThread().getContextClassLoader()
        };
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
        Reflections ref = new Reflections();

        logger.debug("Searching chaincode class in urls: " + urls);

        for (Class<?> cl : ref.getTypesAnnotatedWith(Contract.class)) {
            String namespace = cl.getAnnotation(Contract.class).namespace();
            if (namespace == null || namespace.isEmpty()) {
                namespace = InvocationRequest.DEFAULT_NAMESPACE;
            }
            logger.debug("Found class: " + cl.getCanonicalName());
            logger.debug("Searching Init and Transaction annotated methods");
            if (ContractInterface.class.isAssignableFrom(cl)) {
                logger.debug("Inheritance ok");
                for (Method m : cl.getMethods()) {
                    if ((m.getAnnotation(Transaction.class) != null) || (m.getAnnotation(Init.class) != null)) {
                        logger.debug("Found annotated method " + m.getName());
                        RoutingImpl rd = new RoutingImpl();
                        rd.clazz = cl;
                        rd.method = m;
                        if (!contracts.containsKey(namespace)) {
                            logger.debug("Creating new instance for class " + rd.getContractClass().getName());
                            ContractInterface contract = (ContractInterface) rd.getContractClass().newInstance();
                            contracts.put(namespace, contract);
                        }
                        rd.contract = contracts.get(namespace);

                        if (m.getAnnotation(Transaction.class) != null) {
                            logger.debug("Found Transaction method: " + m.getName());
                            if (m.getAnnotation(Transaction.class).submit()) {
                                rd.type = TransactionType.INVOKE;
                            } else {
                                rd.type = TransactionType.QUERY;
                            }

                        }
                        if (m.getAnnotation(Init.class) != null) {
                            rd.type = TransactionType.INIT;
                            logger.debug("Found Init method: " + m.getName());
                        }
                        logger.debug("Storing routing data: " + namespace + ":" + m.getName());
                        routingData.put(namespace + ":" + m.getName(), rd);
                    }
                }
            }
        }
    }

    @Override
    public Routing getRouting(InvocationRequest req) {
        if (routingData.containsKey(req.getRequestName())) {
            return routingData.get(req.getRequestName());
        }
        return null;
    }

    @Override
    public Routing getDefaultRouting(InvocationRequest req) {
        if (contracts.containsKey(req.getNamespace())) {
            RoutingImpl tmpRoutingData = new RoutingImpl();
            tmpRoutingData.contract = contracts.get(req.getNamespace());
            try {
                tmpRoutingData.method = tmpRoutingData.contract.getClass().getMethod("unknownTransaction", new Class<?>[]{});
            } catch (NoSuchMethodException e) {

                return null;
            }
            tmpRoutingData.clazz = tmpRoutingData.contract.getClass();
            tmpRoutingData.type = TransactionType.DEFAULT;
            return tmpRoutingData;
        }
        return null;
    }
}
