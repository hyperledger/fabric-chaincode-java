/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.contract;

import org.hyperledger.fabric.Logger;
import org.hyperledger.fabric.contract.execution.ExecutionFactory;
import org.hyperledger.fabric.contract.execution.ExecutionService;
import org.hyperledger.fabric.contract.execution.InvocationRequest;
import org.hyperledger.fabric.contract.metadata.MetadataBuilder;
import org.hyperledger.fabric.contract.routing.ContractDefinition;
import org.hyperledger.fabric.contract.routing.RoutingRegistry;
import org.hyperledger.fabric.contract.routing.TxFunction;
import org.hyperledger.fabric.contract.routing.TxFunction.Routing;
import org.hyperledger.fabric.contract.routing.TypeRegistry;
import org.hyperledger.fabric.contract.routing.impl.RoutingRegistryImpl;
import org.hyperledger.fabric.contract.routing.impl.TypeRegistryImpl;
import org.hyperledger.fabric.shim.ChaincodeBase;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ResponseUtils;

/**
 * Router class routes Init/Invoke requests to contracts. Implements
 * {@link org.hyperledger.fabric.shim.Chaincode} interface.
 */
public class ContractRouter extends ChaincodeBase {
	private static Logger logger = Logger.getLogger(ContractRouter.class.getName());

	private RoutingRegistry registry;
	private TypeRegistry typeRegistry;
	private ExecutionService executor;

	/**
	 * Take the arguments from the cli, and initiate processing of cli options and
	 * environment variables.
	 *
	 * Create the Contract scanner, and the Execution service
	 *
	 * @param args
	 */
	public ContractRouter(String[] args) {
		super.initializeLogging();
		super.processEnvironmentOptions();
		super.processCommandLineOptions(args);

		super.validateOptions();
		registry = new RoutingRegistryImpl();
		typeRegistry = new TypeRegistryImpl();
		executor = ExecutionFactory.getInstance().createExecutionService();
	}

	/**
	 * Locate all the contracts that are available on the classpath
	 */
	void findAllContracts() {
			registry.findAndSetContracts(this.typeRegistry);
	}

	/**
	 * Start the chaincode container off and running, this will send the initial
	 * flow back to the peer
	 *
	 * @throws Exception
	 */
	void startRouting() {
		try {
			super.connectToPeer();
		} catch (Exception e) {
			ContractRuntimeException cre = new ContractRuntimeException("Unable to start routing");
			logger.error(()->logger.formatError(cre));
			throw cre;
		}
	}

	@Override
	public Response init(ChaincodeStub stub) {
		InvocationRequest request = ExecutionFactory.getInstance().createRequest(stub);
		Routing routing = getRouting(request);

		logger.debug(() -> "Got routing:" + routing);
		return executor.executeRequest(routing, request, stub);
	}

	@Override
	public Response invoke(ChaincodeStub stub) {
		logger.debug(() -> "Got the invocations:" + stub.getFunction() + " " + stub.getParameters());
		InvocationRequest request = ExecutionFactory.getInstance().createRequest(stub);
		Routing routing = getRouting(request);

		logger.debug(() -> "Got routing:" + routing);
		return executor.executeRequest(routing, request, stub);
	}

	/**
	 * Given the Invocation Request, return the routing object for this call
	 *
	 * @param request
	 * @return
	 */
	TxFunction.Routing getRouting(InvocationRequest request) {
    	//request name is the fully qualified 'name:txname'
        if (registry.containsRoute(request)) {
            return registry.getRoute(request);
        } else {
        	ContractDefinition contract = registry.getContract(request.getNamespace());
        	return contract.getUnkownRoute();
        }
	}

	/**
	 * Main method to start the contract based chaincode
	 *
	 */
	public static void main(String[] args) {

		ContractRouter cfc = new ContractRouter(args);
		cfc.findAllContracts();

		// Create the Metadata ahead of time rather than have to produce every
		// time
		MetadataBuilder.initialize(cfc.getRoutingRegistry(),cfc.getTypeRegistry());
		logger.info(() -> "Metadata follows:" + MetadataBuilder.debugString());

		// commence routing, once this has returned the chaincode and contract api is
		// 'open for business'
		cfc.startRouting();

	}

	private TypeRegistry getTypeRegistry() {
		return this.typeRegistry;
	}

	private RoutingRegistry getRoutingRegistry() {
		return this.registry;
	}
}
