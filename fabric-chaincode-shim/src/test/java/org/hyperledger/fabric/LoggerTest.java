/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

// Testing deprecated class - hence warnings suppressed
public class LoggerTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

   
    @SuppressWarnings("deprecation")
	@Test
    public void testFormatError() {
        Exception e1 = new Exception("Computer says no");
        Logger l = Logger.getLogger("acme.wibble");
        assertThat(l.formatError(e1), containsString("Computer says no"));
        
        NullPointerException npe1 = new NullPointerException("Nothing here");
        npe1.initCause(e1);
        
        assertThat(l.formatError(npe1), containsString("Computer says no"));
        assertThat(l.formatError(npe1), containsString("Nothing here"));
        
        assertThat(l.formatError(null), CoreMatchers.nullValue());
    }
    
    @Test
    @SuppressWarnings("deprecation")
    public void testGetLogger() {
        Logger l = Logger.getLogger(this.getClass());
        l.error("It'll be fine");
        l.error(()-> "It'll be fine, honest");
        l.debug("Well maybe");
        l.debug(()->"Well no.");
    }
     
}
