package com.forgex.sys.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.sys.domain.entity.SysHomepageComponentCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 首页组件分类 Mapper。
 *
 * @author Forgex Team
 * @since 2026-05-15
 */
@Mapper
@DS("admin")
public interface SysHomepageComponentCategoryMapper extends BaseMapper<SysHomepageComponentCategory> {
}
