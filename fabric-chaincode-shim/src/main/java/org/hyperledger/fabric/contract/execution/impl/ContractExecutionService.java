/*
Copyright IBM Corp., DTCC All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.contract.execution.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hyperledger.fabric.Logger;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.ContractRuntimeException;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.contract.execution.ExecutionService;
import org.hyperledger.fabric.contract.execution.InvocationRequest;
import org.hyperledger.fabric.contract.execution.JSONTransactionSerializer;
import org.hyperledger.fabric.contract.metadata.TypeSchema;
import org.hyperledger.fabric.contract.routing.ParameterDefinition;
import org.hyperledger.fabric.contract.routing.TxFunction;
import org.hyperledger.fabric.contract.routing.TypeRegistry;
import org.hyperledger.fabric.shim.Chaincode;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ResponseUtils;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class ContractExecutionService implements ExecutionService {

	private static Logger logger = Logger.getLogger(ContractExecutionService.class.getName());

	private JSONTransactionSerializer serializer;

	Map<String, Object> proxies = new HashMap<>();
	Map<String, ProxyMethodInterceptor> methodInterceptors = new HashMap<>();

	public ContractExecutionService(TypeRegistry typeRegistry) {
		// FUTURE: Permit this to swapped out as per node.js
		this.serializer = new JSONTransactionSerializer(typeRegistry);
	}

	@Override
	public Chaincode.Response executeRequest(TxFunction txFn, InvocationRequest req, ChaincodeStub stub) {
		logger.debug(() -> "Routing Request" + txFn);
		TxFunction.Routing rd = txFn.getRouting();

		final ContractInterface contractObject = rd.getContractObject();
		final Class<?> contractClass = rd.getContractClass();
		if (!proxies.containsKey(req.getNamespace())) {
			ProxyMethodInterceptor interceptor = createMethodInterceptorForContract(contractClass, contractObject);
			methodInterceptors.put(req.getNamespace(), interceptor);
			proxies.put(req.getNamespace(), createProxyForContract(contractClass, interceptor));
		}

		Object proxyObject = proxies.get(req.getNamespace());
		ProxyMethodInterceptor interceptor = methodInterceptors.get(req.getNamespace());
		interceptor.setContextForThread(stub);
		final List<Object> args = convertArgs(req.getArgs(), txFn);

		Chaincode.Response response;
		try {
			Object value = rd.getMethod().invoke(proxyObject, args.toArray());
			if (value == null) {
				response = ResponseUtils.newSuccessResponse();
			} else {
				response = ResponseUtils.newSuccessResponse(convertReturn(value, txFn));
			}
		} catch (IllegalAccessException e) {
			logger.error(() -> "Error during contract method invocation" + e);
			response = ResponseUtils.newErrorResponse(e);
		} catch (InvocationTargetException e) {
			logger.error(() -> "Error during contract method invocation" + e);
			response = ResponseUtils.newErrorResponse(e.getCause());
		}
		return response;
	}

	private byte[] convertReturn(Object obj, TxFunction txFn) {
		byte[] buffer;
		TypeSchema ts = txFn.getReturnSchema();
		buffer = serializer.toBuffer(obj, ts);

		return buffer;
	}

	private List<Object> convertArgs(List<byte[]> stubArgs, TxFunction txFn) {

		List<ParameterDefinition> schemaParams = txFn.getParamsList();
		List<Object> args = new ArrayList<>();
		for (int i = 0; i < schemaParams.size(); i++) {
			args.add(serializer.fromBuffer(stubArgs.get(i), schemaParams.get(i).getSchema()));
		}
		return args;
	}

	private Object createProxyForContract(Class<?> contractClass, ProxyMethodInterceptor interceptor) {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(contractClass);
		enhancer.setCallback(interceptor);
		return enhancer.create();
	}

	private ProxyMethodInterceptor createMethodInterceptorForContract(final Class<?> contractClass,
			final ContractInterface contractObject) {
		return new ProxyMethodInterceptor(contractClass, contractObject);
	}

	private static class ProxyMethodInterceptor implements MethodInterceptor {
		// TODO: Check if this is really needed
		Class<?> contractClass;
		ContractInterface contractObject;
		ThreadLocal<Context> context = new ThreadLocal<>();

		public ProxyMethodInterceptor(Class<?> contractClass, ContractInterface contractObject) {
			this.contractClass = contractClass;
			this.contractObject = contractObject;
		}

		public void setContextForThread(ChaincodeStub stub) {
			context.set(contractObject.createContext(stub));
		}

		@Override
		public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
			if (method.getName().equals("getContext")) {
				return context.get();
			} else if (method.getDeclaringClass() != Object.class
					&& method.getDeclaringClass() != ContractInterface.class
					&& method.getAnnotation(Transaction.class) != null) {
				contractObject.beforeTransaction();
				Object result = proxy.invokeSuper(obj, args);
				contractObject.afterTransaction();
				return result;
			} else {
				return proxy.invokeSuper(obj, args);
			}
		}

	}

}
