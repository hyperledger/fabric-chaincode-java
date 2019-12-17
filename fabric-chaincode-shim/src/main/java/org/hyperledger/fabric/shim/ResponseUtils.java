/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.shim;

import static org.hyperledger.fabric.shim.Chaincode.Response.Status.INTERNAL_SERVER_ERROR;
import static org.hyperledger.fabric.shim.Chaincode.Response.Status.SUCCESS;

import org.hyperledger.fabric.Logger;

public final class ResponseUtils {

    private static Logger logger = Logger.getLogger(ResponseUtils.class.getName());

    private ResponseUtils() {
    }

    /**
     * @param message
     * @param payload
     * @return Chaincode.Response
     */
    public static Chaincode.Response newSuccessResponse(final String message, final byte[] payload) {
        return new Chaincode.Response(SUCCESS, message, payload);
    }

    /**
     * @return Chaincode.Response
     */
    public static Chaincode.Response newSuccessResponse() {
        return newSuccessResponse(null, null);
    }

    /**
     * @param message
     * @return Chaincode.Response
     */
    public static Chaincode.Response newSuccessResponse(final String message) {
        return newSuccessResponse(message, null);
    }

    /**
     * @param payload
     * @return Chaincode.Response
     */
    public static Chaincode.Response newSuccessResponse(final byte[] payload) {
        return newSuccessResponse(null, payload);
    }

    /**
     * @param message
     * @param payload
     * @return Chaincode.Response
     */
    public static Chaincode.Response newErrorResponse(final String message, final byte[] payload) {
        return new Chaincode.Response(INTERNAL_SERVER_ERROR, message, payload);
    }

    /**
     * @return Chaincode.Response
     */
    public static Chaincode.Response newErrorResponse() {
        return newErrorResponse(null, null);
    }

    /**
     * @param message
     * @return Chaincode.Response
     */
    public static Chaincode.Response newErrorResponse(final String message) {
        return newErrorResponse(message, null);
    }

    /**
     * @param payload
     * @return Chaincode.Response
     */
    public static Chaincode.Response newErrorResponse(final byte[] payload) {
        return newErrorResponse(null, payload);
    }

    /**
     * @param throwable
     * @return Chaincode.Response
     */
    public static Chaincode.Response newErrorResponse(final Throwable throwable) {
        // Responses should not include internals like stack trace but make sure it gets
        // logged
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
