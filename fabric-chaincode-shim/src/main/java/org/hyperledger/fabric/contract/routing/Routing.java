/*
Copyright IBM Corp., DTCC All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.contract.routing;

import org.hyperledger.fabric.contract.ContractInterface;

import java.lang.reflect.Method;

/**
 * Routing information for each contract method
 */
public interface Routing {

    ContractInterface getContractObject();

    Method getMethod();

    Class getContractClass();

    TransactionType getType();

}
