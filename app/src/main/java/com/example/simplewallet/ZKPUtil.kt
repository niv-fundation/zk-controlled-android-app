package com.example.simplewallet

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import com.example.simplewallet.ZKPUtil.groth16InternalStorage
import com.google.gson.Gson
import java.io.ByteArrayOutputStream

object ZKPUtil {
    external fun groth16ProverBig(
        fileName: String,
        wtnsBuffer: ByteArray,
        wtnsSize: Long,
        proofBuffer: ByteArray,
        proofSize: LongArray,
        publicBuffer: ByteArray,
        publicSize: LongArray,
        errorMsg: ByteArray,
        errorMsgMaxSize: Long,
        assetManager: AssetManager
    ): Int


    external fun groth16InternalStorage(
        fileName: String,
        fileLength: Long,
        wtnsBuffer: ByteArray,
        wtnsSize: Long,
        proofBuffer: ByteArray,
        proofSize: LongArray,
        publicBuffer: ByteArray,
        publicSize: LongArray,
        errorMsg: ByteArray,
        errorMsgMaxSize: Long,
    ): Int

    external fun auth(
        circuitBuffer: ByteArray,
        circuitSize: Long,
        jsonBuffer: ByteArray,
        jsonSize: Long,
        wtnsBuffer: ByteArray,
        wtnsSize: LongArray,
        errorMsg: ByteArray,
        errorMsgMaxSize: Long
    ): Int

    init {
        System.loadLibrary("simple")
    }
}

class ZKPUseCase(val context: Context, val assetManager: AssetManager) {
    fun generateRegisterZKP(
        zkeyFilePath: String,
        zkeyFileLen: Long,
        datFilePath: String,
        datFileLen: Long,
        inputs: ByteArray,
        proofFunction: (
            datFilePath: String, datFileLen: Long, jsonBuffer: ByteArray, jsonSize: Long, wtnsBuffer: ByteArray, wtnsSize: LongArray, errorMsg: ByteArray, errorMsgMaxSize: Long
        ) -> Int
    ): ZkProof {


        val msg = ByteArray(256)

        val witnessLen = LongArray(1)
        witnessLen[0] = 100 * 1024 * 1024

        val byteArr = ByteArray(witnessLen[0].toInt())

        val res = proofFunction(
            datFilePath, datFileLen, inputs, inputs.size.toLong(), byteArr, witnessLen, msg, 256
        )

        if (res == -2) {
            throw Exception("file reading failure")
        }

        if (res == 2) {
            throw Exception("Not enough memory for zkp")
        }

        if (res == 1) {
            throw Exception("Error during zkp ${msg.decodeToString()}")
        }

        val pubData = ByteArray(2 * 1024 * 1024)


        val pubLen = LongArray(1)
        pubLen[0] = pubData.size.toLong()

        val proofData = ByteArray(2 * 1024 * 1024)
        val proofLen = LongArray(1)
        proofLen[0] = proofData.size.toLong()

        val witnessData = byteArr.copyOfRange(0, witnessLen[0].toInt())

        Log.i("witnessDataLen", witnessData.size.toString())
        Log.i("proofDataLen", witnessLen[0].toInt().toString())

        val verification = groth16InternalStorage(
            zkeyFilePath,
            zkeyFileLen,
            witnessData,
            witnessLen[0],
            proofData,
            proofLen,
            pubData,
            pubLen,
            msg,
            256
        )

        if (verification == -2) {
            throw Exception("Error during zkp: file reading failure")
        }

        if (verification == -1) {
            throw Exception("Error during zkp: file opening failure")
        }

        if (verification == 2) {
            throw Exception("Not enough memory for verification ${msg.decodeToString()}")
        }

        if (verification == 1) {
            throw Exception("Error during verification ${msg.decodeToString()}")
        }


        val proofDataZip = proofData.copyOfRange(0, proofLen[0].toInt())

        val index = findLastIndexOfSubstring(
            proofDataZip.toString(Charsets.UTF_8), "\"protocol\":\"groth16\"}"
        )
        val indexPubData = findLastIndexOfSubstring(
            pubData.decodeToString(), "]"
        )

        val formatedPubData = pubData.decodeToString().slice(0..indexPubData)

        val foramtedProof = proofDataZip.toString(Charsets.UTF_8).slice(0..index)
        val proof = Proof.fromJson(foramtedProof)

        return ZkProof(
            proof = proof, pub_signals = getPubSignals(formatedPubData).toList()
        )
    }

