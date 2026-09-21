package com.forgex.mobile.feature.auth.data

import org.bouncycastle.crypto.engines.SM2Engine
import org.bouncycastle.crypto.params.ECPrivateKeyParameters
import org.bouncycastle.crypto.util.PrivateKeyFactory
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.util.encoders.Hex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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

    /**
     * 用数据库初始化脚本 sys_config(security.crypto.transport) 中的真实密钥对验证：
     * 密文 hex 能被后端同款 BC C1C3C2 引擎解开，且 C1 带 0x04 未压缩点前缀。
     */
    @Test
    fun cipherMatchesSeedKeyPairFromBackendConfig() {
        val seedPublicKey =
            "MFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAEg6UWG2xcLWEYBtsE3aZmC+0HDQGq21zcG6vwoFdXmc6Arci/E1wfiHHSoLNlePS9P3cv6tHLefaSflJt0B9g+A=="
        val seedPrivateKey =
            "MIGTAgEAMBMGByqGSM49AgEGCCqBHM9VAYItBHkwdwIBAQQg36lE/DeNqtLaXTnnhFXcUhTy6Y9R8M/NqEfRDE8aQaGgCgYIKoEcz1UBgi2hRANCAASDpRYbbFwtYRgG2wTdpmYL7QcNAarbXNwbq/CgV1eZzoCtyL8TXB+IcdKgs2V49L0/dy/q0ct59pJ+Um3QH2D4"
        val plain = "Aa123456"

        val encrypted = Sm2Encryptor.encryptToHex(plain, seedPublicKey)
        assertNotNull(encrypted)

        val cipher = Hex.decode(encrypted)
        assertEquals(4, cipher[0].toInt())
        assertEquals(65 + 32 + plain.toByteArray().size, cipher.size)

        val privateKey = PrivateKeyFactory.createKey(Base64.getDecoder().decode(seedPrivateKey)) as ECPrivateKeyParameters
        val engine = SM2Engine(SM2Engine.Mode.C1C3C2)
        engine.init(false, privateKey)
        val decrypted = engine.processBlock(cipher, 0, cipher.size)

        assertEquals(plain, String(decrypted))
    }

    @Test
    fun returnsNullForInvalidPublicKey() {
        assertNull(Sm2Encryptor.encryptToHex("Aa123456", "not-a-key"))
    }
}
