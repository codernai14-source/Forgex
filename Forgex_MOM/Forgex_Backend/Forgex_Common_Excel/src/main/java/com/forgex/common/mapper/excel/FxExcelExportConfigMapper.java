package com.forgex.common.mapper.excel;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.common.domain.entity.excel.FxExcelExportConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * Excel 导出配置主表 Mapper。
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @see FxExcelExportConfig
 */
@Mapper
@DS("common")
public interface FxExcelExportConfigMapper extends BaseMapper<FxExcelExportConfig> {
}

