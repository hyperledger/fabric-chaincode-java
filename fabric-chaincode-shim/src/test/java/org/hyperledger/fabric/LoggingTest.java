/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;

public final class LoggingTest {
    @Test
    public void testMapLevel() {

        assertEquals(Level.SEVERE, proxyMapLevel("ERROR"), "Error maps");
        assertEquals(Level.SEVERE, proxyMapLevel("critical"), "Critical maps");
        assertEquals(Level.WARNING, proxyMapLevel("WARNING"), "Warn maps");
        assertEquals(Level.INFO, proxyMapLevel("INFO"), "Info maps");
        assertEquals(Level.CONFIG, proxyMapLevel(" notice"), "Config maps");
        assertEquals(Level.INFO, proxyMapLevel(" info"), "Info maps");
        assertEquals(Level.FINEST, proxyMapLevel("debug          "), "Debug maps");
        assertEquals(Level.INFO, proxyMapLevel("wibble          "), "Info maps");
        assertEquals(Level.INFO, proxyMapLevel(new Object[] {null}), "Info maps");
    }

    public Object proxyMapLevel(final Object... args) {

        try {
            final Method m = Logging.class.getDeclaredMethod("mapLevel", String.class);
            m.setAccessible(true);
            return m.invoke(null, args);
        } catch (NoSuchMethodException
                | SecurityException
                | IllegalAccessException
                | IllegalArgumentException
                | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testFormatError() {
        final Exception e1 = new Exception("Computer says no");

        assertThat(Logging.formatError(e1), containsString("Computer says no"));

        final NullPointerException npe1 = new NullPointerException("Nothing here");
        npe1.initCause(e1);

        assertThat(Logging.formatError(npe1), containsString("Computer says no"));
        assertThat(Logging.formatError(npe1), containsString("Nothing here"));

        assertThat(Logging.formatError(null), CoreMatchers.nullValue());
    }

    @Test
    public void testSetLogLevel() {

        final java.util.logging.Logger l = java.util.logging.Logger.getLogger("org.hyperledger.fabric.test");
        final java.util.logging.Logger another = java.util.logging.Logger.getLogger("acme.wibble");

        final Level anotherLevel = another.getLevel();
        Logging.setLogLevel("debug");
        assertThat(l.getLevel(), CoreMatchers.equalTo(Level.FINEST));
        assertThat(another.getLevel(), CoreMatchers.equalTo(anotherLevel));

        Logging.setLogLevel("dsomethoig");
        assertThat(l.getLevel(), CoreMatchers.equalTo(Level.INFO));
        assertThat(another.getLevel(), CoreMatchers.equalTo(anotherLevel));

        Logging.setLogLevel("ERROR");
        assertThat(l.getLevel(), CoreMatchers.equalTo(Level.SEVERE));
        assertThat(another.getLevel(), CoreMatchers.equalTo(anotherLevel));

        Logging.setLogLevel("debug");
    }
}
