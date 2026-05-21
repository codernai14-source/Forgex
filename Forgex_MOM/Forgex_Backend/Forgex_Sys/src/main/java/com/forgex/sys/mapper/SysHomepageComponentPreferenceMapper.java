package com.forgex.sys.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.sys.domain.entity.SysHomepageComponentPreference;
import org.apache.ibatis.annotations.Mapper;

/**
 * 首页组件个人偏好 Mapper。
 *
 * @author Forgex Team
 * @since 2026-05-15
 */
@Mapper
@DS("admin")
public interface SysHomepageComponentPreferenceMapper extends BaseMapper<SysHomepageComponentPreference> {
}
