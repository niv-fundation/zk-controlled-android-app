package com.example.simplewallet

import java.math.BigDecimal
import java.math.BigInteger

val Q = BigInteger("21888242871839275222246405745257275088696311157297823662689037894645226208583", 10)

fun validatePrivateKey(input: String): String? {
    if (input.isEmpty()) return "Private key cannot be empty"

    val bigInt: BigInteger? = try {
        if (input.startsWith("0x") || input.startsWith("0X")) {
            BigInteger(input.substring(2), 16)
        } else {
            BigInteger(input, 10)
        }
    } catch (e: NumberFormatException) {
        null
    }

    if (bigInt == null) {
        return "Invalid private key format"
    }

    val minKeySize = BigInteger.ONE.shiftLeft(127) // 2^127

    if (bigInt < minKeySize) {
        return "Private key is too short; must be at least 128 bits"
    }

    if (bigInt > Q) {
        return "Private key is too large"
    }

    return null
}

fun validateAmount(input: String): String? {
    if (input.isEmpty()) return "Amount cannot be empty"

    val amount: BigDecimal = try {
        BigDecimal(input)
    } catch (e: NumberFormatException) {
        return "Invalid amount format"
    }

    if (amount <= BigDecimal.ZERO) {
        return "Amount must be positive"
    }

    if (amount.scale() > 18) {
        return "Amount cannot have more than 18 decimal places"
    }

    return null
}

fun validateRecipientAddress(input: String): String? {
    if (input.isEmpty()) return "Recipient address cannot be empty"

    var address = input
    if (address.startsWith("0x") || address.startsWith("0X")) {
        address = address.substring(2)
    }

    if (address.length != 40) {
        return "Recipient address must be 40 hex characters (20 bytes)"
    }

    val isHex = address.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }
    if (!isHex) {
        return "Recipient address contains invalid characters"
    }

    return null
}
