package com.forgex.basic.label.utils;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 条码生成器工具类
 * <p>
 * 提供多种条码号生成策略，支持：
 * 1. 时间戳 + 随机数
 * 2. 前缀 + 日期 + 序列号
 * 3. 业务规则组合编码
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Slf4j
public class BarcodeGenerator {

    /**
     * 序列号计数器（线程安全）
     */
    private static final AtomicLong SEQUENCE = new AtomicLong(0);

    /**
     * 日期格式化器
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * 私有构造函数，防止实例化
     */
    private BarcodeGenerator() {
    }

    /**
     * 生成条码号（时间戳 + 随机数策略）
     * <p>
     * 格式：BC + yyyyMMddHHmmss + 6位随机字符
     * 示例：BC20260414153045A1B2C3
     * </p>
     *
     * @param prefix 前缀（可选，默认 "BC"）
     * @return 条码号
     */
    public static String generateByTimestamp(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            prefix = "BC";
        }

        String timestamp = LocalDateTime.now().format(DATETIME_FORMATTER);
        String random = generateRandomString(6);

        return prefix + timestamp + random;
    }

    /**
     * 生成条码号（日期 + 序列号策略）
     * <p>
     * 格式：前缀 + yyyyMMdd + 6位序列号
     * 示例：MAT20260414000001
     * </p>
     *
     * @param prefix 前缀
     * @return 条码号
     */
    public static String generateBySequence(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            prefix = "BC";
        }

        String date = LocalDateTime.now().format(DATE_FORMATTER);
        long sequence = SEQUENCE.incrementAndGet() % 1000000; // 限制在6位数
        String seqStr = String.format("%06d", sequence);

        return prefix + date + seqStr;
    }

    /**
     * 生成条码号（业务规则组合策略）
     * <p>
     * 格式：模板类型 + 物料ID后4位 + 时间戳后8位 + 随机4位
     * 示例：MAT012315304500A1B2
     * </p>
     *
     * @param templateType 模板类型
     * @param materialId 物料 ID
     * @return 条码号
     */
    public static String generateByBusinessRule(String templateType, Long materialId) {
        // 提取模板类型前3位
        String typePrefix = templateType != null && templateType.length() >= 3
                ? templateType.substring(0, 3).toUpperCase()
                : "GEN";

        // 提取物料 ID 后4位
        String materialSuffix = materialId != null
                ? String.format("%04d", materialId % 10000)
                : "0000";

        // 时间戳后8位
        String timestamp = LocalDateTime.now().format(DATETIME_FORMATTER);
        String timeSuffix = timestamp.substring(timestamp.length() - 8);

        // 随机4位
        String random = generateRandomString(4);

        return typePrefix + materialSuffix + timeSuffix + random;
    }

    /**
     * 生成条码号（LOT 号关联策略）
     * <p>
     * 格式：LOT + LOT号 + 随机6位
     * 示例：LOT2026041401A1B2C3
     * </p>
     *
     * @param lotNo LOT 号
     * @return 条码号
     */
    public static String generateByLotNo(String lotNo) {
        String prefix = "LOT";
        String lotPart = lotNo != null && !lotNo.isEmpty() ? lotNo : "00000000";
        String random = generateRandomString(6);

        return prefix + lotPart + random;
    }

    /**
     * 生成条码号（工程卡号关联策略）
     * <p>
     * 格式：EC + 工程卡号 + 随机4位
     * 示例：ECENG20260414001A1B2
     * </p>
     *
     * @param engineeringCardNo 工程卡号
     * @return 条码号
     */
    public static String generateByEngineeringCard(String engineeringCardNo) {
        String prefix = "EC";
        String cardPart = engineeringCardNo != null && !engineeringCardNo.isEmpty()
                ? engineeringCardNo
                : "0000000000";
        String random = generateRandomString(4);

        return prefix + cardPart + random;
    }

    /**
     * 生成随机字符串
     *
     * @param length 长度
     * @return 随机字符串（大写字母+数字）
     */
    private static String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }

    /**
     * 重置序列号计数器（用于测试或每日重置）
     */
    public static void resetSequence() {
        SEQUENCE.set(0);
        log.info("条码序列号计数器已重置");
    }
}

