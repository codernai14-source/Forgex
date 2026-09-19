package com.forgex.sys.api.dto;

/**
 * 平台备份任务请求，不接受外部指定的操作人或归档保留期。
 *
 * @param tenantId 任务所属租户，与一次性调用凭证绑定
 */
public record SysBackupJobRequestDTO(Long tenantId) {
}
