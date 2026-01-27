package com.forgex.sys.domain.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 代码生成上下文DTO
 * <p>
 * 封装代码生成所需的所有数据，用于模板渲染。
 * </p>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
public class CodeGenContextDTO {
    
    // ========== 基础信息 ==========
    /**
     * 表名
     */
    private String tableName;
    
    /**
     * 表注释
     */
    private String tableComment;
    
    /**
     * 实体类名
     */
    private String entityName;
    
    /**
     * 实体类名（首字母小写）
     */
    private String entityNameLower;
    
    /**
     * 模块名称
     */
    private String moduleName;
    
    /**
     * 基础包名
     */
    private String basePackage;
    
    /**
     * 生成模式（SINGLE、TREE、MASTER_DETAIL）
     */
    private String mode;
    
    /**
     * 当前日期
     */
    private String date;
    
    // ========== 列信息 ==========
    /**
     * 所有列
     */
    private List<ColumnMetaDTO> columns;
    
    /**
     * 查询列（用于查询表单）
     */
    private List<ColumnMetaDTO> queryColumns;
    
    /**
     * 保存列（用于新增/编辑表单）
     */
    private List<ColumnMetaDTO> saveColumns;
    
    /**
     * 表格列（用于表格显示）
     */
    private List<ColumnMetaDTO> tableColumns;
    
    // ========== 菜单权限信息 ==========
    /**
     * 菜单路径
     */
    private String menuPath;
    
    /**
     * 菜单名称（中文）
     */
    private String menuNameCn;
    
    /**
     * 菜单名称（英文）
     */
    private String menuNameEn;
    
    /**
     * 菜单图标
     */
    private String menuIcon;
    
    /**
     * 菜单排序号
     */
    private Integer menuOrderNum;
    
    /**
     * 组件键
     */
    private String componentKey;
    
    /**
     * 权限键前缀
     */
    private String permKeyPrefix;
    
    /**
     * 模块名称（中文）
     */
    private String moduleNameCn;
    
    /**
     * 模块名称（英文）
     */
    private String moduleNameEn;
    
    /**
     * 模块图标
     */
    private String moduleIcon;
    
    /**
     * 模块排序号
     */
    private Integer moduleOrderNum;
    
    // ========== 表格配置信息 ==========
    /**
     * 表格编码
     */
    private String tableCode;
    
    /**
     * 表格名称（中文）
     */
    private String tableNameCn;
    
    /**
     * 表格名称（英文）
     */
    private String tableNameEn;
    
    // ========== 多语言信息 ==========
    /**
     * 国际化前缀
     */
    private String i18nPrefix;
    
    // ========== 其他配置 ==========
    /**
     * 扩展配置
     */
    private Map<String, Object> extraConfig;
}



