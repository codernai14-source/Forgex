package com.forgex.basic.label.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.basic.label.domain.entity.LabelPrintRecord;
import com.forgex.basic.label.mapper.LabelPrintRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 打印快照处理器
 * <p>
 * 负责在标签打印时保存完整的数据快照，支持：
 * 1. 保存打印前的原始数据（dataSnapshot）
 * 2. 保存打印后的完整标签内容（printResultJson）
 * 3. 生成唯一的打印流水号
 * 4. 支持补打时从快照还原数据
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PrintSnapshotHandler {

    private final LabelPrintRecordMapper labelPrintRecordMapper;
    private final com.forgex.basic.label.mapper.LabelTemplateMapper labelTemplateMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 创建打印快照记录
     * <p>
     * 在正常打印时调用，保存完整的打印数据
     * </p>
     *
     * @param templateId 模板 ID
     * @param templateName 模板名称
     * @param templateType 模板类型
     * @param printData 打印数据（原始数据）
     * @param printResults 打印结果列表（填充后的标签内容）
     * @param factoryId 工厂 ID
     * @param userId 操作用户 ID
     * @param tenantId 租户 ID
     * @return 打印记录 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createSnapshot(Long templateId, String templateName, String templateType,
                               Map<String, Object> printData, List<String> printResults,
                               Long factoryId, Long userId, Long tenantId) {
        try {
            // 1. 序列化数据快照
            String dataSnapshot = objectMapper.writeValueAsString(printData);

            // 2. 序列化打印结果
            String printResultJson = objectMapper.writeValueAsString(printResults);

            // 3. 生成打印流水号
            String printNo = generatePrintNo(templateType);

            // 4. 查询模板信息获取 templateCode 和 templateVersion
            com.forgex.basic.label.domain.entity.LabelTemplate template = labelTemplateMapper.selectById(templateId);
            String templateCode = template != null ? template.getTemplateCode() : null;
            Integer templateVersion = template != null ? template.getTemplateVersion() : 1;

            // 5. 创建打印记录
            LabelPrintRecord record = new LabelPrintRecord();
            record.setPrintNo(printNo);
            record.setTemplateId(templateId);
            record.setTemplateCode(templateCode);
            record.setTemplateName(templateName);
            record.setTemplateVersion(templateVersion);
            record.setTemplateType(templateType);
            record.setDataSnapshot(dataSnapshot);
            record.setPrintResultJson(printResultJson);
            record.setPrintCount(printResults.size());
            record.setPrintType("NORMAL"); // 正常打印
            record.setFactoryId(factoryId);
            record.setOperatorId(userId);
            record.setPrintTime(LocalDateTime.now());
            record.setTenantId(tenantId);
            record.setDeleted(false);

            labelPrintRecordMapper.insert(record);

            log.info("创建打印快照成功，打印流水号: {}, 记录 ID: {}", printNo, record.getId());
            return record.getId();

        } catch (JsonProcessingException e) {
            log.error("序列化打印数据失败", e);
            throw new RuntimeException("创建打印快照失败", e);
        }
    }

    /**
     * 创建补打快照记录
     * <p>
     * 在补打时调用，基于原始快照生成新的记录
     * </p>
     *
     * @param originalRecordId 原始打印记录 ID
     * @param reprintCount 补打张数
     * @param userId 操作用户 ID
     * @param tenantId 租户 ID
     * @return 新的打印记录 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createReprintSnapshot(Long originalRecordId, Integer reprintCount,
                                      Long userId, Long tenantId) {
        try {
            // 1. 查询原始记录
            LabelPrintRecord originalRecord = labelPrintRecordMapper.selectById(originalRecordId);
            if (originalRecord == null || !originalRecord.getTenantId().equals(tenantId)) {
                throw new RuntimeException("原始打印记录不存在");
            }

            // 2. 生成新的打印流水号
            String printNo = generatePrintNo(originalRecord.getTemplateType());

            // 3. 创建补打记录（复制原始快照数据）
            LabelPrintRecord reprintRecord = new LabelPrintRecord();
            reprintRecord.setPrintNo(printNo);
            reprintRecord.setTemplateId(originalRecord.getTemplateId());
            reprintRecord.setTemplateCode(originalRecord.getTemplateCode());
            reprintRecord.setTemplateName(originalRecord.getTemplateName());
            reprintRecord.setTemplateVersion(originalRecord.getTemplateVersion());
            reprintRecord.setTemplateType(originalRecord.getTemplateType());
            reprintRecord.setDataSnapshot(originalRecord.getDataSnapshot()); // 使用原始快照
            reprintRecord.setPrintResultJson(originalRecord.getPrintResultJson()); // 使用原始结果
            reprintRecord.setPrintCount(reprintCount != null ? reprintCount : originalRecord.getPrintCount());
            reprintRecord.setPrintType("REPRINT"); // 补打
            reprintRecord.setFactoryId(originalRecord.getFactoryId());
            reprintRecord.setOperatorId(userId);
            reprintRecord.setPrintTime(LocalDateTime.now());
            reprintRecord.setTenantId(tenantId);
            reprintRecord.setDeleted(false);

            labelPrintRecordMapper.insert(reprintRecord);

            log.info("创建补打快照成功，原始记录 ID: {}, 新记录 ID: {}, 流水号: {}",
                    originalRecordId, reprintRecord.getId(), printNo);
            return reprintRecord.getId();

        } catch (Exception e) {
            log.error("创建补打快照失败", e);
            throw new RuntimeException("创建补打快照失败", e);
        }
    }
    /**
     * 从快照还原打印数据
     * <p>
     * 用于补打时从历史记录中还原数据
     * </p>
     *
     * @param recordId 打印记录 ID
     * @param tenantId 租户 ID
     * @return 打印数据 Map
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> restoreFromSnapshot(Long recordId, Long tenantId) {
        try {
            LabelPrintRecord record = labelPrintRecordMapper.selectById(recordId);
            if (record == null || !record.getTenantId().equals(tenantId)) {
                throw new RuntimeException("打印记录不存在");
            }

            // 反序列化数据快照
            return objectMapper.readValue(record.getDataSnapshot(), Map.class);

        } catch (JsonProcessingException e) {
            log.error("反序列化打印快照失败，记录 ID: {}", recordId, e);
            throw new RuntimeException("还原打印数据失败", e);
        }
    }

    /**
     * 从快照还原打印结果
     * <p>
     * 用于补打时直接获取已填充的标签内容
     * </p>
     *
     * @param recordId 打印记录 ID
     * @param tenantId 租户 ID
     * @return 打印结果列表
     */
    @SuppressWarnings("unchecked")
    public List<String> restorePrintResults(Long recordId, Long tenantId) {
        try {
            LabelPrintRecord record = labelPrintRecordMapper.selectById(recordId);
            if (record == null || !record.getTenantId().equals(tenantId)) {
                throw new RuntimeException("打印记录不存在");
            }

            // 反序列化打印结果
            return objectMapper.readValue(record.getPrintResultJson(), List.class);

        } catch (JsonProcessingException e) {
            log.error("反序列化打印结果失败，记录 ID: {}", recordId, e);
            throw new RuntimeException("还原打印结果失败", e);
        }
    }

    /**
     * 生成打印流水号
     * <p>
     * 格式：PRT + 模板类型前3位 + yyyyMMddHHmmss + 4位随机数
     * 示例：PRTMAT20260414153045A1B2
     * </p>
     *
     * @param templateType 模板类型
     * @return 打印流水号
     */
    private String generatePrintNo(String templateType) {
        String prefix = "PRT";
        String typePrefix = templateType != null && templateType.length() >= 3
                ? templateType.substring(0, 3).toUpperCase()
                : "GEN";

        String timestamp = LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 4).toUpperCase();

        return prefix + typePrefix + timestamp + random;
    }

    /**
     * 更新打印记录的打印机信息
     *
     * @param recordId 记录 ID
     * @param printerName 打印机名称
     * @param printerIp 打印机 IP
     */
    @Transactional(rollbackFor = Exception.class)
    public void updatePrinterInfo(Long recordId, String printerName, String printerIp) {
        LabelPrintRecord record = labelPrintRecordMapper.selectById(recordId);
        if (record != null) {
            // TODO: 如果实体类中有 printerName 和 printerIp 字段，则更新
            // record.setPrinterName(printerName);
            // record.setPrinterIp(printerIp);
            // labelPrintRecordMapper.updateById(record);
            log.debug("更新打印机信息，记录 ID: {}, 打印机: {}", recordId, printerName);
        }
    }
}
