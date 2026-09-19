/*Copyright 2026 coder_nai@163.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package com.forgex.sys.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 代码生成配置记录实体
 * <p>
 * 映射表：{@code sys_codegen_config}。
 * 用于持久化在线开发保存的生成配置，支撑配置记录页、预览与下载。
 * </p>
 *
 * @author coder_nai@163.com
 * @since 2026-04-21
 *
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_codegen_config")
public class SysCodegenConfig extends BaseEntity {

    /**
     * 配置名称
     */
    private String configName;

    /**
     * 数据源 ID
     */
    private Long datasourceId;

    /**
     * 数据源编码
     */
    private String datasourceCode;

    /**
     * schema/catalog 名称
     */
    private String schemaName;

    /**
     * 页面类型
     */
    private String pageType;

    /**
     * 主表名称
     */
    private String mainTableName;

    /**
     * 子表名称
     */
    private String subTableName;

    /**
     * 主表主键字段
     */
    private String mainPkColumn;

    /**
     * 子表外键字段
     */
    private String subFkColumn;

    /**
     * 子表主键字段
     */
    private String subPkColumn;

    /**
     * 模块编码
     */
    private String moduleName;

    /**
     * 业务编码
     */
    private String bizName;

    /**
     * 主实体名称
     */
    private String entityName;

    /**
     * 子实体名称
     */
    private String subEntityName;

    /**
     * 包名
     */
    private String packageName;

    /**
     * 作者
     */
    private String author;

    /**
     * 菜单名称
     */
    private String menuName;

    /**
     * 菜单图标
     */
    private String menuIcon;

    /**
     * 父级菜单路径
     */
    private String parentMenuPath;

    /**
     * 表格编码前缀
     */
    private String tableCodePrefix;

    /**
     * 生成项 JSON
     */
    private String generateItemsJson;

    /**
     * 完整配置 JSON
     */
    private String configJson;

    /**
     * 最近生成时间
     */
    private LocalDateTime lastGenerateTime;

    /**
     * 备注
     */
    private String remark;
}

