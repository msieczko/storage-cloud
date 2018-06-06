package pl.edu.pw.elka.llepak.tinbox.utils

import java.io.ByteArrayOutputStream

object EncodeUtils {
    fun caesarEncode(input: ByteArray): ByteArray {
        val toRet = ByteArrayOutputStream()
        input.forEach { x -> toRet.write(x + 1) }
        return toRet.toByteArray()
    }
    fun caesarDecode(input: ByteArray):ByteArray {
        val toRet = ByteArrayOutputStream()
        input.forEach { x -> toRet.write(x - 1) }
        return toRet.toByteArray()
    }
}