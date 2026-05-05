package com.forgex.sys.domain.dto;

import lombok.Data;

/**
 * 安卓版本新增/编辑请求参数
 * <p>
 * 用于上传 APK 或编辑版本信息时接收前端参数
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0.0
 * @since 2026-05-05
 */
@Data
public class SysAndroidVersionDTO {

    /**
     * 主键 ID（编辑时必填）
     */
    private Long id;

    /**
     * 版本号（内部递增，如 101）
     */
    private Integer versionCode;

    /**
     * 版本名称（展示用，如 1.0.1）
     */
    private String versionName;

    /**
     * 更新日志
     */
    private String changelog;

    /**
     * 状态：1-启用 0-禁用
     */
    private Integer status;
}
