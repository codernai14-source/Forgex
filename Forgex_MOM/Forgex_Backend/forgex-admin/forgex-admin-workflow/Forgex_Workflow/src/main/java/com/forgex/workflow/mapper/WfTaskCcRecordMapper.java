package com.forgex.workflow.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.workflow.domain.entity.WfTaskCcRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审批节点抄送运行时记录 Mapper。
 * <p>
 * 仅继承 {@link BaseMapper}，列表聚合与已读更新由 Service 使用 Wrapper 完成。
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0.0
 * @since 2026-09-11
 * @see WfTaskCcRecord
 */
@Mapper
@DS("workflow")
public interface WfTaskCcRecordMapper extends BaseMapper<WfTaskCcRecord> {
}
