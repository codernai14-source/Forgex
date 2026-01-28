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
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.sys.domain.dto.LoginLogQueryDTO;
import com.forgex.sys.domain.entity.LoginLog;
import com.forgex.sys.domain.vo.LoginLogVO;
import com.forgex.sys.mapper.LoginLogMapper;
import com.forgex.sys.service.ILoginLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 登录日志服务实现
 * 
 * @author coder_nai@163.com
 * @date 2025-01-13
 */
@Slf4j
@Service
public class LoginLogServiceImpl implements ILoginLogService {
    
    @Autowired
    private LoginLogMapper loginLogMapper;
    
    /**
     * 记录登录成功日志（异步）
     */
    @Async
    @Override
    public void recordLoginSuccess(Long userId, String account, Long tenantId, 
                                   String ip, String region, String userAgent, String tokenValue) {
        try {
            LoginLog loginLog = new LoginLog();
            loginLog.setUserId(userId);
            loginLog.setAccount(account);
            loginLog.setTenantId(tenantId);
            loginLog.setLoginIp(ip);
            loginLog.setLoginRegion(region);
            loginLog.setUserAgent(userAgent);
            loginLog.setTokenValue(tokenValue); // 保存token，用于后续记录登出信息
            loginLog.setLoginTime(LocalDateTime.now());
            loginLog.setStatus(1); // 成功
            loginLog.setCreateTime(LocalDateTime.now());
            
            loginLogMapper.insert(loginLog);
            log.info("记录登录成功日志：账号={}, IP={}, 地区={}", account, ip, region);
        } catch (Exception e) {
            log.error("记录登录成功日志失败：账号={}, IP={}", account, ip, e);
        }
    }
    
    /**
     * 记录登录失败日志（异步）
     */
    @Async
    @Override
    public void recordLoginFailure(String account, Long tenantId, 
                                   String ip, String region, String userAgent, String reason) {
        try {
            LoginLog loginLog = new LoginLog();
            loginLog.setAccount(account);
            loginLog.setTenantId(tenantId);
            loginLog.setLoginIp(ip);
            loginLog.setLoginRegion(region);
            loginLog.setUserAgent(userAgent);
            loginLog.setLoginTime(LocalDateTime.now());
            loginLog.setStatus(0); // 失败
            loginLog.setReason(reason);
            loginLog.setCreateTime(LocalDateTime.now());
            
            loginLogMapper.insert(loginLog);
            log.info("记录登录失败日志：账号={}, IP={}, 原因={}", account, ip, reason);
        } catch (Exception e) {
            log.error("记录登录失败日志失败：账号={}, IP={}", account, ip, e);
        }
    }

    /**
     * 分页查询登录日志
     */
    @Override
    public IPage<LoginLogVO> pageLoginLogs(Page<LoginLog> page, LoginLogQueryDTO query) {
        LambdaQueryWrapper<LoginLog> wrapper = new LambdaQueryWrapper<>();
        
        // 账号模糊查询
        if (StringUtils.hasText(query.getAccount())) {
            wrapper.like(LoginLog::getAccount, query.getAccount());
        }
        
        // 状态查询
        if (query.getStatus() != null) {
            wrapper.eq(LoginLog::getStatus, query.getStatus());
        }
        
        // 时间区间查询
        if (query.getStartTime() != null) {
            wrapper.ge(LoginLog::getLoginTime, query.getStartTime());
        }
        if (query.getEndTime() != null) {
            wrapper.le(LoginLog::getLoginTime, query.getEndTime());
        }
        
        // 按登录时间倒序
        wrapper.orderByDesc(LoginLog::getLoginTime);
        
        // 分页查询
        IPage<LoginLog> loginLogPage = loginLogMapper.selectPage(page, wrapper);
        
        // 转换为 VO
        IPage<LoginLogVO> voPage = loginLogPage.convert(loginLog -> {
            LoginLogVO vo = new LoginLogVO();
            BeanUtils.copyProperties(loginLog, vo);
            return vo;
        });
        
        return voPage;
    }
}
