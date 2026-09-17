package com.forgex.sys.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.sys.domain.entity.SysDataBackupRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 备份记录 Mapper。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Mapper
@DS("history")
public interface SysDataBackupRecordMapper extends BaseMapper<SysDataBackupRecord> {
}
