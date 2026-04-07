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
package com.forgex.common.service;

import java.util.List;
import java.util.Map;

/**
 * 模板消息发送服务接口。
 * <p>
 * 提供基于消息模板的通用消息发送功能，支持：
 * <ul>
 *   <li>根据模板编码自动查找模板内容配置</li>
 *   <li>支持占位符替换（${变量名} 格式）</li>
 *   <li>支持多平台发送（站内、企业微信、短信、邮箱）</li>
 *   <li>支持显式指定接收人列表（适用于审批通知等场景）</li>
 *   <li>支持根据模板配置自动解析接收人（适用于定时通知等场景）</li>
 * </ul>
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-06
 */
public interface TemplateMessageService {

    /**
     * 使用模板发送消息（显式指定接收人）。
     * <p>
     * 适用于审批通知等需要动态指定接收人的场景。
     * 该方法根据模板编码查找模板内容，填充占位符后发送给指定用户。
     * </p>
     *
     * @param templateCode   模板编码，不能为空
     * @param receiverUserIds 接收人用户ID列表，不能为空
     * @param dataMap        占位符数据Map，可为空
     * @param bizType        业务类型，可为空（优先使用模板配置的业务类型）
     * @return 发送成功的消息数量，失败返回0
     * @throws IllegalArgumentException 当 templateCode 或 receiverUserIds 为空时抛出
     */
    int sendByTemplate(String templateCode, List<Long> receiverUserIds, Map<String, Object> dataMap, String bizType);

    /**
     * 使用模板发送消息（根据模板配置自动解析接收人）。
     * <p>
     * 适用于定时通知等接收人固定的场景。
     * 该方法根据模板中的接收人配置自动解析接收人列表。
     * </p>
     *
     * @param templateCode 模板编码，不能为空
     * @param dataMap      占位符数据Map，可为空
     * @return 发送成功的消息数量，失败返回0
     * @throws IllegalArgumentException 当 templateCode 为空时抛出
     */
    int sendByTemplate(String templateCode, Map<String, Object> dataMap);

    /**
     * 使用模板发送消息给单个用户。
     * <p>
     * 简化的单用户发送方法。
     * </p>
     *
     * @param templateCode  模板编码，不能为空
     * @param receiverUserId 接收人用户ID，不能为空
     * @param dataMap       占位符数据Map，可为空
     * @param bizType       业务类型，可为空
     * @return 发送成功返回消息ID，失败返回null
     */
    Long sendToUser(String templateCode, Long receiverUserId, Map<String, Object> dataMap, String bizType);

    /**
     * 检查模板是否存在且启用。
     *
     * @param templateCode 模板编码
     * @return true表示模板存在且启用，false表示不存在或已禁用
     */
    boolean isTemplateAvailable(String templateCode);
}