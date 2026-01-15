package com.forgex.sys.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.forgex.common.domain.dto.table.FxTableConfigDTO;
import com.forgex.common.service.table.FxTableConfigService;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.tenant.UserContext;
import com.forgex.common.web.R;
import com.forgex.sys.domain.dto.LoginLogQueryDTO;
import com.forgex.sys.domain.param.CommonTableQueryParam;
import com.forgex.sys.enums.SysPromptEnum;
import com.forgex.sys.service.ILoginLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/sys/common/table/data")
@RequiredArgsConstructor
public class CommonTableDataController {
    private static final String TABLE_LOGIN_LOG = "sys.loginLog.list";
    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final FxTableConfigService tableConfigService;
    private final ILoginLogService loginLogService;

    @PostMapping("/query")
    public R<IPage<?>> query(@RequestBody CommonTableQueryParam param) {
        String tableCode = param == null ? null : param.getTableCode();
        if (!StringUtils.hasText(tableCode)) {
            return R.fail(400, "tableCode不能为空");
        }
        if (!TABLE_LOGIN_LOG.equals(tableCode)) {
            return R.fail(403, SysPromptEnum.TABLE_QUERY_NOT_ALLOWED, tableCode);
        }
        Long tenantId = TenantContext.get();
        Long userId = UserContext.get();
        FxTableConfigDTO cfg = tableConfigService.getTableConfig(tableCode, tenantId, userId);
        if (cfg == null) {
            return R.fail(404, SysPromptEnum.TABLE_CONFIG_NOT_FOUND, tableCode);
        }

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

        if (q != null) {
            if (allowQueryFields.contains("account") && q.containsKey("account")) {
                dto.setAccount(q.get("account") == null ? null : String.valueOf(q.get("account")));
            }
            if (allowQueryFields.contains("status") && q.containsKey("status") && q.get("status") != null) {
                try {
                    dto.setStatus(Integer.valueOf(String.valueOf(q.get("status"))));
                } catch (Exception ignored) {}
            }
            if (allowQueryFields.contains("loginTime") && q.get("loginTime") instanceof java.util.List<?> list && list.size() == 2) {
                dto.setStartTime(parseTime(list.get(0)));
                dto.setEndTime(parseTime(list.get(1)));
            }
            if (allowQueryFields.contains("startTime") && q.containsKey("startTime") && q.get("startTime") != null) {
                dto.setStartTime(parseTime(q.get("startTime")));
            }
            if (allowQueryFields.contains("endTime") && q.containsKey("endTime") && q.get("endTime") != null) {
                dto.setEndTime(parseTime(q.get("endTime")));
            }
        }

        IPage<?> page = (IPage<?>) loginLogService.pageLoginLogs(dto);
        return R.ok(page);
    }

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
