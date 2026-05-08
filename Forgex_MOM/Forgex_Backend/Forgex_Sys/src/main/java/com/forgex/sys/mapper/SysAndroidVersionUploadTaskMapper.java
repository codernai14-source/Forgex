package com.forgex.sys.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.sys.domain.entity.SysAndroidVersionUploadTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * Android APK chunk upload task mapper.
 *
 * @author ForGexTeam
 * @version 1.0.0
 * @since 2026-05-07
 */
@Mapper
@DS("admin")
public interface SysAndroidVersionUploadTaskMapper extends BaseMapper<SysAndroidVersionUploadTask> {
}
