package com.example.simplewallet

import com.google.gson.Gson

enum class SelectedNavigationElement {
    Home,
    Send,
    Settings
}

data class Proof(
    val pi_a: List<String>,
    val pi_b: List<List<String>>,
    val pi_c: List<String>,
    val protocol: String,
) {
    fun toJson(): String {
        val gson = Gson()
        return gson.toJson(this)
    }

    companion object {
        fun fromJson(jsonString: String): Proof {
            val json = Gson().fromJson(jsonString, Proof::class.java)
            return json
        }
    }

}

data class ZkProof(
    val proof: Proof,
    val pub_signals: List<String>
)