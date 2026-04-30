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
package com.forgex.workflow.controller;

import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.web.R;
import com.forgex.workflow.service.IWfCallbackService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 审批回调控制器
 * <p>
 * 处理审批回调相关的 HTTP 请求，提供回调接口的注册、注销等管理功能。
 * 用于在审批流程的关键节点（如审批通过、驳回、取消等）触发外部系统的回调通知。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 * @see IWfCallbackService 审批回调服务接口
 */
@Slf4j
@RestController
@RequestMapping("/wf/callback")
@RequiredArgsConstructor
public class WfCallbackController {
    
    private final IWfCallbackService callbackService;
    
    /**
     * 注册审批回调接口
     * <p>
     * 为指定的审批任务配置注册回调接口，当审批状态发生变化时会调用注册的回调地址
     * </p>
     *
     * @param param 回调注册参数，包含任务编码、回调 URL、回调 Bean 名称（必填）
     * @return 操作成功响应
     * @throws I18nBusinessException 当参数校验失败或注册失败时抛出业务异常
     * @see IWfCallbackService#registerCallback(String, String, String) 注册回调服务方法
     * @see CallbackRegisterParam 回调注册参数类
     */
    @PostMapping("/register")
    public R<Void> registerCallback(@RequestBody CallbackRegisterParam param) {
        callbackService.registerCallback(param.getTaskCode(), 
                                        param.getCallbackUrl(), 
                                        param.getCallbackBean());
        return R.ok(CommonPrompt.OPERATION_SUCCESS);
    }
    
    /**
     * 注销审批回调接口
     * <p>
     * 移除已注册的审批任务回调接口，停止回调通知
     * </p>
     *
     * @param params 请求参数，包含 taskCode（任务编码，必填）
     * @return 操作成功响应
     * @throws I18nBusinessException 当参数无效或注销失败时抛出业务异常
     * @see IWfCallbackService#unregisterCallback(String) 注销回调服务方法
     */
    @PostMapping("/unregister")
    public R<Void> unregisterCallback(@RequestBody Map<String, Object> params) {
        String taskCode = params.get("taskCode").toString();
        callbackService.unregisterCallback(taskCode);
        return R.ok(CommonPrompt.OPERATION_SUCCESS);
    }
    
    /**
     * 回调注册参数
     * <p>
     * 用于封装注册审批回调接口时所需的参数信息
     * </p>
     *
     * @author coder_nai@163.com
     * @version 1.0.0
     * @since 2026-04-01
     */
    @Data
    public static class CallbackRegisterParam {
        /**
         * 任务编码
         * <p>
         * 唯一标识一个审批任务配置，用于关联回调接口与审批任务
         * </p>
         */
        private String taskCode;
        
        /**
         * 回调 URL
         * <p>
         * 外部系统接收回调通知的 HTTP 接口地址
         * </p>
         */
        private String callbackUrl;
        
        /**
         * 回调 Bean 名称
         * <p>
         * Spring 容器中回调处理器的 Bean 名称，用于内部回调处理
         * </p>
         */
        private String callbackBean;
    }
}