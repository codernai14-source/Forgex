package com.forgex.sys.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.sys.domain.entity.SysFileStorage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件存储配置Mapper接口
 * <p>提供文件存储配置数据的数据库访问操作。</p>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
@Mapper
@DS("admin")
public interface SysFileStorageMapper extends BaseMapper<SysFileStorage> {
}

