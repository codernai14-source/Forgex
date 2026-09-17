package com.forgex.auth.mfa;

import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.time.Instant;

/**
 * RFC 6238 TOTP 实现。
 * <p>
 * 使用 HMAC-SHA1 与 30 秒时间步，校验时允许前后各一个时间窗。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Component
public class TotpService {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

    private final SecureRandom random = new SecureRandom();

    /**
     * 生成 Base32 编码的 160 位密钥。
     *
     * @return Base32 密钥
     */
    public String generateSecret() {
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        return encode(bytes);
    }

    /**
     * 校验 6 位动态码。
     *
     * @param secret Base32 密钥
     * @param code   用户输入的动态码
     * @return true 表示通过
     */
    public boolean verify(String secret, String code) {
        if (secret == null || code == null || !code.matches("\\d{6}")) {
            return false;
        }
        long counter = Instant.now().getEpochSecond() / 30;
        for (long offset = -1; offset <= 1; offset++) {
            if (code.equals(generateCode(secret, counter + offset))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 按计数器生成动态码。
     *
     * @param secret  密钥
     * @param counter 时间步计数器
     * @return 6 位数字
     * @throws IllegalArgumentException 密钥非法时抛出
     */
    public String generateCode(String secret, long counter) {
        try {
            byte[] key = decode(secret);
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key, "HmacSHA1"));
            byte[] hash = mac.doFinal(ByteBuffer.allocate(8).putLong(counter).array());
            int index = hash[hash.length - 1] & 0x0f;
            int binary = ((hash[index] & 0x7f) << 24)
                    | ((hash[index + 1] & 0xff) << 16)
                    | ((hash[index + 2] & 0xff) << 8)
                    | (hash[index + 3] & 0xff);
            return String.format("%06d", binary % 1_000_000);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid TOTP secret", ex);
        }
    }

    private static String encode(byte[] bytes) {
        StringBuilder out = new StringBuilder();
        int buffer = 0;
        int bits = 0;
        for (byte b : bytes) {
            buffer = (buffer << 8) | (b & 0xff);
            bits += 8;
            while (bits >= 5) {
                bits -= 5;
                out.append(ALPHABET.charAt((buffer >> bits) & 31));
            }
        }
        if (bits > 0) {
            out.append(ALPHABET.charAt((buffer << (5 - bits)) & 31));
        }
        return out.toString();
    }

    private static byte[] decode(String value) {
        String input = value.replace("=", "").toUpperCase();
        byte[] out = new byte[input.length() * 5 / 8];
        int buffer = 0;
        int bits = 0;
        int index = 0;
        for (char c : input.toCharArray()) {
            int n = ALPHABET.indexOf(c);
            if (n < 0) {
                throw new IllegalArgumentException("Invalid base32");
            }
            buffer = (buffer << 5) | n;
            bits += 5;
            if (bits >= 8) {
                bits -= 8;
                out[index++] = (byte) ((buffer >> bits) & 0xff);
            }
        }
        return out;
    }
}
