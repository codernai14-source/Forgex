package com.forgex.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.report.domain.entity.ReportCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 报表分类 Mapper 接口
 * <p>
 * 数据访问层，继承 MyBatis-Plus 的 BaseMapper
 * 提供报表分类的基础 CRUD 操作
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-09
 * @see BaseMapper
 * @see ReportCategory
 */
@Mapper
public interface ReportCategoryMapper extends BaseMapper<ReportCategory> {

}
