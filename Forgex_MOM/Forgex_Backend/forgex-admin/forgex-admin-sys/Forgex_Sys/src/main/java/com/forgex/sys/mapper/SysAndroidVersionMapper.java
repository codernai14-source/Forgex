package com.forgex.sys.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.sys.domain.entity.SysAndroidVersion;
import org.apache.ibatis.annotations.Mapper;

/**
 * 安卓版本管理 Mapper 接口
 * <p>
 * 提供 sys_android_version 表的基础 CRUD 操作
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0.0
 * @since 2026-05-05
 * @see BaseMapper
 * @see SysAndroidVersion
 */
@Mapper
@DS("admin")
public interface SysAndroidVersionMapper extends BaseMapper<SysAndroidVersion> {
}
