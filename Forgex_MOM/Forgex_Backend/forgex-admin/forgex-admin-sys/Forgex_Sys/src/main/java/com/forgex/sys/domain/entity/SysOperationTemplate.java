package com.forgex.sys.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;

/**
 * 操作模板实体类
 * <p>存储操作日志的模板配置。</p>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
@TableName("sys_operation_template")
public class SysOperationTemplate extends BaseEntity {

    /**
     * 模块名称
     */
    @TableField("module")
    private String module;

    /**
     * 操作类型
     */
    @TableField("operation_type")
    private String operationType;

    /**
     * 模板代码
     */
    @TableField("template_code")
    private String templateCode;

    /**
     * 国际化文本JSON
     */
    @TableField("text_i18n_json")
    private String textI18nJson;

    /**
     * 占位符JSON
     */
    @TableField("placeholders_json")
    private String placeholdersJson;
}

