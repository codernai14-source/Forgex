package com.forgex.sys.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.domain.dto.table.FxTableColumnDTO;
import com.forgex.common.domain.dto.table.FxTableConfigDTO;
import com.forgex.common.domain.dto.table.FxUserTableConfigDTO;
import com.forgex.common.domain.entity.table.FxTableConfig;
import com.forgex.common.domain.entity.table.FxTableColumnConfig;
import com.forgex.common.domain.entity.table.FxUserTableConfig;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.mapper.table.FxTableColumnConfigMapper;
import com.forgex.common.mapper.table.FxTableConfigMapper;
import com.forgex.common.mapper.table.FxUserTableConfigMapper;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.service.table.FxTableConfigService;
import com.forgex.common.service.table.FxUserTableConfigService;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.tenant.UserContext;
import com.forgex.common.web.R;
import com.forgex.common.web.StatusCode;
import com.forgex.sys.domain.param.TableConfigBatchDeleteParam;
import com.forgex.sys.domain.param.TableConfigGetParam;
import com.forgex.sys.domain.param.UserColumnConfigParam;
import com.forgex.sys.domain.param.UserColumnItemParam;
import com.forgex.sys.domain.vo.TableConfigDetailVO;
import com.forgex.sys.enums.SysPromptEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * common表格控制器。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/sys/common/table/config")
@RequiredArgsConstructor
public class CommonTableController {
    private static final int MIN_COLUMN_WIDTH = 60;
    private static final int MAX_COLUMN_WIDTH = 800;
    private static final String ACTION_FIELD = "action";

    private final FxTableConfigService tableConfigService;
    private final FxTableConfigMapper tableConfigMapper;
    private final FxTableColumnConfigMapper tableColumnConfigMapper;
    private final FxUserTableConfigService userTableConfigService;
    private final FxUserTableConfigMapper userTableConfigMapper;

    /**
     * 查询数据详情。
     *
     * @param param 请求参数
     * @return 统一响应结果
     */
    @RequirePerm("sys:tableConfig:view")
    @PostMapping("/get")
    public R<FxTableConfigDTO> get(@RequestBody TableConfigGetParam param) {
        String tableCode = param == null ? null : param.getTableCode();
        Long tenantId = resolveConfigTenantId(param.getIsPublicConfig());
        Long userId = UserContext.get();
        FxTableConfigDTO cfg = tableConfigService.getTableConfig(tableCode, tenantId, userId);
        if (cfg == null) {
            return R.fail(500, SysPromptEnum.TABLE_CONFIG_NOT_FOUND, tableCode);
        }
        return R.ok(cfg);
    }

    /**
     * 获取表格配置（支持公共配置模式）
     * <p>
     * 根据公共配置模式参数获取表格配置。
     * isPublicConfig=true 时查询公共配置（tenantId=0），false 时查询租户配置。
     * </p>
     *
     * @param param 查询参数，包含 tableCode 和 isPublicConfig
     * @return 表格配置 DTO
     */
    @RequirePerm("sys:tableConfig:view")
    @PostMapping("/getWithMode")
    public R<FxTableConfigDTO> getWithMode(@RequestBody TableConfigGetParam param) {
        String tableCode = param == null ? null : param.getTableCode();
        Boolean isPublicConfig = param == null ? false : param.getIsPublicConfig();
        Long tenantId = TenantContext.get();

        if (isPublicConfig == null) {
            isPublicConfig = false;
        }

        FxTableConfigDTO cfg = tableConfigService.getTableConfig(tableCode, tenantId, isPublicConfig);
        if (cfg == null) {
            return R.fail(500, SysPromptEnum.TABLE_CONFIG_NOT_FOUND, tableCode);
        }
        return R.ok(cfg);
    }

    /**
     * 查询数据列表。
     *
     * @param param 请求参数
     * @return 统一响应结果
     */
    @RequirePerm("sys:tableConfig:view")
    @PostMapping("/list")
    public R<IPage<FxTableConfigDTO>> list(@RequestBody TableConfigGetParam param) {
        String tableCode = param == null ? null : param.getTableCode();
        Long tenantId = resolveConfigTenantId(param == null ? null : param.getIsPublicConfig());
        Long userId = UserContext.get();

        LambdaQueryWrapper<FxTableConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FxTableConfig::getDeleted, false);
        wrapper.eq(FxTableConfig::getTenantId, tenantId);

