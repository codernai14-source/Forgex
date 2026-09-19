package com.forgex.sys.domain.param;

import com.forgex.common.base.BaseGetParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Excel导入配置分页查询参数类
 * @author coder_nai@163.com
 * @date 2026年01月16日
 * @description: 用于接收Excel导入配置分页查询的请求参数
 * @version: 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ExcelImportConfigPageParam extends BaseGetParam {
    /**
     * 表名
     */
    private String tableName;
    
    /**
     * 表编码
     */
    private String tableCode;
}