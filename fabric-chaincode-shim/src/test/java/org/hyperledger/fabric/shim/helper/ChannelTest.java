/*
Copyright IBM Corp. All Rights Reserved.

SPDX-License-Identifier: Apache-2.0
*/
package org.hyperledger.fabric.shim.helper;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

public class ChannelTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    Channel<Integer> testChannel = new Channel<>();

    @Test
    public void testChannel() throws InterruptedException {
        testChannel.clear();
        testChannel.add(1);
        testChannel.add(2);
        assertEquals("Wrong item come out the channel", (long) 1, (long) testChannel.take());
        testChannel.close();
        thrown.expect(InterruptedException.class);
        testChannel.take();
        thrown.expect(IllegalStateException.class);
        testChannel.add(1);
    }

}