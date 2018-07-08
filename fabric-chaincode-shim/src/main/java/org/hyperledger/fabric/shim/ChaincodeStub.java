/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.shim;

import org.hyperledger.fabric.protos.peer.ChaincodeEventPackage.ChaincodeEvent;
import org.hyperledger.fabric.protos.peer.ProposalPackage.SignedProposal;
import org.hyperledger.fabric.shim.Chaincode.Response;
import org.hyperledger.fabric.shim.ledger.CompositeKey;
import org.hyperledger.fabric.shim.ledger.KeyModification;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;

public interface ChaincodeStub {

    /**
     * Returns the arguments corresponding to the call to
     * {@link Chaincode#init(ChaincodeStub)} or
     * {@link Chaincode#invoke(ChaincodeStub)}.
     *
     * @return a list of arguments
     */
    List<byte[]> getArgs();

    /**
     * Returns the arguments corresponding to the call to
     * {@link Chaincode#init(ChaincodeStub)} or
     * {@link Chaincode#invoke(ChaincodeStub)}.
     *
     * @return a list of arguments cast to UTF-8 strings
     */
    List<String> getStringArgs();

    /**
     * A convenience method that returns the first argument of the chaincode
     * invocation for use as a function name.
     * <p>
     * The bytes of the first argument are decoded as a UTF-8 string.
     *
     * @return the function name
     */
    String getFunction();

    /**
     * A convenience method that returns all except the first argument of the
     * chaincode invocation for use as the parameters to the function returned
     * by #{@link ChaincodeStub#getFunction()}.
     * <p>
     * The bytes of the arguments are decoded as a UTF-8 strings and returned as
     * a list of string parameters..
     *
     * @return a list of parameters
     */
    List<String> getParameters();

    /**
     * Returns the transaction id
     *
     * @return the transaction id
     */
    String getTxId();

    /**
     * Returns the channel id
     *
     * @return the channel id
     */
    String getChannelId();

    /**
     * Invoke another chaincode using the same transaction context.
     *
     * @param chaincodeName Name of chaincode to be invoked.
     * @param args          Arguments to pass on to the called chaincode.
     * @param channel       If not specified, the caller's channel is assumed.
     * @return
     */
    Response invokeChaincode(String chaincodeName, List<byte[]> args, String channel);

    /**
     * Returns the byte array value specified by the key, from the ledger.
     *
     * @param key name of the value
     * @return value the value read from the ledger
     */
    byte[] getState(String key);

    /**
     * Writes the specified value and key into the ledger
     *
     * @param key   name of the value
     * @param value the value to write to the ledger
     */
    void putState(String key, byte[] value);

    /**
     * Removes the specified key from the ledger
     *
     * @param key name of the value to be deleted
     */
    void delState(String key);

    /**
     * Returns all existing keys, and their values, that are lexicographically
     * between <code>startkey</code> (inclusive) and the <code>endKey</code>
     * (exclusive).
     *
     * @param startKey
     * @param endKey
     * @return an {@link Iterable} of {@link KeyValue}
     */
    QueryResultsIterator<KeyValue> getStateByRange(String startKey, String endKey);

    /**
     * Returns all existing keys, and their values, that are prefixed by the
     * specified partial {@link CompositeKey}.
     * <p>
     * If a full composite key is specified, it will not match itself, resulting
     * in no keys being returned.
     *
     * @param compositeKey partial composite key
     * @return an {@link Iterable} of {@link KeyValue}
     */
    QueryResultsIterator<KeyValue> getStateByPartialCompositeKey(String compositeKey);

    /**
     * Given a set of attributes, this method combines these attributes to
     * return a composite key.
     *
     * @param objectType
     * @param attributes
     * @return a composite key
     */
    CompositeKey createCompositeKey(String objectType, String... attributes);

    /**
     * Parses a composite key from a string.
     *
     * @param compositeKey a composite key string
     * @return a composite key
     */
    CompositeKey splitCompositeKey(String compositeKey);

    /**
     * Perform a rich query against the state database.
     *
     * @param query query string in a syntax supported by the underlying state
     *              database
     * @return
     * @throws UnsupportedOperationException if the underlying state database does not support rich
     *                                       queries.
     */
    QueryResultsIterator<KeyValue> getQueryResult(String query);

