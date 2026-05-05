package com.forgex.sys.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.sys.domain.entity.SysFileRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * sys文件recordMapper 接口。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Mapper
@DS("admin")
public interface SysFileRecordMapper extends BaseMapper<SysFileRecord> {
}
