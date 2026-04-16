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

    public static class NodeType {
        public static final Integer START = 1;
        public static final Integer END = 2;
        public static final Integer APPROVE = 3;
        public static final Integer NORMAL = 4;
        public static final Integer BRANCH = 5;
    }

    public static class ApproveType {
        public static final Integer COUNTERSIGN = 1;
        public static final Integer OR_SIGN = 2;
        public static final Integer COPY = 3;
        public static final Integer VOTE = 4;
        public static final Integer SEQUENTIAL = 5;
    }

    public static class ApproverType {
        public static final Integer SINGLE = 1;
        public static final Integer DEPARTMENT = 2;
        public static final Integer ROLE = 3;
        public static final Integer POSITION = 4;
        public static final Integer INITIATOR_SELECTED = 5;
        public static final Integer SUPERIOR = 6;
    }

    public static class RuleType {
        public static final Integer STATIC = 1;
        public static final Integer INITIATOR_SELECTED = 2;
        public static final Integer SUPERIOR = 3;
        public static final Integer DYNAMIC_APPEND = 4;
    }

    public static class ExecutionStatus {
        public static final Integer PENDING = 0;
        public static final Integer PROCESSING = 1;
        public static final Integer COMPLETED = 2;
        public static final Integer REJECTED = 3;
    }

    public static class NodeStatus {
        public static final Integer PENDING = 0;
        public static final Integer APPROVED = 1;
        public static final Integer REJECTED = 2;
    }

    public static class FormType {
        public static final Integer CUSTOM = 1;
        public static final Integer LOW_CODE = 2;
    }

    public static class ConfigStage {
        public static final String DRAFT = "DRAFT";
        public static final String PUBLISHED = "PUBLISHED";
        public static final String ARCHIVED = "ARCHIVED";
    }

    public static class ApprovalInstanceStatus {
        public static final Integer PENDING = 0;
        public static final Integer APPROVED = 1;
        public static final Integer REJECTED = 2;
        public static final Integer TRANSFERRED = 3;
        public static final Integer CLOSED = 4;
    }

    public static class ApprovalActionType {
        public static final Integer APPROVE = 1;
        public static final Integer REJECT = 2;
        public static final Integer TRANSFER = 3;
        public static final Integer ADD_SIGN = 4;
        public static final Integer DELEGATE = 5;
        public static final Integer TIMEOUT_PASS = 6;
        public static final Integer TIMEOUT_TRANSFER = 7;
        public static final Integer SYSTEM_CLOSE = 8;
    }

    public static class TimeoutAction {
        public static final Integer REMIND = 1;
        public static final Integer AUTO_APPROVE = 2;
        public static final Integer AUTO_TRANSFER = 3;
    }
}
