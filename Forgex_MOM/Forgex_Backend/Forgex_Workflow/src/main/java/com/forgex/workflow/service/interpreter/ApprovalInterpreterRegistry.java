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
package com.forgex.workflow.service.interpreter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 审批解释器注册表。
 * <p>
 * 用于注册和管理所有的审批解释器实现。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 */
@Slf4j
@Component
public class ApprovalInterpreterRegistry {
    
    /**
     * 审批解释器缓存
     * key: Bean 名称
     * value: 审批解释器实例
     */
    private static final Map<String, IApprovalInterpreter> INTERPRETER_CACHE = new ConcurrentHashMap<>();
    
    private final ApplicationContext applicationContext;
    
    @Autowired
    public ApprovalInterpreterRegistry(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        log.info("审批解释器注册表初始化完成");
    }
    
    /**
     * 获取审批解释器
     * 
     * @param beanName Bean 名称
     * @return 审批解释器实例，如果不存在则返回 null
     */
    public IApprovalInterpreter getInterpreter(String beanName) {
        if (beanName == null || beanName.trim().isEmpty()) {
            return null;
        }
        
        return INTERPRETER_CACHE.computeIfAbsent(beanName, key -> {
            try {
                IApprovalInterpreter interpreter = applicationContext.getBean(key, IApprovalInterpreter.class);
                log.info("加载审批解释器：{}", key);
                return interpreter;
            } catch (Exception e) {
                log.warn("审批解释器不存在：{}", key);
                return null;
            }
        });
    }
    
    /**
     * 注册审批解释器
     * 
     * @param beanName Bean 名称
     * @param interpreter 审批解释器实例
     */
    public void registerInterpreter(String beanName, IApprovalInterpreter interpreter) {
        INTERPRETER_CACHE.put(beanName, interpreter);
        log.info("注册审批解释器：{}", beanName);
    }
    
    /**
     * 清除缓存
     */
    public void clearCache() {
        INTERPRETER_CACHE.clear();
        log.info("清除审批解释器缓存");
    }
}