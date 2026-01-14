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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.sys.domain.dto.LoginLogQueryDTO;
import com.forgex.sys.domain.entity.LoginLog;
import com.forgex.sys.domain.vo.LoginLogVO;

/**
 * 登录日志服务接口
 * 
 * @author coder_nai@163.com
 * @date 2025-01-13
 */
public interface ILoginLogService {
    
    /**
     * 记录登录成功日志
     * 
     * @param userId 用户ID
     * @param account 登录账号
     * @param tenantId 租户ID
     * @param ip 登录IP
     * @param region IP归属地
     * @param userAgent 浏览器UA
     */
    void recordLoginSuccess(Long userId, String account, Long tenantId, 
                           String ip, String region, String userAgent);
    
    /**
     * 记录登录失败日志
     * 
     * @param account 登录账号
     * @param tenantId 租户ID
     * @param ip 登录IP
     * @param region IP归属地
     * @param userAgent 浏览器UA
     * @param reason 失败原因
     */
    void recordLoginFailure(String account, Long tenantId, 
                           String ip, String region, String userAgent, String reason);
    
    /**
     * 分页查询登录日志
     * 
     * @param page 分页参数
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<LoginLogVO> pageLoginLogs(Page<LoginLog> page, LoginLogQueryDTO query);
}
