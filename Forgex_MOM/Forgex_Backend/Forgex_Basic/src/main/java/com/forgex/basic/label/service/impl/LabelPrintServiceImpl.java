package com.forgex.basic.label.service.impl;

import com.forgex.basic.label.domain.entity.LabelTemplate;
import com.forgex.basic.label.handler.DataAssemblyHandler;
import com.forgex.basic.label.handler.PlaceholderHandler;
import com.forgex.basic.label.handler.PrintSnapshotHandler;
import com.forgex.basic.label.mapper.LabelTemplateMapper;
import com.forgex.basic.label.service.LabelBindingService;
import com.forgex.basic.label.service.LabelPrintService;
import com.forgex.basic.label.domain.param.LabelPrintExecuteParam;
import com.forgex.basic.label.utils.PlaceholderExtractor;
import com.forgex.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 标签打印 Service 实现类
 * <p>
 * 提供标签打印执行相关的业务操作，包括：
 * 1. 执行打印（正常打印）
 * 2. 打印预览
 * 3. 打印数据组装
 * 4. 占位符替换
 * 5. 打印快照保存
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 * @see LabelPrintService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LabelPrintServiceImpl implements LabelPrintService {

    private final LabelTemplateMapper labelTemplateMapper;
    private final LabelBindingService labelBindingService;

    // 注入 Handler
    private final DataAssemblyHandler dataAssemblyHandler;
    private final PlaceholderHandler placeholderHandler;
    private final PrintSnapshotHandler snapshotHandler;

    /**
     * 执行打印
     * <p>
     * 完整流程：
     * 1. 查询模板
     * 2. 匹配模板（如果未指定）
     * 3. 【使用 DataAssemblyHandler】组装打印数据
     * 4. 验证数据完整性
     * 5. 【使用 PlaceholderHandler】替换占位符生成标签内容
     * 6. 【使用 PrintSnapshotHandler】保存打印快照
     * 7. 返回打印结果
     * </p>
     *
     * @param param 打印执行参数
     * @param userId 用户 ID
     * @param tenantId 租户 ID
     * @return 打印结果列表（每张标签的内容）
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<String> executePrint(LabelPrintExecuteParam param, Long userId, Long tenantId) {
        log.info("开始执行打印，模板 ID: {}, 打印张数: {}", param.getTemplateId(), param.getPrintCount());

        // 1. 查询模板
        LabelTemplate template = getTemplate(param, tenantId);

        // 2. 校验打印张数
        validatePrintCount(param.getPrintCount());

        // 3. 【使用 DataAssemblyHandler】组装完整打印数据
        // 输入: {materialId: 123, lotNo: "LOT001"}
        // 输出: {materialId: 123, materialCode: "MAT001", materialName: "电阻", ..., printTime: "..."}
        Map<String, Object> completeData = dataAssemblyHandler.assemblePrintData(param.getPrintData());

        // 4. 验证数据完整性
        validateDataCompleteness(template.getTemplateContent(), completeData);

        // 5. 【使用 PlaceholderHandler】替换占位符生成标签内容
        List<String> printResults = generateLabelContents(
                template.getTemplateContent(),
                completeData,
                param.getPrintCount()
        );

        // 6. 【使用 PrintSnapshotHandler】保存打印快照
        Long recordId = snapshotHandler.createSnapshot(
                template.getId(),
                template.getTemplateName(),
                template.getTemplateType(),
                completeData,
                printResults,
                param.getFactoryId(),
                userId,
                tenantId
        );

        log.info("打印执行成功，记录 ID: {}, 生成 {} 张标签", recordId, printResults.size());
        return printResults;
    }

    /**
     * 打印预览
     * <p>
     * 与正常打印的区别：
     * 1. 不保存打印记录
     * 2. 可以预览多张
     * 3. 用于前端展示效果
     * </p>
     *
     * @param param 打印执行参数
     * @param tenantId 租户 ID
     * @return 预览结果列表
     */
    @Override
    public List<String> previewPrint(LabelPrintExecuteParam param, Long tenantId) {
        log.info("开始打印预览，模板 ID: {}", param.getTemplateId());

        // 1. 查询模板
        LabelTemplate template = getTemplate(param, tenantId);

        // 2. 校验打印张数（预览最多 10 张）
        if (param.getPrintCount() > 10) {
            throw new BusinessException("预览最多支持 10 张");
        }

        // 3. 【使用 DataAssemblyHandler】组装打印数据
        Map<String, Object> completeData = dataAssemblyHandler.assemblePrintData(param.getPrintData());

        // 4. 验证数据完整性
        validateDataCompleteness(template.getTemplateContent(), completeData);

        // 5. 【使用 PlaceholderHandler】替换占位符生成标签内容
        List<String> previewResults = generateLabelContents(
                template.getTemplateContent(),
                completeData,
                param.getPrintCount()
        );

        log.info("打印预览完成，生成 {} 张预览", previewResults.size());

        // 注意：预览不保存快照
        return previewResults;
    }

    /**
     * 补打标签
     * <p>
     * 基于历史打印记录原样还原，不重新查询业务数据
     * </p>
     *
     * @param recordId 打印记录 ID
     * @param reprintCount 补打张数
     * @param userId 操作用户 ID
     * @param tenantId 租户 ID
     * @return 补打的标签内容列表
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<String> reprintLabel(Long recordId, Integer reprintCount, Long userId, Long tenantId) {
        log.info("开始补打标签，原始记录 ID: {}, 补打张数: {}", recordId, reprintCount);

        // 1. 【使用 PrintSnapshotHandler】从快照还原打印结果
        List<String> printResults = snapshotHandler.restorePrintResults(recordId, tenantId);

        // 2. 【使用 PrintSnapshotHandler】创建补打记录
        Long reprintRecordId = snapshotHandler.createReprintSnapshot(
                recordId,
                reprintCount,
                userId,
                tenantId
        );

        log.info("补打成功，新记录 ID: {}", reprintRecordId);
        return printResults;
    }

    /**
     * 获取模板
     * <p>
     * 如果指定了模板 ID，直接查询；否则根据绑定关系智能匹配
     * </p>
     *
     * @param param 打印参数
     * @param tenantId 租户 ID
     * @return 模板实体
     */
    private LabelTemplate getTemplate(LabelPrintExecuteParam param, Long tenantId) {
        LabelTemplate template;

        if (param.getTemplateId() != null) {
            // 直接查询指定模板
            template = labelTemplateMapper.selectById(param.getTemplateId());
            if (template == null || !template.getTenantId().equals(tenantId)) {
                throw new BusinessException("模板不存在");
            }
        } else {
            // 【使用 LabelBindingService】智能匹配模板
            Long templateId = labelBindingService.matchTemplate(
                    param.getFactoryId(),
                    param.getTemplateType(),
                    getLongValue(param.getPrintData(), "materialId"),
                    getLongValue(param.getPrintData(), "supplierId"),
                    getLongValue(param.getPrintData(), "customerId"),
                    tenantId
            );

            template = labelTemplateMapper.selectById(templateId);
            if (template == null) {
                throw new BusinessException("未找到可用模板");
            }
        }

        // 校验模板状态
        if (template.getStatus() != 1) {
            throw new BusinessException("模板已禁用，无法打印");
        }

        return template;
    }

    /**
     * 校验打印张数
     *
     * @param printCount 打印张数
     */
    private void validatePrintCount(Integer printCount) {
        if (printCount == null || printCount <= 0) {
            throw new BusinessException("打印张数必须大于 0");
        }
        if (printCount > 1000) {
            throw new BusinessException("单次打印不能超过 1000 张");
        }
    }

    /**
     * 验证数据完整性
     * <p>
     * 检查打印数据是否包含模板所需的所有占位符
     * </p>
     *
     * @param templateContent 模板内容
     * @param printData 打印数据
     */
    private void validateDataCompleteness(String templateContent, Map<String, Object> printData) {
        // 【使用 PlaceholderExtractor】提取模板中的所有占位符
        Set<String> placeholders = PlaceholderExtractor.extractUnique(templateContent);

        // 【使用 DataAssemblyHandler】验证数据完整性
        List<String> missingFields = dataAssemblyHandler.validateDataCompleteness(
                printData,
                new ArrayList<>(placeholders)
        );

        if (!missingFields.isEmpty()) {
            throw new BusinessException("数据不完整，缺少字段: " + missingFields);
        }

        log.debug("数据完整性校验通过，共 {} 个占位符", placeholders.size());
    }

    /**
     * 生成标签内容
     * <p>
     * 【使用 PlaceholderHandler】处理占位符，支持：
     * - 基础替换：{{fieldName}}
     * - 默认值：{{fieldName:default}}
     * - 条件渲染：{{#if condition}}...{{/if}}
     * - 循环渲染：{{#each items}}...{{/each}}
     * - 格式化：{{fieldName|uppercase}}
     * </p>
     *
     * @param templateContent 模板内容
     * @param printData 打印数据
     * @param printCount 打印张数
     * @return 标签内容列表
     */
    private List<String> generateLabelContents(String templateContent, Map<String, Object> printData, Integer printCount) {
        List<String> results = new ArrayList<>();

        for (int i = 0; i < printCount; i++) {
            // 【使用 PlaceholderHandler】替换占位符
            String filledLabel = placeholderHandler.process(templateContent, printData);
            results.add(filledLabel);
        }

        log.debug("生成标签内容完成，共 {} 张", results.size());
        return results;
    }

    /**
     * 从 Map 中获取 Long 值
     *
     * @param data 数据 Map
     * @param key 键
     * @return Long 值
     */
    private Long getLongValue(Map<String, Object> data, String key) {
        if (data == null || !data.containsKey(key)) {
            return null;
        }

        Object value = data.get(key);
        if (value == null) {
            return null;
        }

        if (value instanceof Long) {
            return (Long) value;
        }

        if (value instanceof Number) {
            return ((Number) value).longValue();
        }

        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
