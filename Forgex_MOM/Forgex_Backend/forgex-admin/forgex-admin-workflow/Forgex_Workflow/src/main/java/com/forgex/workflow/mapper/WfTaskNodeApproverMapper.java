/*Copyright 2026 coder_nai@163.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package com.forgex.workflow.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.workflow.domain.entity.WfTaskNodeApprover;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审批任务节点审批人配置Mapper接口。
 * <p>
 * 提供审批任务节点审批人配置的数据访问操作。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 */
@Mapper
@DS("workflow")
public interface WfTaskNodeApproverMapper extends BaseMapper<WfTaskNodeApprover> {

}