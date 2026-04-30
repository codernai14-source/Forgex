
package com.forgex.basic.material.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.basic.material.domain.entity.BasicUnit;
import org.apache.ibatis.annotations.Mapper;

/**
 * 计量单位 Mapper 接口
 * <p>
 * 继承 BaseMapper，提供基础的 CRUD 操作能力
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-28
 */
@Mapper
public interface BasicUnitMapper extends BaseMapper<BasicUnit> {
}
