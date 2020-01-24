/*
 * Copyright 2019 IBM All Rights Reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;

import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public final class LoggingTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testMapLevel() {

        assertEquals("Error maps", Level.SEVERE, proxyMapLevel("ERROR"));
        assertEquals("Critical maps", Level.SEVERE, proxyMapLevel("critical"));
        assertEquals("Warn maps", Level.WARNING, proxyMapLevel("WARNING"));
        assertEquals("Info maps", Level.INFO, proxyMapLevel("INFO"));
        assertEquals("Config maps", Level.CONFIG, proxyMapLevel(" notice"));
        assertEquals("Info maps", Level.INFO, proxyMapLevel(" info"));
        assertEquals("Debug maps", Level.FINEST, proxyMapLevel("debug          "));
        assertEquals("Info maps", Level.INFO, proxyMapLevel("wibble          "));
        assertEquals("Info maps", Level.INFO, proxyMapLevel(new Object[] {null}));
    }

    public Object proxyMapLevel(final Object... args) {

        try {
            final Method m = Logging.class.getDeclaredMethod("mapLevel", String.class);
            m.setAccessible(true);
            return m.invoke(null, args);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
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

        Logging.setLogLevel("INFO");
    }
}
