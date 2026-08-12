package com.forgex.common.mapper.i18n;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.common.domain.entity.i18n.FxI18nLanguageType;
import org.apache.ibatis.annotations.Mapper;

/**
 * 多语言类型配置Mapper接口
 * <p>
 * 提供对多语言类型配置表的数据库操作。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see com.forgex.common.domain.entity.i18n.FxI18nLanguageType
 */
@Mapper
@DS("common")
public interface FxI18nLanguageTypeMapper extends BaseMapper<FxI18nLanguageType> {
}
