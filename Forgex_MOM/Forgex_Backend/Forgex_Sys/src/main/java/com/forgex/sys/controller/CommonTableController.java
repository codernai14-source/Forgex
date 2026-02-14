package com.forgex.sys.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.service.table.FxTableConfigService;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.tenant.UserContext;
import com.forgex.common.web.R;
import com.forgex.common.web.StatusCode;
import com.forgex.common.domain.dto.table.FxTableConfigDTO;
import com.forgex.common.domain.entity.table.FxTableConfig;
import com.forgex.common.domain.entity.table.FxTableColumnConfig;
import com.forgex.common.mapper.table.FxTableColumnConfigMapper;
import com.forgex.common.mapper.table.FxTableConfigMapper;
import com.forgex.sys.domain.param.TableConfigGetParam;
import com.forgex.sys.domain.param.TableConfigBatchDeleteParam;
import com.forgex.sys.domain.vo.TableConfigDetailVO;
import com.forgex.sys.enums.SysPromptEnum;
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
        wrapper.eq(FxTableConfig::getDeleted, false);

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
     * 
     * @param param 查询参数，包含id
     * @return 表格配置详情VO
     */
    @PostMapping("/info")
    public R<TableConfigDetailVO> info(@RequestBody TableConfigGetParam param) {
        if (param == null || param.getId() == null) {
            return R.fail(StatusCode.BUSINESS_ERROR, CommonPrompt.BAD_REQUEST, "ID不能为空");
        }
        
        Long id = param.getId();
        Long tenantId = TenantContext.get();
        
        // 查询表格配置
        FxTableConfig config = tableConfigMapper.selectById(id);
        if (config == null || Boolean.TRUE.equals(config.getDeleted())) {
            return R.fail(StatusCode.NOT_FOUND, CommonPrompt.NOT_FOUND);
        }
        
        // 查询列配置
        LambdaQueryWrapper<FxTableColumnConfig> columnWrapper = new LambdaQueryWrapper<>();
        columnWrapper.eq(FxTableColumnConfig::getTableCode, config.getTableCode())
                    .eq(FxTableColumnConfig::getDeleted, false)
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
     * 获取表格配置详情（包含列配置）- RESTful风格接口（保留兼容）
     * 
     * @param id 配置ID
     * @return 表格配置详情VO
     * @deprecated 建议使用 POST /info 接口
     */
    @GetMapping("/{id}")
    @Deprecated
    public R<TableConfigDetailVO> getDetail(@PathVariable Long id) {
        TableConfigGetParam param = new TableConfigGetParam();
        param.setId(id);
        return info(param);
    }

    /**
     * 创建表格配置
     */
    @RequirePerm("sys:tableConfig:add")
    @PostMapping
    public R<Long> create(@RequestBody TableConfigDetailVO vo) {
        Long tenantId = TenantContext.get();
        String currentUser = currentUser();
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
        config.setDeleted(false);
        
        tableConfigMapper.insert(config);
        
        // 保存列配置
        if (vo.getColumns() != null && !vo.getColumns().isEmpty()) {
            for (FxTableColumnConfig column : vo.getColumns()) {
                column.setId(null);
                column.setTableCode(vo.getTableCode());
                column.setTenantId(tenantId);
                column.setCreateBy(currentUser);
                column.setCreateTime(now);
                column.setDeleted(false);
                tableColumnConfigMapper.insert(column);
            }
        }
        
        return R.ok(config.getId());
    }

    /**
     * 更新表格配置
     * 
     * @param vo 表格配置详情VO
     * @return 操作结果
     */
    @RequirePerm("sys:tableConfig:edit")
    @PostMapping("/update")
    public R<Void> update(@RequestBody TableConfigDetailVO vo) {
        if (vo == null || vo.getId() == null) {
            return R.fail(StatusCode.BUSINESS_ERROR, CommonPrompt.BAD_REQUEST, "ID不能为空");
        }
        
        Long id = vo.getId();
        Long tenantId = TenantContext.get();
        String currentUser = currentUser();
        LocalDateTime now = LocalDateTime.now();
        
        // 更新表格配置
        FxTableConfig config = tableConfigMapper.selectById(id);
        if (config == null || Boolean.TRUE.equals(config.getDeleted())) {
            return R.fail(StatusCode.NOT_FOUND, CommonPrompt.NOT_FOUND);
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
                column.setDeleted(false);
                tableColumnConfigMapper.insert(column);
            }
        }
        
        return R.ok();
    }

    /**
     * 更新表格配置 - RESTful风格接口（保留兼容）
     * 
     * @param id 配置ID
     * @param vo 表格配置详情VO
     * @return 操作结果
     * @deprecated 建议使用 POST /update 接口
     */
    @PutMapping("/{id}")
    @Deprecated
    public R<Void> updateById(@PathVariable Long id, @RequestBody TableConfigDetailVO vo) {
        vo.setId(id);
        return update(vo);
    }

    /**
     * 删除表格配置
     * 
     * @param param 查询参数，包含id
     * @return 操作结果
     */
    @RequirePerm("sys:tableConfig:delete")
    @PostMapping("/delete")
    public R<Void> delete(@RequestBody TableConfigGetParam param) {
        if (param == null || param.getId() == null) {
            return R.fail(StatusCode.BUSINESS_ERROR, CommonPrompt.BAD_REQUEST, "ID不能为空");
        }
        
        Long id = param.getId();
        FxTableConfig config = tableConfigMapper.selectById(id);
        if (config == null) {
            return R.fail(StatusCode.NOT_FOUND, CommonPrompt.NOT_FOUND);
        }
        
        // 软删除表格配置
        config.setDeleted(true);
        tableConfigMapper.updateById(config);
        
        // 软删除列配置
        LambdaQueryWrapper<FxTableColumnConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FxTableColumnConfig::getTableCode, config.getTableCode());
        List<FxTableColumnConfig> columns = tableColumnConfigMapper.selectList(wrapper);
        for (FxTableColumnConfig column : columns) {
            column.setDeleted(true);
            tableColumnConfigMapper.updateById(column);
        }
        
        return R.ok();
    }

    /**
     * 删除表格配置 - RESTful风格接口（保留兼容）
     * 
     * @param id 配置ID
     * @return 操作结果
     * @deprecated 建议使用 POST /delete 接口
     */
    @DeleteMapping("/{id}")
    @Deprecated
    public R<Void> deleteById(@PathVariable Long id) {
        TableConfigGetParam param = new TableConfigGetParam();
        param.setId(id);
        return delete(param);
    }

    /**
     * 批量删除表格配置
     * 
     * @param param 查询参数，包含ids列表
     * @return 操作结果
     */
    @RequirePerm("sys:tableConfig:delete")
    @PostMapping("/batchDelete")
    public R<Void> batchDelete(@RequestBody TableConfigBatchDeleteParam param) {
        if (param == null || param.getIds() == null || param.getIds().isEmpty()) {
            return R.fail(StatusCode.BUSINESS_ERROR, CommonPrompt.BAD_REQUEST, "ID列表不能为空");
        }
        
        for (Long id : param.getIds()) {
            TableConfigGetParam deleteParam = new TableConfigGetParam();
            deleteParam.setId(id);
            delete(deleteParam);
        }
        
        return R.ok();
    }

    /**
     * 批量删除表格配置 - RESTful风格接口（保留兼容）
     * 
     * @param ids ID列表
     * @return 操作结果
     * @deprecated 建议使用 POST /batchDelete 接口
     */
    @DeleteMapping("/batch")
    @Deprecated
    public R<Void> batchDeleteByIds(@RequestBody List<Long> ids) {
        TableConfigBatchDeleteParam param = new TableConfigBatchDeleteParam();
        param.setIds(ids);
        return batchDelete(param);
    }

    /**
     * 更新表格配置状态
     * 
     * @param param 查询参数，包含id和enabled
     * @return 操作结果
     */
    @PostMapping("/updateStatus")
    public R<Void> updateStatus(@RequestBody TableConfigGetParam param) {
        if (param == null || param.getId() == null) {
            return R.fail(StatusCode.BUSINESS_ERROR, CommonPrompt.BAD_REQUEST, "ID不能为空");
        }
        
        Long id = param.getId();
        FxTableConfig config = tableConfigMapper.selectById(id);
        if (config == null || Boolean.TRUE.equals(config.getDeleted())) {
            return R.fail(StatusCode.NOT_FOUND, CommonPrompt.NOT_FOUND);
        }
        
        config.setEnabled(param.getEnabled());
        config.setUpdateBy(currentUser());
        config.setUpdateTime(LocalDateTime.now());
        tableConfigMapper.updateById(config);
        
        return R.ok();
    }

    /**
     * 更新表格配置状态 - RESTful风格接口（保留兼容）
     * 
     * @param id 配置ID
     * @param param 查询参数，包含enabled
     * @return 操作结果
     * @deprecated 建议使用 POST /updateStatus 接口
     */
    @PutMapping("/{id}/status")
    @Deprecated
    public R<Void> updateStatusById(@PathVariable Long id, @RequestBody TableConfigGetParam param) {
        param.setId(id);
        return updateStatus(param);
    }

    private String currentUser() {
        Long userId = UserContext.get();
        return userId == null ? "system" : userId.toString();
    }
}
