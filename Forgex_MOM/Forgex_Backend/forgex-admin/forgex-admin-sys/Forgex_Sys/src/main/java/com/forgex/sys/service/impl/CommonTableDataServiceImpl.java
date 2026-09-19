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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.domain.dto.table.FxTableConfigDTO;
import com.forgex.common.service.table.FxTableConfigService;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.tenant.UserContext;
import com.forgex.sys.domain.dto.LoginLogQueryDTO;
import com.forgex.sys.domain.entity.LoginLog;
import com.forgex.sys.domain.param.CommonTableQueryParam;
import com.forgex.sys.enums.SysPromptEnum;
import com.forgex.sys.service.CommonTableDataService;
import com.forgex.sys.service.ILoginLogService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 通用表格数据服务实现类
 * <p>负责处理通用表格数据查询的业务逻辑，包括登录日志查询等功能。</p>
 * 
 * @author coder_nai@163.com
 * @date 2026-01-16
 */
@Service
public class CommonTableDataServiceImpl implements CommonTableDataService {
    
    /**
     * 登录日志表格代码
     */
    private static final String TABLE_LOGIN_LOG = "sys.loginLog.list";
    
    /**
     * 日期时间格式化器
     */
    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 表格配置服务
     */
    private final FxTableConfigService tableConfigService;
    
    /**
     * 登录日志服务
     */
    private final ILoginLogService loginLogService;
    
    /**
     * 构造函数
     * 
     * @param tableConfigService 表格配置服务
     * @param loginLogService    登录日志服务
     */
    public CommonTableDataServiceImpl(FxTableConfigService tableConfigService, 
                                     ILoginLogService loginLogService) {
        this.tableConfigService = tableConfigService;
        this.loginLogService = loginLogService;
    }
    
    /**
     * 查询通用表格数据
     * 
     * @param param 查询参数，包含tableCode、当前页、每页大小和查询条件
     * @return 分页查询结果
     * @throws RuntimeException 当tableCode为空、不允许查询或配置不存在时抛出
     */
    @Override
    public IPage<?> queryTableData(CommonTableQueryParam param) {
        String tableCode = param == null ? null : param.getTableCode();
        if (!StringUtils.hasText(tableCode)) {
            throw new RuntimeException("tableCode不能为空");
        }
        
        if (!TABLE_LOGIN_LOG.equals(tableCode)) {
            throw new RuntimeException(SysPromptEnum.TABLE_QUERY_NOT_ALLOWED.getDefaultTemplate().replace("{0}", tableCode));
        }
        
        Long tenantId = TenantContext.get();
        Long userId = UserContext.get();
        FxTableConfigDTO cfg = tableConfigService.getTableConfig(tableCode, tenantId, userId);
        
        if (cfg == null) {
            throw new RuntimeException(SysPromptEnum.TABLE_CONFIG_NOT_FOUND.getDefaultTemplate().replace("{0}", tableCode));
        }
        
        // 获取允许查询的字段集合
        Set<String> allowQueryFields = new HashSet<>();
        if (cfg.getQueryFields() != null) {
            for (var q : cfg.getQueryFields()) {
                allowQueryFields.add(q.getField());
            }
        }
        
        Map<String, Object> q = param.getQuery();
        LoginLogQueryDTO dto = new LoginLogQueryDTO();
        dto.setCurrent(param.getCurrent() == null ? 1L : param.getCurrent());
        dto.setSize(param.getSize() == null ? 20L : param.getSize());
        dto.setTenantId(tenantId);
        
        // 解析查询条件
        if (q != null) {
            parseQueryConditions(q, allowQueryFields, dto);
        }
        
        // 创建分页对象
        Page<LoginLog> page = new Page<>(dto.getCurrent(), dto.getSize());
        
        // 调用分页查询方法
        return loginLogService.pageLoginLogs(page, dto);
    }
    
    /**
     * 解析查询条件
     * 
     * @param query            查询条件Map
     * @param allowQueryFields 允许查询的字段集合
     * @param dto              登录日志查询DTO
     */
    private void parseQueryConditions(Map<String, Object> query, 
                                     Set<String> allowQueryFields, 
                                     LoginLogQueryDTO dto) {
        if (allowQueryFields.contains("account") && query.containsKey("account")) {
            dto.setAccount(query.get("account") == null ? null : String.valueOf(query.get("account")));
        }
        
        if (allowQueryFields.contains("status") && query.containsKey("status") && query.get("status") != null) {
            try {
                dto.setStatus(Integer.valueOf(String.valueOf(query.get("status"))));
            } catch (Exception ignored) {}
        }
        
        if (allowQueryFields.contains("loginTime") && query.get("loginTime") instanceof java.util.List<?> list && list.size() == 2) {
            dto.setStartTime(parseTime(list.get(0)));
            dto.setEndTime(parseTime(list.get(1)));
        }
        
        if (allowQueryFields.contains("startTime") && query.containsKey("startTime") && query.get("startTime") != null) {
            dto.setStartTime(parseTime(query.get("startTime")));
        }
        
        if (allowQueryFields.contains("endTime") && query.containsKey("endTime") && query.get("endTime") != null) {
            dto.setEndTime(parseTime(query.get("endTime")));
        }
    }
    
    /**
     * 解析时间字符串为LocalDateTime对象
     * 
     * @param v 时间值，可以是字符串或LocalDateTime对象
     * @return 解析后的LocalDateTime对象，解析失败返回null
     */
    private LocalDateTime parseTime(Object v) {
        if (v == null) {
            return null;
        }
        
        if (v instanceof LocalDateTime ldt) {
            return ldt;
        }
        
        String s = String.valueOf(v);
        if (!StringUtils.hasText(s)) {
            return null;
        }
        
        try {
            return LocalDateTime.parse(s, DT);
        } catch (Exception e) {
            return null;
        }
    }
}