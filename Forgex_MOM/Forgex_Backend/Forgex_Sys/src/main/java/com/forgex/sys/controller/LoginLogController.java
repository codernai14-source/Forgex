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
package com.forgex.sys.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.web.R;
import com.forgex.sys.domain.dto.LoginLogQueryDTO;
import com.forgex.sys.domain.entity.LoginLog;
import com.forgex.sys.domain.vo.LoginLogVO;
import com.forgex.sys.service.ILoginLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录日志 Controller
 * 
 * 提供登录日志的查询接口，支持分页和条件筛选
 * 所有接口都需要权限控制（sys:loginLog:view）
 * 
 * @author coder_nai@163.com
 * @version 1.0.0
 * @date 2025-01-13
 * @see ILoginLogService
 * @see LoginLogVO
 */
@RestController
@RequestMapping("/sys/loginLog")
public class LoginLogController {
    
    @Autowired
    private ILoginLogService loginLogService;
    
    /**
     * 分页查询登录日志
     * <p>
     * 接口路径：POST /sys/loginLog/list
     * 需要权限：sys:loginLog:view
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收查询参数（包含 pageNum、pageSize 和筛选条件）</li>
     *   <li>创建 MyBatis-Plus 分页对象</li>
     *   <li>调用 Service 层分页查询登录日志 VO 列表</li>
     *   <li>返回分页结果</li>
     * </ol>
     *
     * @param query 查询参数
     *              - current: 页码（必填，从 1 开始）
     *              - size: 每页大小（必填）
     *              - userId: 用户 ID（可选）
     *              - username: 用户名（可选，模糊查询）
     *              - loginStatus: 登录状态（可选，0=失败，1=成功）
     *              - loginTimeFrom: 登录开始时间（可选）
     *              - loginTimeTo: 登录结束时间（可选）
     *              - ipAddress: IP 地址（可选，模糊查询）
     * @return {@link R} 包含登录日志分页列表的统一返回结构
     *         - code: 状态码（200=成功）
     *         - data: 分页结果（IPage&lt;LoginLogVO&gt;）
     *         - message: 提示信息
     * @see LoginLogQueryDTO
     * @see LoginLogVO
     */
    @PostMapping("/list")
    @RequirePerm("sys:loginLog:view")
    public R<IPage<LoginLogVO>> list(@RequestBody LoginLogQueryDTO query) {
        // 1. 创建 MyBatis-Plus 分页对象，使用查询参数中的 current 和 size
        Page<LoginLog> page = new Page<>(
            query.getCurrent() != null ? query.getCurrent() : 1,
            query.getSize() != null ? query.getSize() : 20
        );
        
        // 2. 委派给 Service 层分页查询登录日志 VO 列表
        IPage<LoginLogVO> result = loginLogService.pageLoginLogs(page, query);
        
        // 3. 返回分页结果
        return R.ok(result);
    }
}
