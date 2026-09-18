package com.forgex.mobile.feature.auth.data

import org.bouncycastle.crypto.engines.SM2Engine
import org.bouncycastle.crypto.params.ECPrivateKeyParameters
import org.bouncycastle.crypto.util.PrivateKeyFactory
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.util.encoders.Hex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.BeforeClass
import org.junit.Test
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.spec.ECGenParameterSpec
import java.util.Base64

class Sm2EncryptorTest {

    companion object {
        @JvmStatic
        @BeforeClass
        fun registerBouncyCastle() {
            if (java.security.Security.getProvider("BC") == null) {
                java.security.Security.addProvider(BouncyCastleProvider())
            }
        }
    }

    @Test
    fun encryptToHexProducesC1C3C2CiphertextThatCanBeDecrypted() {
        val keyPairGenerator = KeyPairGenerator.getInstance("EC", "BC")
        keyPairGenerator.initialize(ECGenParameterSpec("sm2p256v1"), SecureRandom())
        val keyPair = keyPairGenerator.generateKeyPair()
        val publicKey = Base64.getEncoder().encodeToString(keyPair.public.encoded)

        val encrypted = Sm2Encryptor.encryptToHex("forgex-sm2-round-trip", publicKey)

        assertNotNull(encrypted)
        val privateKey = PrivateKeyFactory.createKey(keyPair.private.encoded) as ECPrivateKeyParameters
        val engine = SM2Engine(SM2Engine.Mode.C1C3C2)
        engine.init(false, privateKey)
        val decrypted = engine.processBlock(Hex.decode(encrypted), 0, Hex.decode(encrypted).size)

        assertEquals("forgex-sm2-round-trip", String(decrypted))
    }
}
