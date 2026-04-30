package com.forgex.basic.material.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.basic.material.domain.entity.BasicPackagingType;
import org.apache.ibatis.annotations.Mapper;

/**
 * 包装方式 Mapper 接口
 * <p>
 * 继承 BaseMapper，提供基础的 CRUD 操作能力
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-28
 */
@Mapper
public interface BasicPackagingTypeMapper extends BaseMapper<BasicPackagingType> {
}
