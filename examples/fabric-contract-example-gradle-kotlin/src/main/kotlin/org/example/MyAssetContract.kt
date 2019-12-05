/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.example

import org.hyperledger.fabric.contract.Context
import org.hyperledger.fabric.contract.ContractInterface
import org.hyperledger.fabric.contract.annotation.Contact
import org.hyperledger.fabric.contract.annotation.Contract
import org.hyperledger.fabric.contract.annotation.Default
import org.hyperledger.fabric.contract.annotation.Info
import org.hyperledger.fabric.contract.annotation.License
import org.hyperledger.fabric.contract.annotation.Transaction

@Contract(name = "MyAssetContract",
    info = Info(title = "MyAsset contract",
                description = "Kotlin gradle dsl and Kotlin Contract",
                version = "0.0.1",
                license =
                        License(name = "Apache-2.0",
                                url = ""),
                                contact = Contact(email = "gradle-kotlin@example.com",
                                                  name = "gradle-kotlin",
                                                  url = "http://gradle-kotlin.me")))
@Default
class MyAssetContract : ContractInterface {

    @Transaction
    fun myAssetExists(ctx: Context, myAssetId: String): Boolean {
        val buffer = ctx.stub.getState(myAssetId)
        return (buffer != null && buffer.size > 0)
    }

    @Transaction
    fun createMyAsset(ctx: Context, myAssetId: String, value: String) {
        val exists = myAssetExists(ctx, myAssetId)
        if (exists) {
            throw RuntimeException("The my asset $myAssetId already exists")
        }
        val asset = MyAsset(value)
        ctx.stub.putState(myAssetId, asset.toJSONString().toByteArray(Charsets.UTF_8))
    }

    @Transaction
    fun readMyAsset(ctx: Context, myAssetId: String): MyAsset {
        val exists = myAssetExists(ctx, myAssetId)
        if (!exists) {
            throw RuntimeException("The my asset $myAssetId does not exist")
        }
        return MyAsset.fromJSONString(ctx.stub.getState(myAssetId).toString(Charsets.UTF_8))
    }

    @Transaction
    fun updateMyAsset(ctx: Context, myAssetId: String, newValue: String) {
        val asset = readMyAsset(ctx, myAssetId)
        asset.value = newValue
        ctx.stub.putState(myAssetId, asset.toJSONString().toByteArray(Charsets.UTF_8))
    }

    @Transaction
    fun deleteMyAsset(ctx: Context, myAssetId: String) {
        val exists = myAssetExists(ctx, myAssetId)
        if (!exists) {
            throw RuntimeException("The my asset $myAssetId does not exist")
        }
        ctx.stub.delState(myAssetId)
    }

}
