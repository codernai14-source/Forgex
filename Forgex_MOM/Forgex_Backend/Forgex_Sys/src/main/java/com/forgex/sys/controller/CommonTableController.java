package com.forgex.sys.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.service.table.FxTableConfigService;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.tenant.UserContext;
import com.forgex.common.web.R;
import com.forgex.common.domain.dto.table.FxTableConfigDTO;
import com.forgex.common.domain.entity.table.FxTableConfig;
import com.forgex.common.domain.entity.table.FxTableColumnConfig;
import com.forgex.sys.domain.param.TableConfigGetParam;
import com.forgex.sys.domain.vo.TableConfigDetailVO;
import com.forgex.sys.enums.SysPromptEnum;
import com.forgex.sys.mapper.FxTableConfigMapper;
import com.forgex.sys.mapper.FxTableColumnConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/sys/common/table/config")
@RequiredArgsConstructor
public class CommonTableController {
    private final FxTableConfigService tableConfigService;
    private final FxTableConfigMapper tableConfigMapper;
    private final FxTableColumnConfigMapper tableColumnConfigMapper;

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

        LambdaQueryWrapper<FxTableConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FxTableConfig::getDeleted, 0);

        if (tableCode != null && !tableCode.isEmpty()) {
            wrapper.like(FxTableConfig::getTableCode, tableCode);
        }

        wrapper.orderByAsc(FxTableConfig::getId);

        Page<FxTableConfig> page = new Page<>(param != null && param.getCurrent() != null ? param.getCurrent() : 1, param != null && param.getSize() != null ? param.getSize() : 20);
        IPage<FxTableConfigDTO> result = tableConfigService.page(page, wrapper, tenantId, userId);

        return R.ok(result);
    }

    /**
     * 获取表格配置详情（包含列配置）
     */
    @GetMapping("/{id}")
    public R<TableConfigDetailVO> getDetail(@PathVariable Long id) {
        Long tenantId = TenantContext.get();
        
        // 查询表格配置
        FxTableConfig config = tableConfigMapper.selectById(id);
        if (config == null || config.getDeleted() == 1) {
            return R.fail(404, "表格配置不存在");
        }
        
        // 查询列配置
        LambdaQueryWrapper<FxTableColumnConfig> columnWrapper = new LambdaQueryWrapper<>();
        columnWrapper.eq(FxTableColumnConfig::getTableCode, config.getTableCode())
                    .eq(FxTableColumnConfig::getDeleted, 0)
                    .orderByAsc(FxTableColumnConfig::getOrderNum);
        List<FxTableColumnConfig> columns = tableColumnConfigMapper.selectList(columnWrapper);
        
        // 构建返回对象
        TableConfigDetailVO vo = new TableConfigDetailVO();
        vo.setId(config.getId());
        vo.setTableCode(config.getTableCode());
        vo.setTableNameI18nJson(config.getTableNameI18nJson());
        vo.setTableType(config.getTableType());
        vo.setRowKey(config.getRowKey());
        vo.setDefaultPageSize(config.getDefaultPageSize());
        vo.setDefaultSortJson(config.getDefaultSortJson());
        vo.setEnabled(config.getEnabled());
        vo.setVersion(config.getVersion());
        vo.setCreateBy(config.getCreateBy());
        vo.setCreateTime(config.getCreateTime());
        vo.setUpdateBy(config.getUpdateBy());
        vo.setUpdateTime(config.getUpdateTime());
        vo.setColumns(columns);
        
        return R.ok(vo);
    }

    /**
     * 创建表格配置
     */
    @PostMapping
    public R<Long> create(@RequestBody TableConfigDetailVO vo) {
        Long tenantId = TenantContext.get();
        String currentUser = UserContext.getAccount();
        LocalDateTime now = LocalDateTime.now();
        
        // 保存表格配置
        FxTableConfig config = new FxTableConfig();
        config.setTableCode(vo.getTableCode());
        config.setTableNameI18nJson(vo.getTableNameI18nJson());
        config.setTableType(vo.getTableType());
        config.setRowKey(vo.getRowKey());
        config.setDefaultPageSize(vo.getDefaultPageSize());
        config.setDefaultSortJson(vo.getDefaultSortJson());
        config.setEnabled(vo.getEnabled());
        config.setVersion(1);
        config.setTenantId(tenantId);
        config.setCreateBy(currentUser);
        config.setCreateTime(now);
        config.setDeleted(0);
        
        tableConfigMapper.insert(config);
        
        // 保存列配置
        if (vo.getColumns() != null && !vo.getColumns().isEmpty()) {
            for (FxTableColumnConfig column : vo.getColumns()) {
                column.setId(null);
                column.setTableCode(vo.getTableCode());
                column.setTenantId(tenantId);
                column.setCreateBy(currentUser);
                column.setCreateTime(now);
                column.setDeleted(0);
                tableColumnConfigMapper.insert(column);
            }
        }
        
        return R.ok(config.getId());
    }

    /**
     * 更新表格配置
     */
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody TableConfigDetailVO vo) {
        Long tenantId = TenantContext.get();
        String currentUser = UserContext.getAccount();
        LocalDateTime now = LocalDateTime.now();
        
        // 更新表格配置
        FxTableConfig config = tableConfigMapper.selectById(id);
        if (config == null || config.getDeleted() == 1) {
            return R.fail(404, "表格配置不存在");
        }
        
        config.setTableNameI18nJson(vo.getTableNameI18nJson());
        config.setTableType(vo.getTableType());
        config.setRowKey(vo.getRowKey());
        config.setDefaultPageSize(vo.getDefaultPageSize());
        config.setDefaultSortJson(vo.getDefaultSortJson());
        config.setEnabled(vo.getEnabled());
        config.setUpdateBy(currentUser);
        config.setUpdateTime(now);
        
        tableConfigMapper.updateById(config);
        
        // 删除旧的列配置
        LambdaQueryWrapper<FxTableColumnConfig> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(FxTableColumnConfig::getTableCode, config.getTableCode());
        tableColumnConfigMapper.delete(deleteWrapper);
        
        // 保存新的列配置
        if (vo.getColumns() != null && !vo.getColumns().isEmpty()) {
            for (FxTableColumnConfig column : vo.getColumns()) {
                column.setId(null);
                column.setTableCode(config.getTableCode());
                column.setTenantId(tenantId);
                column.setCreateBy(currentUser);
                column.setCreateTime(now);
                column.setDeleted(0);
                tableColumnConfigMapper.insert(column);
            }
        }
        
        return R.ok();
    }

    /**
     * 删除表格配置
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        FxTableConfig config = tableConfigMapper.selectById(id);
        if (config == null) {
            return R.fail(404, "表格配置不存在");
        }
        
        // 软删除表格配置
        config.setDeleted(1);
        tableConfigMapper.updateById(config);
        
        // 软删除列配置
        LambdaQueryWrapper<FxTableColumnConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FxTableColumnConfig::getTableCode, config.getTableCode());
        List<FxTableColumnConfig> columns = tableColumnConfigMapper.selectList(wrapper);
        for (FxTableColumnConfig column : columns) {
            column.setDeleted(1);
            tableColumnConfigMapper.updateById(column);
        }
        
        return R.ok();
    }

    /**
     * 批量删除表格配置
     */
    @DeleteMapping("/batch")
    public R<Void> batchDelete(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return R.fail(400, "ID列表不能为空");
        }
        
        for (Long id : ids) {
            delete(id);
        }
        
        return R.ok();
    }

    /**
     * 更新表格配置状态
     */
    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id, @RequestBody TableConfigGetParam param) {
        FxTableConfig config = tableConfigMapper.selectById(id);
        if (config == null || config.getDeleted() == 1) {
            return R.fail(404, "表格配置不存在");
        }
        
        config.setEnabled(param.getEnabled());
        config.setUpdateBy(UserContext.getAccount());
        config.setUpdateTime(LocalDateTime.now());
        tableConfigMapper.updateById(config);
        
        return R.ok();
    }
}
