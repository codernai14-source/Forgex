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
package com.forgex.sys.service;

import java.util.Map;

/**
 * 消息汇总服务接口
 * <p>
 * 提供用户登录时的消息汇总推送功能。
 * 查询未读消息数量，使用 UNREAD_SUMMARY 模板发送汇总消息。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-08
 * @see TemplateMessageService
 */
public interface MessageSummaryService {
    
    /**
     * 推送未读消息汇总
     * <p>
     * 查询当前用户的未读消息数量，使用模板发送汇总消息。
     * 模板编码：UNREAD_SUMMARY
     * 模板内容：尊敬的${userName}，您有${unreadCount}条消息未读
     * </p>
     *
     * @param userId 用户 ID
     * @param tenantId 租户 ID
     * @return 未读消息数量
     */
    Long pushUnreadSummary(Long userId, Long tenantId);
    
    /**
     * 获取未读消息数量
     *
     * @param userId 用户 ID
     * @param tenantId 租户 ID
     * @return 未读消息数量
     */
    Long getUnreadCount(Long userId, Long tenantId);
    
    /**
     * 获取未读消息汇总信息
     * <p>
     * 返回包含未读消息数量、汇总文本等信息的 Map。
     * </p>
     *
     * @param userId 用户 ID
     * @param tenantId 租户 ID
     * @return 汇总信息 Map
     */
    Map<String, Object> getUnreadSummaryInfo(Long userId, Long tenantId);
}
