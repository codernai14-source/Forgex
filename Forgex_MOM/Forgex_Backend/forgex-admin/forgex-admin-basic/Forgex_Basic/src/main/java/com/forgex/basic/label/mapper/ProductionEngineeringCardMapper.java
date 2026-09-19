package com.forgex.basic.label.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.basic.label.domain.entity.ProductionEngineeringCard;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工程卡 Mapper 接口
 * <p>
 * 提供基本的 CRUD 操作，继承自 MyBatis-Plus 的 BaseMapper
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Mapper
public interface ProductionEngineeringCardMapper extends BaseMapper<ProductionEngineeringCard> {
}

