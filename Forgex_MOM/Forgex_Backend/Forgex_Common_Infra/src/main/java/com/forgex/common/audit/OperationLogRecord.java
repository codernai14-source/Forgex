package com.forgex.common.audit;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 操作日志记录实体
 * <p>
 * 封装操作日志的完整信息，用于传递给{@link OperationLogRecorder}进行持久化存储。
 * </p>
 * <p><strong>主要字段：</strong></p>
 * <ul>
 *   <li>租户ID和用户ID：标识操作主体</li>
 *   <li>模块和菜单路径：标识操作所属的功能模块</li>
 *   <li>操作类型：标识操作类型（增删改查等）</li>
 *   <li>请求信息：请求方法、URL、IP、User-Agent</li>
 *   <li>请求参数：过滤敏感信息后的请求参数</li>
 *   <li>响应信息：响应状态码、响应结果</li>
 *   <li>异常信息：错误堆栈信息</li>
 *   <li>操作信息：操作时间、操作耗时</li>
 *   <li>详情信息：详情模板代码、详情字段</li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see OperationLogAspect
 * @see OperationLogRecorder
 */
@Data
public class OperationLogRecord {
    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 模块标识
     */
    private String module;

    /**
     * 菜单路径
     */
    private String menuPath;

    /**
     * 操作类型
     */
    private String operationType;

    /**
     * 请求方法（GET、POST、PUT、DELETE等）
     */
    private String requestMethod;

    /**
     * 请求URL
     */
    private String requestUrl;

    /**
     * 请求参数（过滤敏感信息后的JSON字符串）
     */
    private String requestParams;

    /**
     * 响应状态码
     */
    private Integer responseStatus;

    /**
     * 响应结果（过滤敏感信息后的JSON字符串）
     */
    private String responseResult;

    /**
     * 错误堆栈信息（限制2000字符）
     */
    private String errorStack;

    /**
     * 操作时间
     */
    private LocalDateTime operationTime;

    /**
     * 操作耗时（毫秒）
     */
    private Long costTime;

    /**
     * 客户端IP地址
     */
    private String ip;

    /**
     * 客户端User-Agent
     */
    private String userAgent;

    /**
     * 详情模板代码
     */
    private String detailTemplateCode;

    /**
     * 详情字段映射（字段名到字段值的映射）
     */
    private Map<String, Object> detailFields;
}

