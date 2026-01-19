package com.forgex.common.mapper.excel;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.common.domain.entity.excel.FxExcelExportConfigItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * Excel 导出配置子表 Mapper。
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @see FxExcelExportConfigItem
 */
@Mapper
@DS("common")
public interface FxExcelExportConfigItemMapper extends BaseMapper<FxExcelExportConfigItem> {
}

