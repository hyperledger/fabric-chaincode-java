/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.shim;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import static org.hyperledger.fabric.shim.Chaincode.Response.Status.INTERNAL_SERVER_ERROR;
import static org.hyperledger.fabric.shim.Chaincode.Response.Status.SUCCESS;

public class ResponseUtils {
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
        return newErrorResponse(throwable.getMessage(), printStackTrace(throwable));
    }

    private static byte[] printStackTrace(Throwable throwable) {
        if (throwable == null) return null;
        final StringWriter buffer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(buffer));
        return buffer.toString().getBytes(StandardCharsets.UTF_8);
    }

}
