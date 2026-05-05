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
package com.forgex.workflow.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forgex.workflow.domain.entity.WfTaskConfig;
import com.forgex.workflow.domain.entity.WfTaskExecution;
import com.forgex.workflow.mapper.WfTaskConfigMapper;
import com.forgex.workflow.mapper.WfTaskExecutionMapper;
import com.forgex.workflow.service.IWfCallbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 审批回调服务实现类。
 * <p>
 * 提供审批回调的注册和触发功能实现。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 */
@Slf4j
@Service
@DS("workflow")
@RequiredArgsConstructor
public class WfCallbackServiceImpl implements IWfCallbackService {

    private final WfTaskExecutionMapper executionMapper;
    private final WfTaskConfigMapper taskConfigMapper;
    private final RestTemplate restTemplate;

    /**
     * 回调注册表（也可以使用数据库存储）
     */
    private final Map<String, CallbackConfig> callbackRegistry = new ConcurrentHashMap<>();

    /**
     * 回调配置
     */
    private static class CallbackConfig {
        private String callbackUrl;
        private String callbackBean;

        public CallbackConfig(String callbackUrl, String callbackBean) {
            this.callbackUrl = callbackUrl;
            this.callbackBean = callbackBean;
        }

        /**
         * 获取回调url。
         *
         * @return 字符串结果
         */
        public String getCallbackUrl() {
            return callbackUrl;
        }

        /**
         * 获取回调bean。
         *
         * @return 字符串结果
         */
        public String getCallbackBean() {
            return callbackBean;
        }
    }

    /**
     * 注册工作流回调处理器。
     *
     * @param taskCode 任务编码
     * @param callbackUrl 回调url
     * @param callbackBean 回调bean
     */
    @Override
    public void registerCallback(String taskCode, String callbackUrl, String callbackBean) {
        CallbackConfig config = new CallbackConfig(callbackUrl, callbackBean);
        callbackRegistry.put(taskCode, config);

        log.info("注册审批回调成功，taskCode={}, callbackUrl={}, callbackBean={}",
                taskCode, callbackUrl, callbackBean);
    }

    /**
     * 注销工作流回调处理器。
     *
     * @param taskCode 任务编码
     */
    @Override
    public void unregisterCallback(String taskCode) {
        CallbackConfig removed = callbackRegistry.remove(taskCode);
        if (removed != null) {
            log.info("注册工作流回调成功，taskCode={}", taskCode);
        } else {
            log.warn("注册工作流回调失败，回调处理器未注册，taskCode={}", taskCode);
        }
    }

    /**
     * 触发工作流回调。
     *
     * @param executionId 执行 ID
     * @param status 状态
     */
    @Override
    public void triggerCallback(Long executionId, Integer status) {
        try {
            // 获取执行记录
            WfTaskExecution execution = executionMapper.selectById(executionId);
            if (execution == null) {
                log.warn("审批执行记录不存在，executionId={}", executionId);
                return;
            }

            // 获取任务配置
            WfTaskConfig taskConfig = taskConfigMapper.selectById(execution.getTaskConfigId());
            if (taskConfig == null) {
                log.warn("审批任务配置不存在，taskConfigId={}", execution.getTaskConfigId());
                return;
            }

            // 获取回调配置
            CallbackConfig callbackConfig = resolveCallbackConfig(taskConfig);
            if (callbackConfig == null || !StringUtils.hasText(callbackConfig.getCallbackUrl())) {
                // 没有配置回调，直接返回
                log.debug("未配置回调，taskCode={}", taskConfig.getTaskCode());
                return;
            }

            // 构建回调参数
            Map<String, Object> params = new HashMap<>();
            params.put("executionId", executionId);
            params.put("taskCode", taskConfig.getTaskCode());
            params.put("taskName", taskConfig.getTaskName());
            params.put("status", status);
            params.put("formContent", execution.getFormContent());

            // 发送 HTTP 请求
            String callbackUrl = callbackConfig.getCallbackUrl();
            log.info("触发审批回调，executionId={}, callbackUrl={}", executionId, callbackUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(params, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(callbackUrl, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("审批回调成功，executionId={}", executionId);
            } else {
                log.error("审批回调失败，executionId={}, statusCode={}",
                        executionId, response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("触发审批回调异常，executionId={}", executionId, e);
            // 回调失败不影响主流程，仅记录日志
        }
    }

    /**
     * 解析审批回调配置。
     * <p>
     * 兼容历史内存注册方式，同时允许从任务配置表读取持久化回调地址。
     * </p>
     *
     * @param taskConfig 审批任务配置
     * @return 回调配置
     */
    private CallbackConfig resolveCallbackConfig(WfTaskConfig taskConfig) {
        CallbackConfig registeredConfig = callbackRegistry.get(taskConfig.getTaskCode());
        if (registeredConfig != null && StringUtils.hasText(registeredConfig.getCallbackUrl())) {
            return registeredConfig;
        }
        if (!StringUtils.hasText(taskConfig.getCallbackUrl()) && !StringUtils.hasText(taskConfig.getCallbackBean())) {
            return null;
        }
        return new CallbackConfig(taskConfig.getCallbackUrl(), taskConfig.getCallbackBean());
    }
}
