package com.forgex.integration.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.integration.domain.entity.ApiOutboundTarget;
import org.apache.ibatis.annotations.Mapper;

/**
 * 接口出站目标Mapper 接口。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Mapper
public interface ApiOutboundTargetMapper extends BaseMapper<ApiOutboundTarget> {
}
