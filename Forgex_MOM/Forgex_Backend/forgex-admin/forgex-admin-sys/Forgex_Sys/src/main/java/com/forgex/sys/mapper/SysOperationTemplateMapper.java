package com.forgex.sys.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.sys.domain.entity.SysOperationTemplate;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作模板Mapper接口
 * <p>提供操作模板数据的数据库访问操作。</p>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
@Mapper
@DS("common")
public interface SysOperationTemplateMapper extends BaseMapper<SysOperationTemplate> {
}
