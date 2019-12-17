/*
 * Copyright 2019 IBM DTCC All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.contract;

import java.io.IOException;
import java.security.cert.CertificateException;

import org.hyperledger.fabric.shim.ChaincodeStub;
import org.json.JSONException;

/**
 *
 * This context is available to all 'transaction functions' and provides the
 * transaction context. It also provides access to the APIs for the world state
 * using {@link #getStub()}
 * <p>
 * Applications can implement their own versions if they wish to add
 * functionality. All subclasses MUST implement a constructor, for example
 *
 * <pre>
 * {@code
 *
 * public MyContext extends Context {
 *
 *     public MyContext(ChaincodeStub stub) {
 *        super(stub);
 *     }
 * }
 *
 *}
 * </pre>
 *
 */
public class Context {
    /**
     *
     */
    protected ChaincodeStub stub;

    /**
     *
     */
    protected ClientIdentity clientIdentity;

    /**
     * Creates new client identity and sets it as a property of the stub.
     *
     * @param stub Instance of the {@link ChaincodeStub} to use
     */
    public Context(final ChaincodeStub stub) {
        this.stub = stub;
        try {
            this.clientIdentity = new ClientIdentity(stub);
        } catch (CertificateException | JSONException | IOException e) {
            throw new ContractRuntimeException("Could not create new client identity", e);
        }
    }

    /**
     *
     * @return ChaincodeStub instance to use
     */
    public ChaincodeStub getStub() {
        return this.stub;
    }

    /**
     *
     * @return ClientIdentity object to use
     */
    public ClientIdentity getClientIdentity() {
        return this.clientIdentity;
    }
}
