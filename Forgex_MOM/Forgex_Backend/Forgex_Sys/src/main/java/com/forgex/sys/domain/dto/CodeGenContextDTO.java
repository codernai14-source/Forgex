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
package com.forgex.sys.domain.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 代码生成上下文 DTO
 * <p>
 * 封装 Beetl 模板渲染所需的全部运行时信息。
 * </p>
 *
 * @author coder_nai@163.com
 * @since 2026-04-21
 */
@Data
public class CodeGenContextDTO {

    /**
     * 数据源编码
     */
    private String datasourceCode;

    /**
     * schema 名称
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
     * 主表注释
     */
    private String mainTableComment;

    /**
     * 子表名称
     */
    private String subTableName;

    /**
     * 子表注释
     */
    private String subTableComment;

    /**
     * 主表实体名
     */
    private String entityName;

    /**
     * 主表实体变量名
     */
    private String entityNameLower;

    /**
     * 子表实体名
     */
    private String subEntityName;

    /**
     * 子表实体变量名
     */
    private String subEntityNameLower;

    /**
     * 模块编码
     */
    private String moduleName;

    /**
     * 业务标识
     */
    private String bizName;

    /**
     * 基础包名
     */
    private String packageName;

    /**
     * 作者
     */
    private String author;

    /**
     * 生成日期
     */
    private String date;

    /**
     * 主表主键列
     */
    private String mainPkColumn;

    private String mainPkJavaField;

    /**
     * 子表主键列
     */
    private String subPkColumn;

    private String subPkJavaField;

    /**
     * 子表外键列
     */
    private String subFkColumn;

    private String subFkJavaField;

    private String subDefaultSortColumn;

    private String subDefaultSortJavaField;

    /**
     * 菜单名称
     */
    private String menuName;

    /**
     * 菜单图标
     */
    private String menuIcon;

    /**
     * 菜单路径
     */
    private String menuPath;

    /**
     * 父级菜单路径
     */
    private String parentMenuPath;

    /**
     * 权限前缀
     */
    private String permKeyPrefix;

    /**
     * 主表表格编码
     */
    private String mainTableCode;

    /**
     * 子表表格编码
     */
    private String subTableCode;

    /**
     * 国际化前缀
     */
    private String i18nPrefix;

    /**
     * 生成项
     */
    private List<String> generateItems = new ArrayList<>();

    /**
     * 主表字段
     */
    private List<ColumnMetaDTO> mainColumns = new ArrayList<>();

    /**
     * 子表字段
     */
    private List<ColumnMetaDTO> subColumns = new ArrayList<>();

    /**
     * 主表查询字段
     */
    private List<ColumnMetaDTO> mainQueryColumns = new ArrayList<>();

    /**
     * 主表表单字段
     */
    private List<ColumnMetaDTO> mainFormColumns = new ArrayList<>();

    /**
     * 主表列表字段
     */
    private List<ColumnMetaDTO> mainTableColumns = new ArrayList<>();

    /**
     * 子表查询字段
     */
    private List<ColumnMetaDTO> subQueryColumns = new ArrayList<>();

    /**
     * 子表表单字段
     */
    private List<ColumnMetaDTO> subFormColumns = new ArrayList<>();

    /**
     * 子表列表字段
     */
    private List<ColumnMetaDTO> subTableColumns = new ArrayList<>();

    /**
     * 需要导入的 Java 类型
     */
    private Set<String> imports = new LinkedHashSet<>();

    /**
     * 扩展配置
     */
    private Map<String, Object> extraConfig;
}
