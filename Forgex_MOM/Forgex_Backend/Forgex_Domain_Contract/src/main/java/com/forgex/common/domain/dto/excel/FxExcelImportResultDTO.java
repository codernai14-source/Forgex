package com.forgex.common.domain.dto.excel;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Excel 公共导入结果。
 * <p>
 * 统一返回创建、更新、跳过、失败统计和错误明细，便于前端公共导入组件统一展示结果。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-01
 */
@Data
public class FxExcelImportResultDTO {

    /**
     * 总行数。
     */
    private Integer totalCount = 0;

    /**
     * 新增数量。
     */
    private Integer createdCount = 0;

    /**
     * 更新数量。
     */
    private Integer updatedCount = 0;

    /**
     * 跳过数量。
     */
    private Integer skippedCount = 0;

    /**
     * 失败数量。
     */
    private Integer failedCount = 0;

    /**
     * 错误明细。
     */
    private List<String> errorMessages = new ArrayList<>();

    /**
     * 总数加一。
     */
    public void increaseTotal() {
        totalCount = safe(totalCount) + 1;
    }

    /**
     * 新增数加一。
     */
    public void increaseCreated() {
        createdCount = safe(createdCount) + 1;
    }

    /**
     * 更新数加一。
     */
    public void increaseUpdated() {
        updatedCount = safe(updatedCount) + 1;
    }

    /**
     * 跳过数加一。
     */
    public void increaseSkipped() {
        skippedCount = safe(skippedCount) + 1;
    }

    /**
     * 失败数加一。
     */
    public void increaseFailed() {
        failedCount = safe(failedCount) + 1;
    }

    /**
     * 记录失败信息并累计失败数。
     *
     * @param message 失败信息
     */
    public void addError(String message) {
        increaseFailed();
        if (message != null && !message.isBlank()) {
            errorMessages.add(message);
        }
    }

    private int safe(Integer value) {
        return value == null ? 0 : value;
    }
}
