/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

/**
 * Provides interfaces and classes required for chaincode development and state variable access.
 *
 * <p>
 * It is possible to implement Java chaincode by extending the {@link org.hyperledger.fabric.shim.ChaincodeBase} class however new projects should should implement {@link org.hyperledger.fabric.contract.ContractInterface} and use the contract programming model instead.
 *
 * @see org.hyperledger.fabric.contract
 * @see org.hyperledger.fabric.shim.ChaincodeBase
 */
package org.hyperledger.fabric.shim;

