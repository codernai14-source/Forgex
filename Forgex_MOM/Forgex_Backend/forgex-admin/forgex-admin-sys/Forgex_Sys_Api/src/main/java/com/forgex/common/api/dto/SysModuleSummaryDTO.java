package com.forgex.common.api.dto;

/**
 * 平台模块摘要，避免调用方依赖 Sys 实体。
 *
 * @param id 模块 ID
 * @param name 模块名称
 */
public record SysModuleSummaryDTO(Long id, String name) {
}
