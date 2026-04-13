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
package com.forgex.workflow.common;

/**
 * 工作流模块常量定义。
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 */
public class WorkflowConstants {

    private WorkflowConstants() {
        throw new IllegalStateException("Constant class");
    }

    /**
     * 节点类型。
     */
    public static class NodeType {
        /** 开始节点。 */
        public static final Integer START = 1;
        /** 结束节点。 */
        public static final Integer END = 2;
        /** 审批节点。 */
        public static final Integer APPROVE = 3;
        /** 普通节点。 */
        public static final Integer NORMAL = 4;
        /** 分支节点。 */
        public static final Integer BRANCH = 5;
    }

    /**
     * 审批类型。
     */
    public static class ApproveType {
        /** 会签。 */
        public static final Integer COUNTERSIGN = 1;
        /** 或签。 */
        public static final Integer OR_SIGN = 2;
        /** 抄送。 */
        public static final Integer COPY = 3;
        /** 会签投票。 */
        public static final Integer VOTE = 4;
        /** 逐个审批。 */
        public static final Integer SEQUENTIAL = 5;
    }

    /**
     * 审批人类型。
     */
    public static class ApproverType {
        /** 指定单人。 */
        public static final Integer SINGLE = 1;
        /** 指定部门。 */
        public static final Integer DEPARTMENT = 2;
        /** 指定角色。 */
        public static final Integer ROLE = 3;
        /** 指定岗位。 */
        public static final Integer POSITION = 4;
    }

    /**
     * 审批执行状态。
     */
    public static class ExecutionStatus {
        /** 待审批。 */
        public static final Integer PENDING = 0;
        /** 审批中。 */
        public static final Integer PROCESSING = 1;
        /** 已完成。 */
        public static final Integer COMPLETED = 2;
        /** 已驳回。 */
        public static final Integer REJECTED = 3;
    }

    /**
     * 节点状态。
     */
    public static class NodeStatus {
        /** 待审批。 */
        public static final Integer PENDING = 0;
        /** 已通过。 */
        public static final Integer APPROVED = 1;
        /** 已驳回。 */
        public static final Integer REJECTED = 2;
    }

    /**
     * 表单类型。
     */
    public static class FormType {
        /** 自定义表单。 */
        public static final Integer CUSTOM = 1;
        /** 低代码表单。 */
        public static final Integer LOW_CODE = 2;
    }

    /**
     * 配置阶段。
     */
    public static class ConfigStage {
        /** 草稿。 */
        public static final String DRAFT = "DRAFT";
        /** 已发布。 */
        public static final String PUBLISHED = "PUBLISHED";
        /** 已归档。 */
        public static final String ARCHIVED = "ARCHIVED";
    }
}
