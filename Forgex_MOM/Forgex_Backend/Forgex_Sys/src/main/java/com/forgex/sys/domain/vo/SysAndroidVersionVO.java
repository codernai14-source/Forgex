package com.forgex.sys.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 安卓版本管理视图对象
 * <p>
 * 用于前端列表展示，包含版本信息、文件信息、操作人信息等
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0.0
 * @since 2026-05-05
 */
@Data
public class SysAndroidVersionVO {

    /**
     * 主键 ID
     */
    private Long id;

    /**
     * 版本号（内部递增）
     */
    private Integer versionCode;

    /**
     * 版本名称（展示用）
     */
    private String versionName;

    /**
     * 更新日志
     */
    private String changelog;

    /**
     * 原始文件名
     */
    private String fileName;

    /**
     * 文件访问 URL
     */
    private String fileUrl;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 存储类型
     */
    private String storageType;

    /**
     * 状态：1-启用 0-禁用
     */
    private Integer status;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
