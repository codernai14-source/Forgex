package com.forgex.common.audit;

/**
 * 操作类型枚举
 * <p>
 * 定义系统支持的所有操作类型，用于标识用户操作的性质。
 * </p>
 * <p><strong>操作类型说明：</strong></p>
 * <ul>
 *   <li>{@link #ADD} - 新增操作，如创建用户、创建角色等</li>
 *   <li>{@link #DELETE} - 删除操作，如删除用户、删除角色等</li>
 *   <li>{@link #UPDATE} - 更新操作，如修改用户信息、修改角色权限等</li>
 *   <li>{@link #UPLOAD} - 上传操作，如上传文件、上传图片等</li>
 *   <li>{@link #DOWNLOAD} - 下载操作，如下载文件、导出数据等</li>
 *   <li>{@link #REPORT} - 报表操作，如生成报表、导出统计等</li>
 *   <li>{@link #PUSH} - 推送操作，如发送消息、推送通知等</li>
 *   <li>{@link #SEND} - 发送操作，如发送邮件、发送短信等</li>
 *   <li>{@link #GENERATE} - 生成操作，如生成代码、生成文档等</li>
 * </ul>
 * <p><strong>使用场景：</strong></p>
 * <ul>
 *   <li>配合{@link OperationLog}注解使用，指定操作类型</li>
 *   <li>用于统计和分析用户行为</li>
 *   <li>用于审计和追溯操作历史</li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see OperationLog
 */
public enum OperationType {
    /**
     * 新增操作
     * <p>用于标识创建、添加等操作，如创建用户、创建角色、创建菜单等。</p>
     */
    ADD,

    /**
     * 删除操作
     * <p>用于标识删除、移除等操作，如删除用户、删除角色、删除菜单等。</p>
     */
    DELETE,

    /**
     * 更新操作
     * <p>用于标识修改、编辑等操作，如修改用户信息、修改角色权限、更新菜单等。</p>
     */
    UPDATE,

    /**
     * 上传操作
     * <p>用于标识文件上传、图片上传等操作。</p>
     */
    UPLOAD,

    /**
     * 下载操作
     * <p>用于标识文件下载、数据导出等操作。</p>
     */
    DOWNLOAD,

    /**
     * 报表操作
     * <p>用于标识生成报表、导出统计等操作。</p>
     */
    REPORT,

    /**
     * 推送操作
     * <p>用于标识发送消息、推送通知等操作。</p>
     */
    PUSH,

    /**
     * 发送操作
     * <p>用于标识发送邮件、发送短信等操作。</p>
     */
    SEND,

    /**
     * 生成操作
     * <p>用于标识生成代码、生成文档、生成模板等操作。</p>
     */
    GENERATE
}

