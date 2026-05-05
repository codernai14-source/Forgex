package com.forgex.sys.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体类
 * <p>记录用户的操作日志信息。</p>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
@TableName("sys_operation_log")
public class SysOperationLog extends BaseEntity {

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 账号。
     */
    @TableField(exist = false)
    private String account;

    /**
     * 模块名称
     */
    @TableField("module")
    private String module;

    /**
     * 菜单路径
     */
    @TableField("menu_path")
    private String menuPath;

    /**
     * 操作类型
     */
    @TableField("operation_type")
    private String operationType;

    /**
     * 请求方法
     */
    @TableField("request_method")
    private String requestMethod;

    /**
     * 请求URL
     */
    @TableField("request_url")
    private String requestUrl;

    /**
     * 请求参数
     */
    @TableField("request_params")
    private String requestParams;

    /**
     * 响应状态码
     */
    @TableField("response_status")
    private Integer responseStatus;

    /**
     * 响应结果
     */
    @TableField("response_result")
    private String responseResult;

    /**
     * 错误堆栈
     */
    @TableField("error_stack")
    private String errorStack;

    /**
     * 操作时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("operation_time")
    private LocalDateTime operationTime;

    /**
     * 耗时（毫秒）
     */
    @TableField("cost_time")
    private Long costTime;

    /**
     * IP地址
     */
    @TableField("ip")
    private String ip;

    /**
     * User-Agent
     */
    @TableField("user_agent")
    private String userAgent;

    /**
     * 详情模板代码
     */
    @TableField("detail_template_code")
    private String detailTemplateCode;

    /**
     * 详情字段JSON
     */
    @TableField("detail_fields_json")
    private String detailFieldsJson;

    /**
     * 详情文本
     */
    @TableField("detail_text")
    private String detailText;
}

