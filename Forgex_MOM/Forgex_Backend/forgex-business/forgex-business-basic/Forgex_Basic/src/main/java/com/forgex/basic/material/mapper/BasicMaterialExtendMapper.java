package com.forgex.basic.material.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.basic.material.domain.entity.BasicMaterialExtend;
import org.apache.ibatis.annotations.Mapper;

/**
 * 物料扩展信息表 Mapper 接口
 * <p>
 * 提供基本的 CRUD 操作，继承自 MyBatis-Plus 的 BaseMapper
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-09
 */
@Mapper
public interface BasicMaterialExtendMapper extends BaseMapper<BasicMaterialExtend> {

    // 如果有关联查询或复杂统计 SQL，可以在这里定义方法
    // 并在对应的 XML 文件中实现

}
