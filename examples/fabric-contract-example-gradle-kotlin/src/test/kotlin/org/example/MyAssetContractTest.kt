/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.example

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import com.nhaarman.mockitokotlin2.*

import java.util.ArrayList

import org.hyperledger.fabric.contract.Context
import org.hyperledger.fabric.shim.ChaincodeStub
import org.hyperledger.fabric.shim.Chaincode.Response
import org.hyperledger.fabric.shim.Chaincode.Response.Status

class MyAssetContractTest {

    lateinit var ctx: Context
    lateinit var stub: ChaincodeStub

    @BeforeEach
    fun beforeEach() {
        ctx = mock()
        stub = mock()
        whenever(ctx.stub).thenReturn(stub)
        whenever(stub.getState("1001")).thenReturn("{\"value\":\"my asset 1001 value\"}".toByteArray(Charsets.UTF_8))
        whenever(stub.getState("1002")).thenReturn("{\"value\":\"my asset 1002 value\"}".toByteArray(Charsets.UTF_8))
    }

    @Nested
    inner class myAssetExists {

        @Test
        fun `should return true for a my asset`() {
            val contract = MyAssetContract()
            val result = contract.myAssetExists(ctx, "1001")
            assertTrue(result)
        }

        @Test
        fun `should return false for a my asset that does not exist (no key)`() {
            val contract = MyAssetContract()
            val result = contract.myAssetExists(ctx, "1003")
            assertFalse(result)
        }

        @Test
        fun `should return false for a my asset that does not exist (no data)`() {
            val contract = MyAssetContract()
            whenever(stub.getState("1003")).thenReturn(ByteArray(0))
            val result = contract.myAssetExists(ctx, "1003")
            assertFalse(result)
        }

    }

    @Nested
    inner class createMyAsset {

        @Test
        fun `should create a my asset`() {
            val contract = MyAssetContract()
            contract.createMyAsset(ctx, "1003", "my asset 1003 value")
            verify(stub, times(1)).putState("1003", "{\"value\":\"my asset 1003 value\"}".toByteArray(Charsets.UTF_8))
        }

        @Test
        fun `should throw an error for a my asset that already exists`() {
            val contract = MyAssetContract()
            val e = assertThrows(RuntimeException::class.java) { contract.createMyAsset(ctx, "1001", "my asset 1001 value") }
            assertEquals(e.message, "The my asset 1001 already exists")
        }

    }

    @Nested
    inner class readMyAsset {

        @Test
        fun `should return a my asset`() {
            val contract = MyAssetContract()
            val asset = contract.readMyAsset(ctx, "1001")
            assertEquals("my asset 1001 value", asset.value)
        }

        @Test
        fun `should throw an error for a my asset that does not exist`() {
            val contract = MyAssetContract()
            val e = assertThrows(RuntimeException::class.java) { contract.readMyAsset(ctx, "1003") }
            assertEquals(e.message, "The my asset 1003 does not exist")
        }

    }

    @Nested
    inner class updateMyAsset {

        @Test
        fun `should update a my asset`() {
            val contract = MyAssetContract()
            contract.updateMyAsset(ctx, "1001", "my asset 1001 new value")
            verify(stub, times(1)).putState("1001", "{\"value\":\"my asset 1001 new value\"}".toByteArray(Charsets.UTF_8))
        }

        @Test
        fun `should throw an error for a my asset that does not exist`() {
            val contract = MyAssetContract()
            val e = assertThrows(RuntimeException::class.java) { contract.updateMyAsset(ctx, "1003", "my asset 1003 new value") }
            assertEquals(e.message, "The my asset 1003 does not exist")
        }

    }

    @Nested
    inner class deleteMyAsset {

        @Test
        fun `should delete a my asset`() {
            val contract = MyAssetContract()
            contract.deleteMyAsset(ctx, "1001")
            verify(stub, times(1)).delState("1001")
        }

        @Test
        fun `should throw an error for a my asset that does not exist`() {
            val contract = MyAssetContract()
            val e = assertThrows(RuntimeException::class.java) { contract.deleteMyAsset(ctx, "1003") }
            assertEquals(e.message, "The my asset 1003 does not exist")
        }

    }

}
