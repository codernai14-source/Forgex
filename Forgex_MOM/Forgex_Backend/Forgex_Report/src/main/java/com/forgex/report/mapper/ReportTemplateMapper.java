package com.forgex.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.report.domain.entity.ReportTemplate;
import org.apache.ibatis.annotations.Mapper;

/**
 * 报表模板 Mapper 接口
 * <p>
 * 数据访问层，继承 MyBatis-Plus 的 BaseMapper
 * 提供报表模板的基础 CRUD 操作
 * </p>
 *
 * @author LiDaoMoM
 * @version 1.0
 * @since 2026-04-09
 * @see BaseMapper
 * @see ReportTemplate
 */
@Mapper
public interface ReportTemplateMapper extends BaseMapper<ReportTemplate> {

}