        if (tableCode != null && !tableCode.isEmpty()) {
            wrapper.like(FxTableConfig::getTableCode, tableCode);
        }
        if (param != null && StringUtils.hasText(param.getTableName())) {
            wrapper.like(FxTableConfig::getTableNameI18nJson, param.getTableName());
        }
        if (param != null && StringUtils.hasText(param.getTableType())) {
            wrapper.eq(FxTableConfig::getTableType, param.getTableType());
        }
        if (param != null && param.getEnabled() != null) {
            wrapper.eq(FxTableConfig::getEnabled, param.getEnabled());
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
    @RequirePerm("sys:tableConfig:view")
    @PostMapping("/info")
    public R<TableConfigDetailVO> info(@RequestBody TableConfigGetParam param) {
        if (param == null || param.getId() == null) {
            return R.fail(StatusCode.BUSINESS_ERROR, CommonPrompt.BAD_REQUEST, "ID不能为空");
        }

        Long id = param.getId();
        Long tenantId = resolveConfigTenantId(param.getIsPublicConfig());

        // 查询表格配置
        FxTableConfig config = tableConfigMapper.selectById(id);
        if (config == null || Boolean.TRUE.equals(config.getDeleted()) || !tenantId.equals(config.getTenantId())) {
            return R.fail(StatusCode.NOT_FOUND, CommonPrompt.NOT_FOUND);
        }

        // 查询列配置
        LambdaQueryWrapper<FxTableColumnConfig> columnWrapper = new LambdaQueryWrapper<>();
        columnWrapper.eq(FxTableColumnConfig::getTableCode, config.getTableCode())
                    .eq(FxTableColumnConfig::getTenantId, tenantId)
                    .eq(FxTableColumnConfig::getDeleted, false)
                    .orderByAsc(FxTableColumnConfig::getOrderNum);
        List<FxTableColumnConfig> columns = tableColumnConfigMapper.selectList(columnWrapper);
        if (!isPublicTenant(tenantId) && (columns == null || columns.isEmpty())) {
            LambdaQueryWrapper<FxTableColumnConfig> fallbackWrapper = new LambdaQueryWrapper<>();
            fallbackWrapper.eq(FxTableColumnConfig::getTableCode, config.getTableCode())
                    .eq(FxTableColumnConfig::getTenantId, 0L)
                    .eq(FxTableColumnConfig::getDeleted, false)
                    .orderByAsc(FxTableColumnConfig::getOrderNum);
            columns = tableColumnConfigMapper.selectList(fallbackWrapper);
        }

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
        vo.setPublicConfig(isPublicTenant(tenantId));
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
        Long tenantId = resolveConfigTenantId(vo == null ? null : vo.getPublicConfig());
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
     * 拉取公共配置。
     *
     * @return 统一响应结果
     */
    @RequirePerm("sys:tableConfig:add")
    @PostMapping("/pull-public")
    public R<Integer> pullPublicConfig() {
        Long tenantId = TenantContext.get();
        if (tenantId == null || tenantId == 0L) {
            return R.ok(CommonPrompt.OPERATION_SUCCESS, 0);
        }

        List<FxTableConfig> publicConfigs = tableConfigMapper.selectList(new LambdaQueryWrapper<FxTableConfig>()
                .eq(FxTableConfig::getTenantId, 0L)
                .eq(FxTableConfig::getDeleted, false)
                .orderByAsc(FxTableConfig::getId));
        if (publicConfigs == null || publicConfigs.isEmpty()) {
            return R.ok(CommonPrompt.OPERATION_SUCCESS, 0);
        }

        String currentUser = currentUser();
        LocalDateTime now = LocalDateTime.now();
        int count = 0;
        for (FxTableConfig publicConfig : publicConfigs) {
            FxTableConfig existed = tableConfigMapper.selectOne(new LambdaQueryWrapper<FxTableConfig>()
                    .eq(FxTableConfig::getTenantId, tenantId)
                    .eq(FxTableConfig::getTableCode, publicConfig.getTableCode())
                    .eq(FxTableConfig::getDeleted, false)
                    .last("limit 1"));
            if (existed != null) {
                continue;
            }

            FxTableConfig target = new FxTableConfig();
            BeanUtils.copyProperties(publicConfig, target, "id", "tenantId", "createBy", "createTime", "updateBy", "updateTime", "deleted");
            target.setId(null);
            target.setTenantId(tenantId);
            target.setCreateBy(currentUser);
            target.setCreateTime(now);
            target.setUpdateBy(currentUser);
            target.setUpdateTime(now);
            target.setDeleted(false);
            tableConfigMapper.insert(target);

            List<FxTableColumnConfig> columns = tableColumnConfigMapper.selectList(new LambdaQueryWrapper<FxTableColumnConfig>()
                    .eq(FxTableColumnConfig::getTenantId, 0L)
                    .eq(FxTableColumnConfig::getTableCode, publicConfig.getTableCode())
                    .eq(FxTableColumnConfig::getDeleted, false)
                    .orderByAsc(FxTableColumnConfig::getOrderNum));
            for (FxTableColumnConfig publicColumn : columns) {
                FxTableColumnConfig targetColumn = new FxTableColumnConfig();
                BeanUtils.copyProperties(publicColumn, targetColumn, "id", "tenantId", "createBy", "createTime", "updateBy", "updateTime", "deleted");
                targetColumn.setId(null);
                targetColumn.setTenantId(tenantId);
                targetColumn.setCreateBy(currentUser);
                targetColumn.setCreateTime(now);
                targetColumn.setUpdateBy(currentUser);
                targetColumn.setUpdateTime(now);
                targetColumn.setDeleted(false);
                tableColumnConfigMapper.insert(targetColumn);
            }
            count++;
        }

        return R.ok(CommonPrompt.OPERATION_SUCCESS, count);
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
        Long tenantId = resolveConfigTenantId(vo.getPublicConfig());
        String currentUser = currentUser();
        LocalDateTime now = LocalDateTime.now();

        // 更新表格配置
        FxTableConfig config = tableConfigMapper.selectById(id);
        if (config == null || Boolean.TRUE.equals(config.getDeleted()) || !tenantId.equals(config.getTenantId())) {
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
        deleteWrapper.eq(FxTableColumnConfig::getTenantId, tenantId)
                .eq(FxTableColumnConfig::getTableCode, config.getTableCode());
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
        Long tenantId = resolveConfigTenantId(param.getIsPublicConfig());
        FxTableConfig config = tableConfigMapper.selectById(id);
        if (config == null || !tenantId.equals(config.getTenantId())) {
            return R.fail(StatusCode.NOT_FOUND, CommonPrompt.NOT_FOUND);
        }

        // 软删除表格配置
        config.setDeleted(true);
        tableConfigMapper.updateById(config);

        // 软删除列配置
        LambdaQueryWrapper<FxTableColumnConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FxTableColumnConfig::getTenantId, tenantId)
                .eq(FxTableColumnConfig::getTableCode, config.getTableCode());
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
     * 批量删除数据。
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
            deleteParam.setIsPublicConfig(param.getIsPublicConfig());
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
    @RequirePerm("sys:tableConfig:edit")
    @PostMapping("/updateStatus")
    public R<Void> updateStatus(@RequestBody TableConfigGetParam param) {
        if (param == null || param.getId() == null) {
            return R.fail(StatusCode.BUSINESS_ERROR, CommonPrompt.BAD_REQUEST, "ID不能为空");
        }

        Long id = param.getId();
        Long tenantId = resolveConfigTenantId(param.getIsPublicConfig());
        FxTableConfig config = tableConfigMapper.selectById(id);
        if (config == null || Boolean.TRUE.equals(config.getDeleted()) || !tenantId.equals(config.getTenantId())) {
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

    /**
     * 获取用户列配置
     * <p>
     * 获取当前用户对指定表格的个性化列配置，包括列的显示/隐藏和排序。
     * </p>
     *
     * @param param 参数，包含 tableCode
     * @return 用户列配置列表
     */
    @PostMapping("/user/columns")
    public R<FxUserTableConfigDTO> getUserColumns(@RequestBody TableConfigGetParam param) {
        if (param == null || !StringUtils.hasText(param.getTableCode())) {
            return R.fail(StatusCode.BUSINESS_ERROR, CommonPrompt.BAD_REQUEST, "tableCode 不能为空");
        }

        Long tenantId = TenantContext.get();
        Long userId = UserContext.get();

        if (userId == null) {
            return R.fail(StatusCode.UNAUTHORIZED, CommonPrompt.NOT_LOGIN);
        }

        // 获取用户配置
        FxUserTableConfigDTO userConfig = userTableConfigService.getUserTableConfig(param.getTableCode(), tenantId, userId);

        if (userConfig == null) {
            // 用户没有配置，返回空对象
            FxUserTableConfigDTO emptyConfig = new FxUserTableConfigDTO();
            emptyConfig.setTableCode(param.getTableCode());
            emptyConfig.setUserId(userId);
            emptyConfig.setTenantId(tenantId);
            emptyConfig.setColumns(null);
            return R.ok(emptyConfig);
        }

        return R.ok(userConfig);
    }

    /**
     * 保存用户列配置
     * <p>
     * 保存当前用户对指定表格的个性化列配置，包括列的显示/隐藏和排序。
     * </p>
     *
     * @param param 用户列配置参数
     * @return 配置 ID
     */
    @PostMapping("/user/columns/save")
    public R<Long> saveUserColumns(@RequestBody UserColumnConfigParam param) {
        if (param == null || !StringUtils.hasText(param.getTableCode())) {
            return R.fail(StatusCode.BUSINESS_ERROR, CommonPrompt.BAD_REQUEST, "tableCode 不能为空");
        }

        if (param.getColumns() == null || param.getColumns().isEmpty()) {
            return R.fail(StatusCode.BUSINESS_ERROR, CommonPrompt.BAD_REQUEST, "列配置不能为空");
        }

        Long tenantId = TenantContext.get();
        Long userId = UserContext.get();

        if (userId == null) {
            return R.fail(StatusCode.UNAUTHORIZED, CommonPrompt.NOT_LOGIN);
        }

        // 获取原始表格配置
        // 保存列设置时必须基于完整基础列配置合并，不能使用当前用户已过滤掉隐藏列的结果
        FxTableConfigDTO baseConfig = tableConfigService.getTableConfig(param.getTableCode(), tenantId, null);
        if (baseConfig == null || baseConfig.getColumns() == null) {
            return R.fail(StatusCode.NOT_FOUND, SysPromptEnum.TABLE_CONFIG_NOT_FOUND, param.getTableCode());
        }

        // 构建用户配置 DTO
        FxUserTableConfigDTO dto = new FxUserTableConfigDTO();
        dto.setTableCode(param.getTableCode());
        dto.setUserId(userId);
        dto.setTenantId(tenantId);

        // 合并列配置：基于原始配置，根据用户参数更新 visible 和 order
        List<FxTableColumnDTO> mergedColumns = mergeColumnConfig(baseConfig.getColumns(), param.getColumns());
        dto.setColumns(mergedColumns);

        // 保存用户配置
        Long configId = userTableConfigService.saveUserTableConfig(dto, userId);

        return R.ok(configId);
    }

    /**
     * 重置用户列配置
     * <p>
     * 删除当前用户对指定表格的个性化列配置，恢复为默认配置。
     * </p>
     *
     * @param param 参数，包含 tableCode
     * @return 操作结果
     */
    @PostMapping("/user/columns/reset")
    public R<Void> resetUserColumns(@RequestBody TableConfigGetParam param) {
        if (param == null || !StringUtils.hasText(param.getTableCode())) {
            return R.fail(StatusCode.BUSINESS_ERROR, CommonPrompt.BAD_REQUEST, "tableCode 不能为空");
        }

        Long tenantId = TenantContext.get();
        Long userId = UserContext.get();

        if (userId == null) {
            return R.fail(StatusCode.UNAUTHORIZED, CommonPrompt.NOT_LOGIN);
        }

        // 删除用户配置
        Boolean deleted = userTableConfigService.deleteUserTableConfig(param.getTableCode(), tenantId, userId);

        if (!deleted) {
            // 用户没有配置，无需删除
            return R.ok();
        }

        return R.ok();
    }

    /**
     * 合并列配置
     * <p>
     * 将用户的列配置参数与原始列配置合并，生成用户个性化列配置。
     * </p>
     *
     * @param baseColumns 原始列配置列表
     * @param userColumns 用户列配置参数列表
     * @return 合并后的列配置列表
     */
    private List<FxTableColumnDTO> mergeColumnConfig(List<FxTableColumnDTO> baseColumns, List<UserColumnItemParam> userColumns) {
        // 构建用户配置的 field -> param 映射
        Map<String, UserColumnItemParam> userParamMap = userColumns.stream()
                .collect(Collectors.toMap(UserColumnItemParam::getField, p -> p, (a, b) -> a));

        // 合并配置
        List<FxTableColumnDTO> mergedColumns = new ArrayList<>();

        for (FxTableColumnDTO baseCol : baseColumns) {
            FxTableColumnDTO mergedCol = new FxTableColumnDTO();
            // 复制所有属性
            mergedCol.setField(baseCol.getField());
            mergedCol.setTitle(baseCol.getTitle());
            mergedCol.setAlign(baseCol.getAlign());
            mergedCol.setWidth(baseCol.getWidth());
            mergedCol.setFixed(baseCol.getFixed());
            mergedCol.setEllipsis(baseCol.getEllipsis());
            mergedCol.setSortable(baseCol.getSortable());
            mergedCol.setSorterField(baseCol.getSorterField());
            mergedCol.setQueryable(baseCol.getQueryable());
            mergedCol.setQueryType(baseCol.getQueryType());
            mergedCol.setQueryOperator(baseCol.getQueryOperator());
            mergedCol.setDictCode(baseCol.getDictCode());
            mergedCol.setDictField(baseCol.getDictField());
            mergedCol.setRenderType(baseCol.getRenderType());
            mergedCol.setPermKey(baseCol.getPermKey());

            // 应用用户配置
            UserColumnItemParam userParam = userParamMap.get(baseCol.getField());
            if (userParam != null) {
                // 设置显示状态和排序顺序
                mergedCol.setVisible(userParam.getVisible() != null ? userParam.getVisible() : true);
                mergedCol.setOrder(userParam.getOrder() != null ? userParam.getOrder() : Integer.MAX_VALUE);
                if (userParam.getWidth() != null && !ACTION_FIELD.equals(baseCol.getField())) {
                    mergedCol.setWidth(clampColumnWidth(userParam.getWidth()));
                }
            } else {
                // 用户没有配置该列，默认显示，排序靠后
                mergedCol.setVisible(true);
                mergedCol.setOrder(Integer.MAX_VALUE);
            }

            mergedColumns.add(mergedCol);
        }

        // 按照用户的 order 排序
        mergedColumns.sort((a, b) -> {
            int orderA = a.getOrder() != null ? a.getOrder() : Integer.MAX_VALUE;
            int orderB = b.getOrder() != null ? b.getOrder() : Integer.MAX_VALUE;
            return Integer.compare(orderA, orderB);
        });

        return mergedColumns;
    }

    private Integer clampColumnWidth(Integer width) {
        if (width == null) {
            return null;
        }
        return Math.max(MIN_COLUMN_WIDTH, Math.min(MAX_COLUMN_WIDTH, width));
    }

    private String currentUser() {
        Long userId = UserContext.get();
        return userId == null ? "system" : userId.toString();
    }

    private Long resolveConfigTenantId(Boolean publicConfig) {
        if (Boolean.TRUE.equals(publicConfig)) {
            return 0L;
        }
        return TenantContext.get();
    }

    private boolean isPublicTenant(Long tenantId) {
        return tenantId != null && tenantId == 0L;
    }
}
