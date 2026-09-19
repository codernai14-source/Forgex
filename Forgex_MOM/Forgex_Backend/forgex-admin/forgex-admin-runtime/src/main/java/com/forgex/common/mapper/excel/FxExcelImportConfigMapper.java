package com.forgex.common.mapper.excel;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.common.domain.entity.excel.FxExcelImportConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * Excel 导入配置主表 Mapper。
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @see FxExcelImportConfig
 */
@Mapper
@DS("common")
public interface FxExcelImportConfigMapper extends BaseMapper<FxExcelImportConfig> {
}

