/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

import org.hyperledger.fabric.shim.ChaincodeBase;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ResponseUtils;

public final class EmptyChaincodeWithoutPackage extends ChaincodeBase {
    @Override
    public Response init(final ChaincodeStub stub) {
        return ResponseUtils.newSuccessResponse();
    }

    @Override
    public Response invoke(final ChaincodeStub stub) {
        return ResponseUtils.newSuccessResponse();
    }
}
