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
 * 审批回调 Controller。
 * <p>
 * 提供审批回调的注册和管理接口。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 */
@Slf4j
@RestController
@RequestMapping("/wf/callback")
@RequiredArgsConstructor
public class WfCallbackController {
    
    private final IWfCallbackService callbackService;
    
    /**
     * 注册回调
     *
     * @param param 回调参数
     * @return 是否成功
     */
    @PostMapping("/register")
    public R<Void> registerCallback(@RequestBody CallbackRegisterParam param) {
        callbackService.registerCallback(param.getTaskCode(), 
                                        param.getCallbackUrl(), 
                                        param.getCallbackBean());
        return R.ok(CommonPrompt.OPERATION_SUCCESS);
    }
    
    /**
     * 注销回调
     *
     * @param params 参数（taskCode）
     * @return 是否成功
     */
    @PostMapping("/unregister")
    public R<Void> unregisterCallback(@RequestBody Map<String, Object> params) {
        String taskCode = params.get("taskCode").toString();
        callbackService.unregisterCallback(taskCode);
        return R.ok(CommonPrompt.OPERATION_SUCCESS);
    }
    
    /**
     * 回调参数
     */
    @Data
    public static class CallbackRegisterParam {
        /**
         * 任务编码
         */
        private String taskCode;
        
        /**
         * 回调 URL
         */
        private String callbackUrl;
        
        /**
         * 回调 Bean 名称
         */
        private String callbackBean;
    }
}