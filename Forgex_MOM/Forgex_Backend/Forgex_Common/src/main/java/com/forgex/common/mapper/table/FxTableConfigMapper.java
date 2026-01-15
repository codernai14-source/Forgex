package com.forgex.common.mapper.table;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.common.domain.entity.table.FxTableConfig;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@DS("common")
public interface FxTableConfigMapper extends BaseMapper<FxTableConfig> {
}

