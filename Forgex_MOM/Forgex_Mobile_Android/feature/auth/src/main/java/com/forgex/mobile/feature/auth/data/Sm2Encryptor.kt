package com.forgex.mobile.feature.auth.data

import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import org.bouncycastle.crypto.engines.SM2Engine
import org.bouncycastle.crypto.params.ECPublicKeyParameters
import org.bouncycastle.crypto.params.ParametersWithRandom
import org.bouncycastle.crypto.util.PublicKeyFactory
import org.bouncycastle.util.encoders.Hex

object Sm2Encryptor {

    private val secureRandom = SecureRandom()

    /**
     * Encrypt plaintext to hex string, compatible with backend Hutool SM2 decrypt.
     */
    fun encryptToHex(plainText: String, publicKey: String): String? {
        return runCatching {
            val publicKeyBytes = decodePublicKey(publicKey)
            val publicKeyParams = PublicKeyFactory.createKey(publicKeyBytes) as ECPublicKeyParameters
            val engine = SM2Engine(SM2Engine.Mode.C1C3C2)
            engine.init(true, ParametersWithRandom(publicKeyParams, secureRandom))
            val plainBytes = plainText.toByteArray(StandardCharsets.UTF_8)
            val cipherBytes = engine.processBlock(
                plainBytes,
                0,
                plainBytes.size
            )
            Hex.toHexString(cipherBytes)
        }.getOrNull()
    }

    private fun decodePublicKey(raw: String): ByteArray {
        val normalized = raw
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\\s".toRegex(), "")
        return Base64.decode(normalized, Base64.DEFAULT)
    }
}
