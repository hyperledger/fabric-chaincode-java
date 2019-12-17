/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.shim;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines methods that all chaincodes must implement.
 */
public interface Chaincode {
    /**
     * Called during an instantiate transaction after the container has been
     * established, allowing the chaincode to initialize its internal data.
     *
     * @param stub the chaincode stub
     * @return the chaincode response
     */
    Response init(ChaincodeStub stub);

    /**
     * Called for every Invoke transaction. The chaincode may change its state
     * variables.
     *
     * @param stub the chaincode stub
     * @return the chaincode response
     */
    Response invoke(ChaincodeStub stub);

    /**
     * Wrapper around protobuf Response, contains status, message and payload.
     * Object returned by call to {@link #init(ChaincodeStub)}
     * and{@link #invoke(ChaincodeStub)}
     */
    class Response {

        private final int statusCode;
        private final String message;
        private final byte[] payload;

        public Response(final Status status, final String message, final byte[] payload) {
            this.statusCode = status.getCode();
            this.message = message;
            this.payload = payload;
        }

        public Response(final int statusCode, final String message, final byte[] payload) {
            this.statusCode = statusCode;
            this.message = message;
            this.payload = payload;
        }

        public Status getStatus() {
            if (Status.hasStatusForCode(statusCode)) {
                return Status.forCode(statusCode);
            } else {
                return null;
            }
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getMessage() {
            return message;
        }

        public byte[] getPayload() {
            return payload;
        }

        public String getStringPayload() {
            return (payload == null) ? null : new String(payload, UTF_8);
        }

        /**
         * {@link Response} status enum.
         */
        public enum Status {
            SUCCESS(200), ERROR_THRESHOLD(400), INTERNAL_SERVER_ERROR(500);

            private static final Map<Integer, Status> CODETOSTATUS = new HashMap<>();
            private final int code;

            Status(final int code) {
                this.code = code;
            }

            public int getCode() {
                return code;
            }

            public static Status forCode(final int code) {
                final Status result = CODETOSTATUS.get(code);
                if (result == null) {
                    throw new IllegalArgumentException("no status for code " + code);
                }
                return result;
            }

            public static boolean hasStatusForCode(final int code) {
                return CODETOSTATUS.containsKey(code);
            }

            static {
                for (final Status status : Status.values()) {
                    CODETOSTATUS.put(status.code, status);
                }
            }

        }

    }
}
