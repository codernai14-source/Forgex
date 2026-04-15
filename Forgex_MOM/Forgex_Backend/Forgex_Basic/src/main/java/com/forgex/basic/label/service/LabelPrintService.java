package com.forgex.basic.label.service;

import com.forgex.basic.label.domain.param.LabelPrintExecuteParam;

import java.util.List;
import java.util.Map;

/**
 * 标签打印 Service 接口
 * <p>
 * 提供标签打印相关的核心业务操作，包括：
 * 1. 参数校验
 * 2. 模板匹配
 * 3. 数据组装与占位符替换
 * 4. 打印记录保存
 * 5. 补打支持
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
public interface LabelPrintService {

    /**
     * 执行标签打印
     * <p>
     * 核心流程：
     * 1. 校验参数
     * 2. 匹配模板
     * 3. 【使用 DataAssemblyHandler】准备业务数据
     * 4. 【使用 PlaceholderHandler】替换占位符
     * 5. 生成打印结果
     * 6. 【使用 PrintSnapshotHandler】保存打印记录
     * </p>
     *
     * @param param 打印执行参数
     * @param userId 操作用户 ID
     * @param tenantId 租户 ID
     * @return 打印结果列表（已填充数据的标签 JSON）
     */
    List<String> executePrint(LabelPrintExecuteParam param, Long userId, Long tenantId);

    /**
     * 打印预览
     * <p>
     * 与执行打印类似，但不保存打印记录，仅用于预览效果
     * </p>
     *
     * @param param 打印执行参数
     * @param tenantId 租户 ID
     * @return 预览结果列表（已填充数据的标签 JSON）
     */
    List<String> previewPrint(LabelPrintExecuteParam param, Long tenantId);

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
    List<String> reprintLabel(Long recordId, Integer reprintCount, Long userId, Long tenantId);
}
