package com.example.simplewallet

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.math.BigInteger

data class AuthProofInput(
    @SerializedName("sk_i") val skI: BigInteger,
    @SerializedName("eventID") val eventID: BigInteger,
    @SerializedName("messageHash") val messageHash: BigInteger,
    @SerializedName("signatureR8x") val signatureR8x: BigInteger,
    @SerializedName("signatureR8y") val signatureR8y: BigInteger,
    @SerializedName("signatureS") val signatureS: BigInteger
) {
    constructor(
        skI: String,
        eventID: String,
        messageHash: String,
        signatureR8x: String,
        signatureR8y: String,
        signatureS: String
    ) : this(
        skI = BigInteger(skI),
        eventID = BigInteger(eventID),
        messageHash = BigInteger(messageHash),
        signatureR8x = BigInteger(signatureR8x),
        signatureR8y = BigInteger(signatureR8y),
        signatureS = BigInteger(signatureS)
    )

    fun toJson(): String {
        val gson = Gson()
        return gson.toJson(this.toMap())
    }

    private fun toMap(): Map<String, String> {
        return mapOf(
            "sk_i" to skI.toString(),
            "eventID" to eventID.toString(),
            "messageHash" to messageHash.toString(),
            "signatureR8x" to signatureR8x.toString(),
            "signatureR8y" to signatureR8y.toString(),
            "signatureS" to signatureS.toString()
        )
    }
}
