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
 * @author coder_nai@163.com
 * @date 2025-01-13
 */
@RestController
@RequestMapping("/sys/loginLog")
public class LoginLogController {
    
    @Autowired
    private ILoginLogService loginLogService;
    
    /**
     * 分页查询登录日志
     * 
     * @param query 查询条件（包含分页参数）
     * @return 分页结果
     */
    @PostMapping("/list")
    public R<IPage<LoginLogVO>> list(@RequestBody LoginLogQueryDTO query) {
        // 创建分页对象
        Page<LoginLog> page = new Page<>(
            query.getCurrent() != null ? query.getCurrent() : 1,
            query.getSize() != null ? query.getSize() : 20
        );
        
        // 分页查询
        IPage<LoginLogVO> result = loginLogService.pageLoginLogs(page, query);
        
        return R.ok(result);
    }
}
