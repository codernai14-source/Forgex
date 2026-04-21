package com.forgex.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.report.domain.entity.ReportDatasource;
import org.apache.ibatis.annotations.Mapper;

/**
 * 报表数据源 Mapper 接口
 * <p>
 * 数据访问层，继承 MyBatis-Plus 的 BaseMapper
 * 提供报表数据源的基础 CRUD 操作
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-09
 * @see BaseMapper
 * @see ReportDatasource
 */
@Mapper
public interface ReportDatasourceMapper extends BaseMapper<ReportDatasource> {

}
