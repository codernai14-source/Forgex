package com.forgex.sys.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.service.table.FxTableConfigService;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.tenant.UserContext;
import com.forgex.common.web.R;
import com.forgex.common.domain.dto.table.FxTableConfigDTO;
import com.forgex.sys.domain.param.TableConfigGetParam;
import com.forgex.sys.enums.SysPromptEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/sys/common/table/config")
@RequiredArgsConstructor
public class CommonTableController {
    private final FxTableConfigService tableConfigService;

    @PostMapping("/get")
    public R<FxTableConfigDTO> get(@RequestBody TableConfigGetParam param) {
        String tableCode = param == null ? null : param.getTableCode();
        Long tenantId = TenantContext.get();
        Long userId = UserContext.get();
        FxTableConfigDTO cfg = tableConfigService.getTableConfig(tableCode, tenantId, userId);
        if (cfg == null) {
            return R.fail(500, SysPromptEnum.TABLE_CONFIG_NOT_FOUND, tableCode);
        }
        return R.ok(cfg);
    }

    @PostMapping("/list")
    public R<IPage<FxTableConfigDTO>> list(@RequestBody TableConfigGetParam param) {
        String tableCode = param == null ? null : param.getTableCode();
        Long tenantId = TenantContext.get();
        Long userId = UserContext.get();

        LambdaQueryWrapper<com.forgex.common.domain.entity.table.FxTableConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(com.forgex.common.domain.entity.table.FxTableConfig::getDeleted, 0);

        if (tableCode != null && !tableCode.isEmpty()) {
            wrapper.like(com.forgex.common.domain.entity.table.FxTableConfig::getTableCode, tableCode);
        }

        wrapper.orderByAsc(com.forgex.common.domain.entity.table.FxTableConfig::getId);

        Page<com.forgex.common.domain.entity.table.FxTableConfig> page = new Page<>(param != null && param.getCurrent() != null ? param.getCurrent() : 1, param != null && param.getSize() != null ? param.getSize() : 20);
        IPage<FxTableConfigDTO> result = tableConfigService.page(page, wrapper, tenantId, userId);

        return R.ok(result);
    }
}
