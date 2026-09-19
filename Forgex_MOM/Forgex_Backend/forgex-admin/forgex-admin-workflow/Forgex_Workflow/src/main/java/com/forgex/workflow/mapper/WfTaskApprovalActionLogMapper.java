package com.forgex.workflow.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.workflow.domain.entity.WfTaskApprovalActionLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审批动作日志 Mapper。
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-15
 */
@Mapper
@DS("workflow")
public interface WfTaskApprovalActionLogMapper extends BaseMapper<WfTaskApprovalActionLog> {
}
