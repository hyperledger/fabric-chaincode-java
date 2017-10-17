/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/

package org.hyperledger.fabric.shim.impl;

import org.hyperledger.fabric.protos.peer.ChaincodeShim.QueryResponse;
import org.hyperledger.fabric.protos.peer.ChaincodeShim.QueryResultBytes;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

class QueryResultsIteratorImpl<T> implements QueryResultsIterator<T> {

	private final Handler handler;
	private final String txId;
	private Iterator<QueryResultBytes> currentIterator;
	private QueryResponse currentQueryResponse;
	private Function<QueryResultBytes, T> mapper;

	public QueryResultsIteratorImpl(final Handler handler, final String txId, final QueryResponse queryResponse, Function<QueryResultBytes, T> mapper) {
		this.handler = handler;
		this.txId = txId;
		this.currentQueryResponse = queryResponse;
		this.currentIterator = currentQueryResponse.getResultsList().iterator();
		this.mapper = mapper;
	}

	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {

			@Override
			public boolean hasNext() {
				return currentIterator.hasNext() || currentQueryResponse.getHasMore();
			}

			@Override
			public T next() {

				// return next fetched result, if any
				if(currentIterator.hasNext()) return mapper.apply(currentIterator.next());

				// throw exception if there are no more expected results
				if(!currentQueryResponse.getHasMore()) throw new NoSuchElementException();

				// get more results from peer
				currentQueryResponse = handler.queryStateNext(txId, currentQueryResponse.getId());
				currentIterator = currentQueryResponse.getResultsList().iterator();

				// return next fetched result
				return mapper.apply(currentIterator.next());

			}

		};
	}

	@Override
	public void close() throws Exception {
		this.handler.queryStateClose(txId, currentQueryResponse.getId());
		this.currentIterator = Collections.emptyIterator();
		this.currentQueryResponse = QueryResponse.newBuilder().setHasMore(false).build();
	}

}
