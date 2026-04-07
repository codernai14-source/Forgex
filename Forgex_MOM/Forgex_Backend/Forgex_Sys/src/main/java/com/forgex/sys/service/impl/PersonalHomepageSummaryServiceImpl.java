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
import com.forgex.sys.domain.entity.LoginLog;
import com.forgex.sys.domain.entity.SysUser;
import com.forgex.sys.domain.vo.PersonalHomepageSummaryVO;
import com.forgex.sys.mapper.LoginLogMapper;
import com.forgex.sys.mapper.SysUserMapper;
import com.forgex.sys.service.PersonalHomepageSummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * 个人首页摘要信息服务实现类。
 * <p>
 * 提供个人首页顶部用户卡片信息的查询功能，
 * 包括头像、昵称、在线时长和问候语的计算。
 * </p>
 * <p><strong>在线时长计算：</strong></p>
 * <ul>
 *   <li>从当前会话的登录记录计算</li>
 *   <li>登录时间到当前时间的差值</li>
 * </ul>
 * <p><strong>问候语生成：</strong></p>
 * <ul>
 *   <li>早晨（6:00-12:00）：早上好，祝您工作愉快！</li>
 *   <li>下午（12:00-18:00）：下午好，继续加油！</li>
 *   <li>晚上（18:00-6:00）：晚上好，注意休息！</li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PersonalHomepageSummaryServiceImpl implements PersonalHomepageSummaryService {

    /**
     * 用户Mapper
     */
    private final SysUserMapper userMapper;

    /**
     * 登录日志Mapper
     */
    private final LoginLogMapper loginLogMapper;

    /**
     * 早晨结束时间
     */
    private static final int MORNING_END_HOUR = 12;

    /**
     * 下午结束时间
     */
    private static final int AFTERNOON_END_HOUR = 18;

    @Override
    public PersonalHomepageSummaryVO getSummary(Long userId, Long tenantId) {
        // 参数校验
        if (userId == null || tenantId == null) {
            throw new IllegalArgumentException("用户ID和租户ID不能为空");
        }

        // 查询用户信息
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            log.warn("用户不存在: userId={}", userId);
            return createEmptySummary(userId);
        }

        // 构建VO
        PersonalHomepageSummaryVO vo = new PersonalHomepageSummaryVO();
        vo.setUserId(userId);
        vo.setAvatar(user.getAvatar());
        vo.setNickname(StringUtils.hasText(user.getUsername()) ? user.getUsername() : user.getAccount());
        vo.setAccount(user.getAccount());
        vo.setGender(user.getGender());

        // 计算在线时长
        calculateOnlineDuration(vo, userId, tenantId);

        // 生成问候语
        generateGreeting(vo);

        return vo;
    }

    /**
     * 计算在线时长。
     * <p>
     * 从当前会话的登录记录计算登录时间到现在的时长。
     * </p>
     *
     * @param vo      摘要信息VO
     * @param userId  用户ID
     * @param tenantId 租户ID
     */
    private void calculateOnlineDuration(PersonalHomepageSummaryVO vo, Long userId, Long tenantId) {
        try {
            // 查询今日最近的登录记录（未登出的会话）
            LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
            LoginLog loginLog = loginLogMapper.selectOne(new LambdaQueryWrapper<LoginLog>()
                    .eq(LoginLog::getUserId, userId)
                    .eq(LoginLog::getTenantId, tenantId)
                    .eq(LoginLog::getStatus, 1)
                    .ge(LoginLog::getLoginTime, todayStart)
                    .isNull(LoginLog::getLogoutTime)
                    .orderByDesc(LoginLog::getLoginTime)
                    .last("LIMIT 1"));

            if (loginLog == null || loginLog.getLoginTime() == null) {
                // 如果没有找到登录记录，尝试查找最近的成功登录记录
                loginLog = loginLogMapper.selectOne(new LambdaQueryWrapper<LoginLog>()
                        .eq(LoginLog::getUserId, userId)
                        .eq(LoginLog::getTenantId, tenantId)
                        .eq(LoginLog::getStatus, 1)
                        .ge(LoginLog::getLoginTime, todayStart)
                        .orderByDesc(LoginLog::getLoginTime)
                        .last("LIMIT 1"));
            }

            if (loginLog != null && loginLog.getLoginTime() != null) {
                LocalDateTime loginTime = loginLog.getLoginTime();
                LocalDateTime now = LocalDateTime.now();
                Duration duration = Duration.between(loginTime, now);

                long minutes = duration.toMinutes();
                vo.setOnlineDurationMinutes(minutes);
                vo.setOnlineDurationText(formatDuration(minutes));
            } else {
                vo.setOnlineDurationMinutes(0L);
                vo.setOnlineDurationText("0分钟");
            }
        } catch (Exception e) {
            log.error("计算在线时长失败: userId={}, tenantId={}", userId, tenantId, e);
            vo.setOnlineDurationMinutes(0L);
            vo.setOnlineDurationText("0分钟");
        }
    }

    /**
     * 生成问候语。
     * <p>
     * 根据当前时间段生成对应的问候语。
     * </p>
     *
     * @param vo 摘要信息VO
     */
    private void generateGreeting(PersonalHomepageSummaryVO vo) {
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();

        String greeting;
        if (hour >= 6 && hour < MORNING_END_HOUR) {
            greeting = "早上好，祝您工作愉快！";
        } else if (hour >= MORNING_END_HOUR && hour < AFTERNOON_END_HOUR) {
            greeting = "下午好，继续加油！";
        } else {
            greeting = "晚上好，注意休息！";
        }

        vo.setGreeting(greeting);

        // 生成问候语副标题（今天是星期X）
        String[] weekDays = {"星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"};
        int dayOfWeek = now.getDayOfWeek().getValue() - 1;
        String dateStr = now.format(DateTimeFormatter.ofPattern("M月d日"));
        vo.setGreetingSubtitle("今天是" + dateStr + " " + weekDays[dayOfWeek]);
    }

    /**
     * 格式化时长。
     *
     * @param minutes 分钟数
     * @return 格式化后的文本，例如 "2小时30分钟"
     */
    private String formatDuration(long minutes) {
        if (minutes <= 0) {
            return "0分钟";
        }

        long hours = minutes / 60;
        long remainMinutes = minutes % 60;

        if (hours > 0 && remainMinutes > 0) {
            return hours + "小时" + remainMinutes + "分钟";
        } else if (hours > 0) {
            return hours + "小时";
        } else {
            return remainMinutes + "分钟";
        }
    }

    /**
     * 创建空的摘要信息。
     *
     * @param userId 用户ID
     * @return 空的摘要信息VO
     */
    private PersonalHomepageSummaryVO createEmptySummary(Long userId) {
        PersonalHomepageSummaryVO vo = new PersonalHomepageSummaryVO();
        vo.setUserId(userId);
        vo.setAvatar(null);
        vo.setNickname("用户");
        vo.setAccount("");
        vo.setOnlineDurationMinutes(0L);
        vo.setOnlineDurationText("0分钟");
        vo.setGreeting("您好，欢迎回来！");
        vo.setGreetingSubtitle("");
        return vo;
    }
}