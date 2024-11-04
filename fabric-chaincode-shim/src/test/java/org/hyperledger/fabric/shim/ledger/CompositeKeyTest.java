/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.shim.ledger;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

final class CompositeKeyTest {
    @Test
    void testValidateSimpleKeys() {
        CompositeKey.validateSimpleKeys("abc", "def", "ghi");
    }

    @Test
    void testValidateSimpleKeysException() {
        assertThatThrownBy(() -> CompositeKey.validateSimpleKeys("\u0000abc"))
                .isInstanceOf(CompositeKeyFormatException.class);
    }

    @Test
    void testCompositeKeyStringStringArray() {
        final CompositeKey key = new CompositeKey("abc", "def", "ghi", "jkl", "mno");
        assertThat(key.getObjectType(), is(equalTo("abc")));
        assertThat(key.getAttributes(), hasSize(4));
        assertThat(key.toString(), is(equalTo("\u0000abc\u0000def\u0000ghi\u0000jkl\u0000mno\u0000")));
    }

    @Test
    void testCompositeKeyStringListOfString() {
        final CompositeKey key = new CompositeKey("abc", Arrays.asList("def", "ghi", "jkl", "mno"));
        assertThat(key.getObjectType(), is(equalTo("abc")));
        assertThat(key.getAttributes(), hasSize(4));
        assertThat(key.toString(), is(equalTo("\u0000abc\u0000def\u0000ghi\u0000jkl\u0000mno\u0000")));
    }

    @Test
    void testEmptyAttributes() {
        final CompositeKey key = new CompositeKey("abc");
        assertThat(key.getObjectType(), is(equalTo("abc")));
        assertThat(key.getAttributes(), hasSize(0));
        assertThat(key.toString(), is(equalTo("\u0000abc\u0000")));
    }

    @Test
    void testCompositeKeyWithInvalidObjectTypeDelimiter() {
        assertThatThrownBy(() -> new CompositeKey("ab\u0000c", Arrays.asList("def", "ghi", "jkl", "mno")))
                .isInstanceOf(CompositeKeyFormatException.class);
    }

    @Test
    void testCompositeKeyWithInvalidAttributeDelimiter() {
        assertThatThrownBy(() -> new CompositeKey("abc", Arrays.asList("def", "ghi", "j\u0000kl", "mno")))
                .isInstanceOf(CompositeKeyFormatException.class);
    }

    @Test
    void testCompositeKeyWithInvalidObjectTypeMaxCodePoint() {
        assertThatThrownBy(() -> new CompositeKey("ab\udbff\udfffc", Arrays.asList("def", "ghi", "jkl", "mno")))
                .isInstanceOf(CompositeKeyFormatException.class);
    }

    @Test
    void testCompositeKeyWithInvalidAttributeMaxCodePoint() {
        assertThatThrownBy(() -> new CompositeKey("abc", Arrays.asList("def", "ghi", "jk\udbff\udfffl", "mno")))
                .isInstanceOf(CompositeKeyFormatException.class);
    }

    @Test
    void testGetObjectType() {
        final CompositeKey key = new CompositeKey("abc", Arrays.asList("def", "ghi", "jkl", "mno"));
        assertThat(key.getObjectType(), is(equalTo("abc")));
    }

    @Test
    void testGetAttributes() {
        final CompositeKey key = new CompositeKey("abc", Arrays.asList("def", "ghi", "jkl", "mno"));
        assertThat(key.getObjectType(), is(equalTo("abc")));
        assertThat(key.getAttributes(), hasSize(4));
        assertThat(key.getAttributes(), contains("def", "ghi", "jkl", "mno"));
    }

    @Test
    void testToString() {
        final CompositeKey key = new CompositeKey("abc", Arrays.asList("def", "ghi", "jkl", "mno"));
        assertThat(key.toString(), is(equalTo("\u0000abc\u0000def\u0000ghi\u0000jkl\u0000mno\u0000")));
    }

    @Test
    void testParseCompositeKey() {
        final CompositeKey key = CompositeKey.parseCompositeKey("\u0000abc\u0000def\u0000ghi\u0000jkl\u0000mno\u0000");
        assertThat(key.getObjectType(), is(equalTo("abc")));
        assertThat(key.getAttributes(), hasSize(4));
        assertThat(key.getAttributes(), contains("def", "ghi", "jkl", "mno"));
        assertThat(key.toString(), is(equalTo("\u0000abc\u0000def\u0000ghi\u0000jkl\u0000mno\u0000")));
    }

    @Test
    void testParseCompositeKeyInvalidObjectType() {
        assertThatThrownBy(() ->
                        CompositeKey.parseCompositeKey("ab\udbff\udfffc\u0000def\u0000ghi\u0000jkl\u0000mno\u0000"))
                .isInstanceOf(CompositeKeyFormatException.class);
    }

    @Test
    void testParseCompositeKeyInvalidAttribute() {
        assertThatThrownBy(() ->
                        CompositeKey.parseCompositeKey("abc\u0000def\u0000ghi\u0000jk\udbff\udfffl\u0000mno\u0000"))
                .isInstanceOf(CompositeKeyFormatException.class);
    }
}
