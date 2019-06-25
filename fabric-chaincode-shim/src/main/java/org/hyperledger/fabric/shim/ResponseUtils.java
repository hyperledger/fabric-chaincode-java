/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.shim;

import static org.hyperledger.fabric.shim.Chaincode.Response.Status.INTERNAL_SERVER_ERROR;
import static org.hyperledger.fabric.shim.Chaincode.Response.Status.SUCCESS;

import org.hyperledger.fabric.Logger;

public class ResponseUtils {

    private static Logger logger = Logger.getLogger(ResponseUtils.class.getName());

    public static Chaincode.Response newSuccessResponse(String message, byte[] payload) {
        return new Chaincode.Response(SUCCESS, message, payload);
    }

    public static Chaincode.Response newSuccessResponse() {
        return newSuccessResponse(null, null);
    }

    public static Chaincode.Response newSuccessResponse(String message) {
        return newSuccessResponse(message, null);
    }

    public static Chaincode.Response newSuccessResponse(byte[] payload) {
        return newSuccessResponse(null, payload);
    }

    public static Chaincode.Response newErrorResponse(String message, byte[] payload) {
        return new Chaincode.Response(INTERNAL_SERVER_ERROR, message, payload);
    }

    public static Chaincode.Response newErrorResponse() {
        return newErrorResponse(null, null);
    }

    public static Chaincode.Response newErrorResponse(String message) {
        return newErrorResponse(message, null);
    }

    public static Chaincode.Response newErrorResponse(byte[] payload) {
        return newErrorResponse(null, payload);
    }

    public static Chaincode.Response newErrorResponse(Throwable throwable) {
        // Responses should not include internals like stack trace but make sure it gets logged
        logger.error(() -> logger.formatError(throwable));

        String message = null;
        byte[] payload = null;
        if (throwable instanceof ChaincodeException) {
            message = throwable.getMessage();
            payload = ((ChaincodeException) throwable).getPayload();
        } else {
            message = "Unexpected error";
        }

        return ResponseUtils.newErrorResponse(message, payload);
    }
}