    /**
     * Returns the history of the specified key's values across time.
     *
     * @param key
     * @return an {@link Iterable} of {@link KeyModification}
     */
    QueryResultsIterator<KeyModification> getHistoryForKey(String key);

    /**
     * Returns the value of the specified `key` from the specified
     * `collection`. Note that GetPrivateData doesn't read data from the
     * private writeset, which has not been committed to the `collection`. In
     * other words, GetPrivateData doesn't consider data modified by PutPrivateData
     * that has not been committed.
     *
     * @param collection name of the collection
     * @param key        name of the value
     * @return value the value read from the collection
     */
    byte[] getPrivateData(String collection, String key);

    /**
     * Puts the specified `key` and `value` into the transaction's
     * private writeset. Note that only hash of the private writeset goes into the
     * transaction proposal response (which is sent to the client who issued the
     * transaction) and the actual private writeset gets temporarily stored in a
     * transient store. putPrivateData doesn't effect the `collection` until the
     * transaction is validated and successfully committed. Simple keys must not be
     * an empty string and must not start with null character (0x00), in order to
     * avoid range query collisions with composite keys, which internally get
     * prefixed with 0x00 as composite key namespace.
     *
     * @param collection name of the collection
     * @param key        name of the value
     * @param value      the value to write to the ledger
     */
    void putPrivateData(String collection, String key, byte[] value);

    /**
     * Records the specified `key` to be deleted in the private writeset of
     * the transaction. Note that only hash of the private writeset goes into the
     * transaction proposal response (which is sent to the client who issued the
     * transaction) and the actual private writeset gets temporarily stored in a
     * transient store. The `key` and its value will be deleted from the collection
     * when the transaction is validated and successfully committed.
     *
     * @param collection name of the collection
     * @param key        name of the value to be deleted
     */
    void delPrivateData(String collection, String key);

    /**
     * Returns all existing keys, and their values, that are lexicographically
     * between <code>startkey</code> (inclusive) and the <code>endKey</code>
     * (exclusive) in a given private collection.
     * Note that startKey and endKey can be empty string, which implies unbounded range
     * query on start or end.
     * The query is re-executed during validation phase to ensure result set
     * has not changed since transaction endorsement (phantom reads detected).
     *
     * @param collection name of the collection
     * @param startKey
     * @param endKey
     * @return an {@link Iterable} of {@link KeyValue}
     */
    QueryResultsIterator<KeyValue> getPrivateDataByRange(String collection, String startKey, String endKey);

    /**
     * Returns all existing keys, and their values, that are prefixed by the
     * specified partial {@link CompositeKey} in a given private collection.
     * <p>
     * If a full composite key is specified, it will not match itself, resulting
     * in no keys being returned.
     * <p>
     * The query is re-executed during validation phase to ensure result set
     * has not changed since transaction endorsement (phantom reads detected).
     *
     * @param collection   name of the collection
     * @param compositeKey partial composite key
     * @return an {@link Iterable} of {@link KeyValue}
     */
    QueryResultsIterator<KeyValue> getPrivateDataByPartialCompositeKey(String collection, String compositeKey);

    /**
     * Perform a rich query against a given private collection. It is only
     * supported for state databases that support rich query, e.g.CouchDB.
     * The query string is in the native syntax of the underlying state database.
     * An iterator is returned which can be used to iterate (next) over the query result set.
     * The query is NOT re-executed during validation phase, phantom reads are not detected.
     * That is, other committed transactions may have added, updated, or removed keys that
     * impact the result set, and this would not be detected at validation/commit time.
     * Applications susceptible to this should therefore not use GetQueryResult as part of
     * transactions that update ledger, and should limit use to read-only chaincode operations.
     *
     * @param collection name of the collection
     * @param query      query string in a syntax supported by the underlying state
     *                   database
     * @return
     * @throws UnsupportedOperationException if the underlying state database does not support rich
     *                                       queries.
     */
    QueryResultsIterator<KeyValue> getPrivateDataQueryResult(String collection, String query);

    /**
     * Defines the CHAINCODE type event that will be posted to interested
     * clients when the chaincode's result is committed to the ledger.
     *
     * @param name    Name of event. Cannot be null or empty string.
     * @param payload Optional event payload.
     */
    void setEvent(String name, byte[] payload);

