package com.forgex.common.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 物料附属信息同步 DTO。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-12
 */
@Data
public class MaterialExtendSyncDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 附属信息 ID。
     */
    private Long id;

    /**
     * 物料 ID。
     */
    private Long materialId;

    /**
     * 模块编码。
     */
    private String module;

    /**
     * 扩展 JSON。
     */
    private String extendJson;
}
