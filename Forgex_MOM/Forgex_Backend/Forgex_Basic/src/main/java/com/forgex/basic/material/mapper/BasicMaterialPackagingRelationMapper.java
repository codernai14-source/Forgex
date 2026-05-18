package com.forgex.basic.material.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.basic.material.domain.entity.BasicMaterialPackagingRelation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 物料包装规格关联 Mapper 接口。
 * <p>
 * 提供物料与包装规格三槽关联表的基础 CRUD 能力。
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-05-16
 */
@Mapper
public interface BasicMaterialPackagingRelationMapper extends BaseMapper<BasicMaterialPackagingRelation> {
}