    fun generateZKP(
        zkeyFileName: String, datFile: Int, inputs: ByteArray, proofFunction: (
            circuitBuffer: ByteArray, circuitSize: Long, jsonBuffer: ByteArray, jsonSize: Long, wtnsBuffer: ByteArray, wtnsSize: LongArray, errorMsg: ByteArray, errorMsgMaxSize: Long
        ) -> Int
    ): ZkProof {
        val datFile = openRawResourceAsByteArray(datFile)

        val msg = ByteArray(256)

        val witnessLen = LongArray(1)
        witnessLen[0] = 100 * 1024 * 1024

        val byteArr = ByteArray(100 * 1024 * 1024)

        val res = proofFunction(
            datFile,
            datFile.size.toLong(),
            inputs,
            inputs.size.toLong(),
            byteArr,
            witnessLen,
            msg,
            256
        )

        if (res == 2) {
            throw Exception("Not enough memory for zkp")
        }

        if (res == 1) {
            throw Exception("Error during zkp ${msg.decodeToString()}")
        }

        val pubData = ByteArray(2 * 1024 * 1024)


        val pubLen = LongArray(1)
        pubLen[0] = pubData.size.toLong()

        val proofData = ByteArray(4 * 1024 * 1024)
        val proofLen = LongArray(1)
        proofLen[0] = proofData.size.toLong()

        val witnessData = byteArr.copyOfRange(0, witnessLen[0].toInt())

        val verification = ZKPUtil.groth16ProverBig(
            zkeyFileName,
            witnessData,
            witnessLen[0],
            proofData,
            proofLen,
            pubData,
            pubLen,
            msg,
            256,
            assetManager = assetManager
        )

        if (verification == -2) {
            throw Exception("Error during zkp: Cant find file")
        }

        if (verification == 2) {
            throw Exception("Not enough memory for verification ${msg.decodeToString()}")
        }

        if (verification == 1) {
            throw Exception("Error during verification ${msg.decodeToString()}")
        }

        val proofDataZip = proofData.copyOfRange(0, proofLen[0].toInt())

        val index = findLastIndexOfSubstring(
            proofDataZip.toString(Charsets.UTF_8), "\"protocol\":\"groth16\"}"
        )
        val indexPubData = findLastIndexOfSubstring(
            pubData.decodeToString(), "]"
        )

        val formatedPubData = pubData.decodeToString().slice(0..indexPubData)

        val foramtedProof = proofDataZip.toString(Charsets.UTF_8).slice(0..index)
        val proof = Proof.fromJson(foramtedProof)

        return ZkProof(
            proof = proof, pub_signals = getPubSignals(formatedPubData).toList()
        )
    }

    private fun openRawResourceAsByteArray(resourceName: Int): ByteArray {
        val inputStream = context.resources.openRawResource(resourceName)
        val byteArrayOutputStream = ByteArrayOutputStream()

        try {
            val buffer = ByteArray(1024)
            var length: Int

            while (inputStream.read(buffer).also { length = it } != -1) {
                byteArrayOutputStream.write(buffer, 0, length)
            }

            return byteArrayOutputStream.toByteArray()
        } finally {
            // Close the streams in a finally block to ensure they are closed even if an exception occurs
            byteArrayOutputStream.close()
            inputStream.close()
        }
    }

    private fun findLastIndexOfSubstring(mainString: String, searchString: String): Int {
        val index = mainString.lastIndexOf(searchString)

        if (index != -1) {
            // If substring is found, calculate the last index of the substring
            return index + searchString.length - 1
        }
        return -1
    }

    private fun getPubSignals(jsonString: String): List<String> {
        val gson = Gson()
        val stringArray = gson.fromJson(jsonString, Array<String>::class.java)
        return stringArray.toList()
    }
}