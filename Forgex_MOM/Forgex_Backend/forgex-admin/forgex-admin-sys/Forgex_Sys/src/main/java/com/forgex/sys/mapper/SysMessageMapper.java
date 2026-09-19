package com.forgex.sys.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.sys.domain.entity.SysMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统消息Mapper接口
 * <p>提供系统消息数据的数据库访问操作。</p>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
@Mapper
@DS("admin")
public interface SysMessageMapper extends BaseMapper<SysMessage> {
}

