package pl.edu.pw.elka.llepak.tinbox.utils

import java.security.MessageDigest

object HashUtils {
    fun sha512(input: ByteArray) = hashString("SHA-512", input)

    fun sha256(input: ByteArray) = hashString("SHA-256", input)

    fun sha1(input: ByteArray) = hashString("SHA-1", input)

    private fun hashString(type: String, input: ByteArray): ByteArray {
        return MessageDigest.getInstance(type).digest(input)
    }
}