    /**
     * Invoke another chaincode using the same transaction context.
     *
     * @param chaincodeName Name of chaincode to be invoked.
     * @param args          Arguments to pass on to the called chaincode.
     * @return
     */
    default Response invokeChaincode(String chaincodeName, List<byte[]> args) {
        return invokeChaincode(chaincodeName, args, null);
    }

    /**
     * Invoke another chaincode using the same transaction context.
     * <p>
     * This is a convenience version of
     * {@link #invokeChaincode(String, List, String)}. The string args will be
     * encoded into as UTF-8 bytes.
     *
     * @param chaincodeName Name of chaincode to be invoked.
     * @param args          Arguments to pass on to the called chaincode.
     * @param channel       If not specified, the caller's channel is assumed.
     * @return
     */
    default Response invokeChaincodeWithStringArgs(String chaincodeName, List<String> args, String channel) {
        return invokeChaincode(chaincodeName, args.stream().map(x -> x.getBytes(UTF_8)).collect(toList()), channel);
    }

    /**
     * Invoke another chaincode using the same transaction context.
     * <p>
     * This is a convenience version of {@link #invokeChaincode(String, List)}.
     * The string args will be encoded into as UTF-8 bytes.
     *
     * @param chaincodeName Name of chaincode to be invoked.
     * @param args          Arguments to pass on to the called chaincode.
     * @return
     */
    default Response invokeChaincodeWithStringArgs(String chaincodeName, List<String> args) {
        return invokeChaincodeWithStringArgs(chaincodeName, args, null);
    }

    /**
     * Invoke another chaincode using the same transaction context.
     * <p>
     * This is a convenience version of {@link #invokeChaincode(String, List)}.
     * The string args will be encoded into as UTF-8 bytes.
     *
     * @param chaincodeName Name of chaincode to be invoked.
     * @param args          Arguments to pass on to the called chaincode.
     * @return
     */
    default Response invokeChaincodeWithStringArgs(final String chaincodeName, final String... args) {
        return invokeChaincodeWithStringArgs(chaincodeName, Arrays.asList(args), null);
    }

    /**
     * Returns the byte array value specified by the key and decoded as a UTF-8
     * encoded string, from the ledger.
     *
     * @param key name of the value
     * @return value the value read from the ledger
     */
    default String getStringState(String key) {
        return new String(getState(key), UTF_8);
    }

    /**
     * Writes the specified value and key into the sidedb collection
     * value converted to byte array.
     *
     * @param collection collection name
     * @param key        name of the value
     * @param value      the value to write to the ledger
     */

    default void putPrivateData(String collection, String key, String value) {
        putPrivateData(collection, key, value.getBytes(UTF_8));
    }

    /**
     * Returns the byte array value specified by the key and decoded as a UTF-8
     * encoded string, from the sidedb collection.
     *
     * @param collection collection name
     * @param key        name of the value
     * @return value the value read from the ledger
     */
    default String getPrivateDataUTF8(String collection, String key) {
        return new String(getPrivateData(collection, key), UTF_8);
    }

    /**
     * Writes the specified value and key into the ledger
     *
     * @param key   name of the value
     * @param value the value to write to the ledger
     */
    default void putStringState(String key, String value) {
        putState(key, value.getBytes(UTF_8));
    }

    /**
     * Returns the CHAINCODE type event that will be posted to interested
     * clients when the chaincode's result is committed to the ledger.
     *
     * @return the chaincode event or null
     */
    ChaincodeEvent getEvent();

    /**
     * Returns the signed transaction proposal currently being executed.
     *
     * @return null if the current transaction is an internal call to a system
     * chaincode.
     */
    SignedProposal getSignedProposal();

    /**
     * Returns the timestamp when the transaction was created.
     *
     * @return timestamp as specified in the transaction's channel header.
     */
    Instant getTxTimestamp();

    /**
     * Returns the identity of the agent (or user) submitting the transaction.
     *
     * @return the bytes of the creator field of the proposal's signature
     * header.
     */
    byte[] getCreator();

    /**
     * Returns the transient map associated with the current transaction.
     *
     * @return
     */
    Map<String, byte[]> getTransient();

    /**
     * Returns the transaction binding.
     */
    byte[] getBinding();

}
