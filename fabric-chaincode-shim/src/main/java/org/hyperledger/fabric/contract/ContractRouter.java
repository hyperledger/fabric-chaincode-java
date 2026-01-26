/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.contract;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;
import org.hyperledger.fabric.Logging;
import org.hyperledger.fabric.contract.execution.ExecutionFactory;
import org.hyperledger.fabric.contract.execution.ExecutionService;
import org.hyperledger.fabric.contract.execution.InvocationRequest;
import org.hyperledger.fabric.contract.metadata.MetadataBuilder;
import org.hyperledger.fabric.contract.routing.ContractDefinition;
import org.hyperledger.fabric.contract.routing.RoutingRegistry;
import org.hyperledger.fabric.contract.routing.TxFunction;
import org.hyperledger.fabric.contract.routing.TypeRegistry;
import org.hyperledger.fabric.contract.routing.impl.RoutingRegistryImpl;
import org.hyperledger.fabric.contract.routing.impl.SerializerRegistryImpl;
import org.hyperledger.fabric.metrics.Metrics;
import org.hyperledger.fabric.shim.ChaincodeBase;
import org.hyperledger.fabric.shim.ChaincodeServer;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.NettyChaincodeServer;
import org.hyperledger.fabric.shim.ResponseUtils;
import org.hyperledger.fabric.traces.Traces;

/**
 * Router class routes Init/Invoke requests to contracts. Implements {@link org.hyperledger.fabric.shim.Chaincode}
 * interface.
 *
 * @see ContractInterface
 */
public final class ContractRouter extends ChaincodeBase {
    private static final Logger LOGGER = Logger.getLogger(ContractRouter.class.getName());

    private final RoutingRegistry registry;
    private final TypeRegistry typeRegistry;

    // Store instances of SerializerInterfaces - identified by the contract
    // annotation (default is JSON)
    private final SerializerRegistryImpl serializers;
    private final ExecutionService executor;

    /**
     * Take the arguments from the cli, and initiate processing of cli options and environment variables.
     *
     * <p>Create the Contract scanner, and the Execution service
     *
     * @param args
     */
    public ContractRouter(final String[] args) {
        super();
        super.initializeLogging();
        super.processEnvironmentOptions();
        super.processCommandLineOptions(args);
        super.validateOptions();

        final Properties props = super.getChaincodeConfig();
        Metrics.initialize(props);
        Traces.initialize(props);

        LOGGER.fine("ContractRouter<init>");
        registry = new RoutingRegistryImpl();
        typeRegistry = TypeRegistry.getRegistry();

        serializers = new SerializerRegistryImpl();

        try {
            serializers.findAndSetContents();
        } catch (InstantiationException | IllegalAccessException e) {
            final ContractRuntimeException cre = new ContractRuntimeException("Unable to locate Serializers", e);
            LOGGER.severe(() -> Logging.formatError(cre));
            throw cre;
        }

        executor = ExecutionFactory.getInstance().createExecutionService(serializers);
    }

    /** Locate all the contracts that are available on the classpath. */
    void findAllContracts() {
        registry.findAndSetContracts(this.typeRegistry);
    }

    /**
     * Start the chaincode container off and running.
     *
     * <p>This will send the initial flow back to the peer
     *
     * @throws Exception
     */
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    void startRouting() {
        try {
            super.connectToPeer();
        } catch (final Exception e) {
            LOGGER.severe(() -> Logging.formatError(e));
            throw new ContractRuntimeException("Unable to start routing", e);
        }
    }

    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    private Response processRequest(final ChaincodeStub stub) {
        LOGGER.info(() -> "Got invoke routing request");
        try {
            if (stub.getStringArgs().isEmpty()) {
                return ResponseUtils.newSuccessResponse();
            }

            LOGGER.info(() -> "Got the invoke request for:" + stub.getFunction() + " " + stub.getParameters());
            final InvocationRequest request = ExecutionFactory.getInstance().createRequest(stub);
            final TxFunction txFn = getRouting(request);
            LOGGER.info(() -> "Got routing:" + txFn.getRouting());
            return executor.executeRequest(txFn, request, stub);
        } catch (final Throwable throwable) {
            return ResponseUtils.newErrorResponse(throwable);
        }
    }

    @Override
    public Response invoke(final ChaincodeStub stub) {
        return processRequest(stub);
    }

    @Override
    public Response init(final ChaincodeStub stub) {
        return processRequest(stub);
    }

    /**
     * Given the Invocation Request, return the routing object for this call.
     *
     * @param request
     * @return TxFunction for the request
     */
    TxFunction getRouting(final InvocationRequest request) {
        // request name is the fully qualified 'name:txname'
        if (registry.containsRoute(request)) {
            return registry.getTxFn(request);
        } else {
            LOGGER.fine(() -> "Namespace is " + request);
            final ContractDefinition contract = registry.getContract(request.getNamespace());
            return contract.getUnknownRoute();
        }
    }

    /**
     * Main method to start the contract based chaincode.
     *
     * @param args
     */
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public static void main(final String[] args) throws Exception {

        final ContractRouter cfc = new ContractRouter(args);
        cfc.findAllContracts();

        LOGGER.fine(() -> cfc.getRoutingRegistry().toString());

        // Create the Metadata ahead of time rather than have to produce every
        // time
        MetadataBuilder.initialize(cfc.getRoutingRegistry(), cfc.getTypeRegistry());
        LOGGER.info(() -> "Metadata follows:" + MetadataBuilder.debugString());

        // check if this should be running in client or server mode
        if (cfc.isServer()) {
            LOGGER.info("Starting chaincode as server");
            ChaincodeServer chaincodeServer = new NettyChaincodeServer(cfc, cfc.getChaincodeServerConfig());
            chaincodeServer.start();
        } else {
            LOGGER.info("Starting chaincode as client");
            cfc.startRouting();
        }
    }

    TypeRegistry getTypeRegistry() {
        return this.typeRegistry;
    }

    RoutingRegistry getRoutingRegistry() {
        return this.registry;
    }

    /**
     * Start router and Chaincode server.
     *
     * @param chaincodeServer
     */
    public void startRouterWithChaincodeServer(final ChaincodeServer chaincodeServer)
            throws IOException, InterruptedException {
        findAllContracts();
        LOGGER.fine(() -> getRoutingRegistry().toString());

        MetadataBuilder.initialize(getRoutingRegistry(), getTypeRegistry());
        LOGGER.info(() -> "Metadata follows:" + MetadataBuilder.debugString());

        chaincodeServer.start();
    }
}
