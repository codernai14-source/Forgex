package com.forgex.job.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.job.domain.entity.SysJobAlarmLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@DS("job")
public interface SysJobAlarmLogMapper extends BaseMapper<SysJobAlarmLog> {
}
