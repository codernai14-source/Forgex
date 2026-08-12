/*
 * Copyright 2026 coder_nai@163.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.forgex.common.crypto;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 文件加密服务接口。
 * <p>
 * 提供统一的文件加密、解密和完整性校验功能，支持多种加密算法（AES-256-GCM、SM4-CBC）。
 * <p>
 * 功能列表：
 * <ul>
 *   <li>文件加密：将明文文件加密为密文文件</li>
 *   <li>文件解密：将密文文件解密为明文文件</li>
 *   <li>流式加密/解密：支持流式处理大文件</li>
 *   <li>SM3 完整性校验：计算和验证文件哈希</li>
 * </ul>
 * <p>
 * 支持的算法：
 * <ul>
 *   <li>{@code "aes"} - AES-256-GCM（国际银行级标准）</li>
 *   <li>{@code "sm4"} - SM4-CBC（国密二级标准）</li>
 * </ul>
 * <p>
 * 使用示例：
 * <pre>
 * &#64;Autowired
 * private FileEncryptService fileEncryptService;
 *
 * // 使用 AES-256 加密文件
 * fileEncryptService.encryptFile(sourceFile, targetFile, "aes");
 *
 * // 使用 SM4 解密文件
 * fileEncryptService.decryptFile(encryptedFile, outputFile, "sm4");
 *
 * // 计算文件 SM3 哈希
 * String hash = fileEncryptService.computeDigest(file);
 *
 * // 验证文件完整性
 * boolean valid = fileEncryptService.verifyDigest(file, expectedHash);
 * </pre>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
public interface FileEncryptService {

    /** 算法标识：AES-256-GCM */
    String ALGORITHM_AES = "aes";

    /** 算法标识：SM4-CBC */
    String ALGORITHM_SM4 = "sm4";

    /**
     * 加密文件。
     *
     * @param inputFile  输入文件（明文）
     * @param outputFile 输出文件（密文）
     * @param algorithm  加密算法标识：{@code "aes"} 或 {@code "sm4"}
     * @throws IOException              文件读写异常
     * @throws IllegalArgumentException 不支持的算法
     */
    void encryptFile(File inputFile, File outputFile, String algorithm) throws IOException;

    /**
     * 解密文件。
     *
     * @param encryptedFile 加密文件（密文）
     * @param outputFile    输出文件（明文）
     * @param algorithm     加密算法标识：{@code "aes"} 或 {@code "sm4"}
     * @throws IOException              文件读写异常
     * @throws IllegalArgumentException 不支持的算法
     */
    void decryptFile(File encryptedFile, File outputFile, String algorithm) throws IOException;

    /**
     * 加密流。
     *
     * @param in        输入流（明文）
     * @param out       输出流（密文）
     * @param algorithm 加密算法标识：{@code "aes"} 或 {@code "sm4"}
     * @throws IOException              流读写异常
     * @throws IllegalArgumentException 不支持的算法
     */
    void encryptStream(InputStream in, OutputStream out, String algorithm) throws IOException;

    /**
     * 解密流。
     *
     * @param in        输入流（密文）
     * @param out       输出流（明文）
     * @param algorithm 加密算法标识：{@code "aes"} 或 {@code "sm4"}
     * @throws IOException              流读写异常
     * @throws IllegalArgumentException 不支持的算法
     */
    void decryptStream(InputStream in, OutputStream out, String algorithm) throws IOException;

    /**
     * 计算文件的 SM3 哈希摘要。
     *
     * @param file 目标文件
     * @return SM3 哈希值（64 字符十六进制字符串）
     * @throws IOException 文件读取异常
     */
    String computeDigest(File file) throws IOException;

    /**
     * 计算输入流的 SM3 哈希摘要。
     *
     * @param inputStream 输入流
     * @return SM3 哈希值（64 字符十六进制字符串）
     * @throws IOException 流读取异常
     */
    String computeDigest(InputStream inputStream) throws IOException;

    /**
     * 验证文件的 SM3 哈希摘要。
     *
     * @param file         目标文件
     * @param expectedHash 期望的 SM3 哈希值
     * @return 是否匹配
     * @throws IOException 文件读取异常
     */
    boolean verifyDigest(File file, String expectedHash) throws IOException;

    /**
     * 使用默认算法（AES-256-GCM）加密文件。
     *
     * @param inputFile  输入文件（明文）
     * @param outputFile 输出文件（密文）
     * @throws IOException 文件读写异常
     */
    default void encryptFile(File inputFile, File outputFile) throws IOException {
        encryptFile(inputFile, outputFile, ALGORITHM_AES);
    }

    /**
     * 使用默认算法（AES-256-GCM）解密文件。
     *
     * @param encryptedFile 加密文件（密文）
     * @param outputFile    输出文件（明文）
     * @throws IOException 文件读写异常
     */
    default void decryptFile(File encryptedFile, File outputFile) throws IOException {
        decryptFile(encryptedFile, outputFile, ALGORITHM_AES);
    }
}

