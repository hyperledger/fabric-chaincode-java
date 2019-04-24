/*
Copyright IBM Corp., DTCC All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.contract.execution.impl;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Init;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.contract.execution.ExecutionService;
import org.hyperledger.fabric.contract.execution.InvocationRequest;
import org.hyperledger.fabric.contract.routing.Routing;
import org.hyperledger.fabric.shim.Chaincode;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ResponseUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContractExecutionService implements ExecutionService {

    private static Log logger = LogFactory.getLog(ContractExecutionService.class);

    Map<String, Object> proxies = new HashMap<>();
    Map<String, ProxyMethodInterceptor> methodInterceptors = new HashMap<>();

    @Override
    public Chaincode.Response executeRequest(Routing rd, InvocationRequest req, ChaincodeStub stub) {
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
        final List<Object> args = convertArgs(req.getArgs(), rd.getMethod().getParameterTypes());

        Chaincode.Response response;
        try {
            response = (Chaincode.Response)rd.getMethod().invoke(proxyObject, args.toArray());
        } catch (IllegalAccessException|InvocationTargetException e) {
            logger.warn("Error during contract method invocation", e);
            response = ResponseUtils.newErrorResponse(e);
        }
        return response;
    }

    private List<Object> convertArgs(List<byte[]> stubArgs, Class<?>[] methodParameterTypes) {
        List<Object> args = new ArrayList<>();
        for (int i = 0; i < methodParameterTypes.length; i++) {
            Class<?> param = methodParameterTypes[i];
            if (param.isArray()) {
                args.add(stubArgs.get(i));
            } else {
                args.add(new String(stubArgs.get(i), StandardCharsets.UTF_8));
            }
        }
        return args;
    }

    private Object createProxyForContract(Class<?> contractClass, ProxyMethodInterceptor interceptor) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(contractClass);
        enhancer.setCallback(interceptor);
        return enhancer.create();
    }

    private ProxyMethodInterceptor createMethodInterceptorForContract(final Class<?> contractClass, final ContractInterface contractObject) {
        return new ProxyMethodInterceptor(contractClass, contractObject);
    }

    private static class ProxyMethodInterceptor implements MethodInterceptor {
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
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy)
                throws Throwable {
            if (method.getName().equals("getContext")) {
                return context.get();
            } else if (method.getDeclaringClass() != Object.class && method.getDeclaringClass() != ContractInterface.class &&
                    (method.getAnnotation(Init.class) != null || method.getAnnotation(Transaction.class) != null)) {
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
