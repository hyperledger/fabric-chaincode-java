/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.shim.ledger;

final class CompositeKeyFormatException extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;

    private CompositeKeyFormatException(final String s) {
        super(s);
    }

    static CompositeKeyFormatException forInputString(final String s, final String group, final int index) {
        return new CompositeKeyFormatException(String.format("For input string '%s', found 'U+%06X' at index %d.", s, group.codePointAt(0), index));
    }

    static CompositeKeyFormatException forSimpleKey(final String key) {
        return new CompositeKeyFormatException(
                String.format("First character of the key [%s] contains a 'U+%06X' which is not allowed", key, CompositeKey.NAMESPACE.codePointAt(0)));
    }
}
