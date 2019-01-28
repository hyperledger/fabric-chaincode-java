/*
Copyright IBM Corp., DTCC All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.contract;

import org.hyperledger.fabric.shim.ChaincodeStub;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Factory to create {@link Context} from {@link ChaincodeStub}
 * by wrapping stub with dynamic proxy.
 */
public class ContextFactory {
    private static ContextFactory cf;

    static synchronized public ContextFactory getInstance() {
        if (cf == null) {
            cf = new ContextFactory();
        }
        return cf;
    }

    public synchronized Context createContext(final ChaincodeStub stub) {
        Context newContext = (Context) Proxy.newProxyInstance(
                this.getClass().getClassLoader(),
                new Class[]{Context.class},
                new ContextInvocationHandler(stub)
        );
        return newContext;
    }

    static class ContextInvocationHandler implements InvocationHandler {

        private ChaincodeStub stub;

        ContextInvocationHandler(final ChaincodeStub stub) {
            this.stub = stub;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Method m = stub.getClass().getMethod(method.getName(), method.getParameterTypes());
            return m.invoke(stub, args);
        }
    }

}
