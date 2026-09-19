package com.forgex.common.api.dto;

/**
 * 校验当前租户可用的字典值。
 *
 * @param tenantId 当前租户 ID，必须与调用会话一致
 * @param dictCode 字典编码
 * @param dictValue 字典值
 */
public record SysDictValueRequest(Long tenantId, String dictCode, String dictValue) {
}
