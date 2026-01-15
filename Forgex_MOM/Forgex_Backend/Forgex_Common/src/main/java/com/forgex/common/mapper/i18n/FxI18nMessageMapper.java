package com.forgex.common.mapper.i18n;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.common.domain.entity.i18n.FxI18nMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@DS("common")
public interface FxI18nMessageMapper extends BaseMapper<FxI18nMessage> {
}

