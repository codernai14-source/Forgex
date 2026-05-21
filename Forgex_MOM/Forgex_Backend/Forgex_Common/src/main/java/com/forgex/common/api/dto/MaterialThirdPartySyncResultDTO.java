package com.forgex.common.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 物料第三方同步结果 DTO。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-12
 */
@Data
public class MaterialThirdPartySyncResultDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 同步总数。
     */
    private Integer totalCount = 0;

    /**
     * 创建数量。
     */
    private Integer createdCount = 0;

    /**
     * 更新数量。
     */
    private Integer updatedCount = 0;

    /**
     * 失败数量。
     */
    private Integer failedCount = 0;

    /**
     * 失败物料编码。
     */
    private List<String> failedMaterialCodes = new ArrayList<>();
}
