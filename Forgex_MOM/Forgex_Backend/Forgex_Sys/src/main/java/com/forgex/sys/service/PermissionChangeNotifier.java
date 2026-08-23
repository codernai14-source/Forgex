/* Copyright 2026 coder_nai@163.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package com.forgex.sys.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 菜单权限变更通知器。
 * <p>权限数据只通过鉴权接口重新读取，SSE 事件仅作为失效信号。</p>
 */
@Service
@RequiredArgsConstructor
public class PermissionChangeNotifier {

    private final SseEmitterService sseEmitterService;

    /**
     * 在当前事务提交后通知租户内在线用户。
     *
     * @param tenantId 租户 ID
     * @param reason 变更原因
     */
    public void notifyAfterCommit(Long tenantId, String reason) {
        if (tenantId == null) {
            return;
        }

        Runnable notifyAction = () -> {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("tenantId", tenantId);
            payload.put("reason", reason == null ? "permission-change" : reason);
            payload.put("timestamp", System.currentTimeMillis());
            sseEmitterService.sendToTenant(tenantId, "permission-changed", payload);
        };

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            notifyAction.run();
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                notifyAction.run();
            }
        });
    }
}
