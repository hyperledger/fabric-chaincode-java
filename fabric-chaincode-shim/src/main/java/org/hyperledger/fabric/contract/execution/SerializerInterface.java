/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.contract.execution;

import org.hyperledger.fabric.contract.metadata.TypeSchema;

/**
 * This interface allows contract developers to change the serialization
 * mechanism. There are two scenarios where instances of DataTypes are
 * serialized.
 *
 * When the objects are (logically) transferred from the Client application to
 * the Contract resulting in a transaction function being invoked. Typically this
 * is JSON, hence a default JSON parser is provided.
 *
 * The JSONTransactionSerializer can be extended if needed
 */
public interface SerializerInterface {

    /**
     * Convert the value supplied to a byte array, according to the TypeSchema.
     *
     * @param value
     * @param ts
     * @return buffer
     */
    byte[] toBuffer(Object value, TypeSchema ts);

    /**
     * Take the byte buffer and return the object as required.
     *
     * @param buffer Byte buffer from the wire
     * @param ts     TypeSchema representing the type
     *
     * @return Object created; relies on Java auto-boxing for primitives
     *
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    Object fromBuffer(byte[] buffer, TypeSchema ts);

}
