package com.example.clicknote.util

import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

fun calculateChecksum(file: File): String {
    val digest = MessageDigest.getInstance("SHA-256")
    FileInputStream(file).use { fis ->
        val buffer = ByteArray(8192)
        var bytesRead: Int
        while (fis.read(buffer).also { bytesRead = it } != -1) {
            digest.update(buffer, 0, bytesRead)
        }
    }
    return digest.digest().joinToString("") { "%02x".format(it) }
}

fun verifyChecksum(file: File, expectedChecksum: String): Boolean {
    return calculateChecksum(file) == expectedChecksum
} 