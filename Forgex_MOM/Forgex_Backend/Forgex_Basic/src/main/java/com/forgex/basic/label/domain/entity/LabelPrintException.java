package com.forgex.basic.label.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 标签打印异常记录实体
 * <p>
 * 用于记录标签打印过程中发生的异常信息，便于问题追溯和审计
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("basic_label_print_exception")
public class LabelPrintException extends BaseEntity {

    /**
     * 主键 ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 错误代码
     */
    private String errorCode;

    /**
     * 错误消息
     */
    private String errorMessage;

    /**
     * 打印记录 ID
     */
    private Long printRecordId;

    /**
     * 模板 ID
     */
    private Long templateId;

    /**
     * 工厂 ID
     */
    private Long factoryId;

    /**
     * 操作人 ID
     */
    private Long operatorId;

    /**
     * 异常发生时间
     */
    private LocalDateTime exceptionTime;

    /**
     * 堆栈信息
     */
    private String stackTrace;

    /**
     * 租户 ID
     */
    private Long tenantId;

    /**
     * 逻辑删除标识
     */
    private Boolean deleted;
}

