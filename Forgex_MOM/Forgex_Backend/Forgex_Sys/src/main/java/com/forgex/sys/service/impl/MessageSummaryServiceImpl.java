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
package com.forgex.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forgex.sys.domain.entity.SysMessage;
import com.forgex.sys.mapper.SysMessageMapper;
import com.forgex.sys.service.MessageSummaryService;
import com.forgex.common.service.TemplateMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息汇总服务实现类
 * <p>
 * 提供用户登录时的消息汇总推送功能。
 * 查询未读消息数量，使用 UNREAD_SUMMARY 模板发送汇总消息。
 * </p>
 * <p>
 * 模板配置：
 * <ul>
 *   <li>模板编码：UNREAD_SUMMARY</li>
 *   <li>占位符：${userName}、${unreadCount}</li>
 *   <li>接收人类型：CUSTOM（由本服务动态指定）</li>
 * </ul>
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-08
 * @see MessageSummaryService
 * @see TemplateMessageService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageSummaryServiceImpl implements MessageSummaryService {
    
    /**
     * 未读消息汇总模板编码
     */
    private static final String TEMPLATE_CODE_SUMMARY = "UNREAD_SUMMARY";
    
    private final SysMessageMapper messageMapper;
    private final TemplateMessageService templateMessageService;
    
    @Override
    public Long pushUnreadSummary(Long userId, Long tenantId) {
        if (userId == null || tenantId == null) {
            log.warn("用户 ID 或租户 ID 为空：userId={}, tenantId={}", userId, tenantId);
            return 0L;
        }
        
        // 1. 查询未读消息数量
        Long unreadCount = getUnreadCount(userId, tenantId);
        if (unreadCount == 0) {
            log.debug("用户未读消息为 0，不推送汇总：userId={}, tenantId={}", userId, tenantId);
            return 0L;
        }
        
        // 2. 查询用户信息
        String userName = queryUserName(userId);
        
        // 3. 构建占位符数据
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("userName", userName);
        dataMap.put("unreadCount", unreadCount);
        
        // 4. 使用模板发送汇总消息
        try {
            List<Long> receiverUserIds = List.of(userId);
            templateMessageService.sendByTemplate(TEMPLATE_CODE_SUMMARY, receiverUserIds, dataMap, "UNREAD_SUMMARY");
            log.info("推送未读消息汇总成功：userId={}, unreadCount={}", userId, unreadCount);
        } catch (Exception e) {
            log.error("推送未读消息汇总失败：userId={}, templateCode={}", userId, TEMPLATE_CODE_SUMMARY, e);
        }
        
        return unreadCount;
    }
    
    @Override
    public Long getUnreadCount(Long userId, Long tenantId) {
        if (userId == null || tenantId == null) {
            return 0L;
        }
        
        return messageMapper.selectCount(new LambdaQueryWrapper<SysMessage>()
                .eq(SysMessage::getReceiverUserId, userId)
                .eq(SysMessage::getStatus, 0)
                .eq(SysMessage::getDeleted, false));
    }
    
    @Override
    public Map<String, Object> getUnreadSummaryInfo(Long userId, Long tenantId) {
        Map<String, Object> result = new HashMap<>();
        
        if (userId == null || tenantId == null) {
            result.put("unreadCount", 0L);
            result.put("summary", "未登录");
            return result;
        }
        
        Long unreadCount = getUnreadCount(userId, tenantId);
        String userName = queryUserName(userId);
        
        result.put("unreadCount", unreadCount);
        result.put("userName", userName);
        result.put("summary", String.format("尊敬的%s，您有%d条消息未读", userName, unreadCount));
        
        return result;
    }
    
    /**
     * 查询用户名称
     * <p>
     * TODO: 注入 SysUserMapper，查询真实用户名称。
     * 当前临时返回用户 ID 作为名称。
     * </p>
     *
     * @param userId 用户 ID
     * @return 用户名称
     */
    private String queryUserName(Long userId) {
        // TODO: 实现真实的用户名称查询
        // 临时返回用户 ID 作为名称
        return "用户 (" + userId + ")";
    }
}
