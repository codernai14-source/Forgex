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
package com.forgex.sys.domain.vo;

import lombok.Data;

/**
 * 个人首页摘要信息VO。
 * <p>
 * 用于展示个人首页顶部的用户卡片信息，包含：
 * <ul>
 *   <li>用户头像</li>
 *   <li>用户昵称</li>
 *   <li>在线时长（今日累计）</li>
 *   <li>问候语（根据时间段动态生成）</li>
 * </ul>
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-06
 */
@Data
public class PersonalHomepageSummaryVO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户头像URL
     */
    private String avatar;

    /**
     * 用户昵称（显示名）
     */
    private String nickname;

    /**
     * 用户账号
     */
    private String account;

    /**
     * 用户性别（0=未知，1=男，2=女）
     */
    private Integer gender;

    /**
     * 在线时长（分钟）
     * <p>
     * 计算规则：当前会话从登录到现在的时长
     * </p>
     */
    private Long onlineDurationMinutes;

    /**
     * 格式化的在线时长
     * <p>
     * 例如：2小时30分钟
     * </p>
     */
    private String onlineDurationText;

    /**
     * 问候语
     * <p>
     * 根据当前时间段动态生成：
     * <ul>
     *   <li>早晨（6:00-12:00）：早上好，祝您工作愉快！</li>
     *   <li>下午（12:00-18:00）：下午好，继续加油！</li>
     *   <li>晚上（18:00-6:00）：晚上好，注意休息！</li>
     * </ul>
     * </p>
     */
    private String greeting;

    /**
     * 问候语副标题
     * <p>
     * 例如：今天是星期一
     * </p>
     */
    private String greetingSubtitle;
}