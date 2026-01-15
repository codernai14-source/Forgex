package com.forgex.common.mapper.table;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.common.domain.entity.table.FxUserTablePref;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@DS("common")
public interface FxUserTablePrefMapper extends BaseMapper<FxUserTablePref> {
}

