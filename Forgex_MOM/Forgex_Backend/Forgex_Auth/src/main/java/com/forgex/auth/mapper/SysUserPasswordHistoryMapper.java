package com.forgex.auth.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.auth.domain.entity.SysUserPasswordHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 口令历史 Mapper（Auth）。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Mapper
@DS("admin")
public interface SysUserPasswordHistoryMapper extends BaseMapper<SysUserPasswordHistory> {
}
