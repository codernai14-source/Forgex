package com.forgex.workflow.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.workflow.domain.entity.WfTaskNodeCc;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审批节点抄送配置 Mapper。
 * <p>
 * 仅继承 {@link BaseMapper}，查询条件由 Service 使用 Wrapper 组装。
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0.0
 * @since 2026-09-11
 * @see WfTaskNodeCc
 */
@Mapper
@DS("workflow")
public interface WfTaskNodeCcMapper extends BaseMapper<WfTaskNodeCc> {
}
