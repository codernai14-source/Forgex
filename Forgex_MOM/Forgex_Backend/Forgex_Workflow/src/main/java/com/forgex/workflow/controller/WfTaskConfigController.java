package com.forgex.workflow.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.security.perm.PermKeyService;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.util.CurrentUserUtils;
import com.forgex.common.web.R;
import com.forgex.common.web.StatusCode;
import com.forgex.workflow.domain.dto.WfTaskConfigDTO;
import com.forgex.workflow.domain.dto.WfTaskConfigSummaryDTO;
import com.forgex.workflow.domain.dto.WfTaskDraftEditorDTO;
import com.forgex.workflow.domain.dto.WfTaskGraphDTO;
import com.forgex.workflow.domain.param.WfTaskConfigQueryParam;
import com.forgex.workflow.domain.param.WfTaskConfigSaveParam;
import com.forgex.workflow.domain.param.WfTaskDraftEditorQueryParam;
import com.forgex.workflow.domain.param.WfTaskGraphSaveParam;
import com.forgex.workflow.service.IWfTaskConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 审批任务配置控制器
 * <p>
 * 处理审批任务配置相关的 HTTP 请求，包括任务配置的增删改查、草稿编辑、流程图配置、发布等操作。
 * 提供审批任务的全生命周期管理功能，支持配置审批流程、节点设置、规则定义等。
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-15
 * @see IWfTaskConfigService 审批任务配置服务接口
 * @see WfTaskConfigDTO 任务配置数据传输对象
 * @see WfTaskConfigSaveParam 任务配置保存参数
 * @see WfTaskDraftEditorDTO 任务草稿编辑器数据传输对象
 * @see WfTaskGraphDTO 任务流程图数据传输对象
 */
@Slf4j
@RestController
@RequestMapping("/wf/task/config")
@RequiredArgsConstructor
public class WfTaskConfigController {

    private final IWfTaskConfigService taskConfigService;
    private final PermKeyService permKeyService;

    /**
     * 分页查询审批任务配置
     * <p>
     * 根据查询条件分页获取审批任务配置列表，返回配置摘要信息
     * </p>
     *
     * @param param 查询参数，包含页码、页数、筛选条件等
     * @return 分页结果，包含任务配置摘要列表
     * @throws I18nBusinessException 当查询失败时抛出业务异常
     * @see IWfTaskConfigService#page(WfTaskConfigQueryParam) 分页查询服务方法
     * @see WfTaskConfigSummaryDTO 任务配置摘要数据传输对象
     */
    @PostMapping("/page")
    @RequirePerm("wf:taskConfig:view")
    public R<Page<WfTaskConfigSummaryDTO>> page(@RequestBody WfTaskConfigQueryParam param) {
        return R.ok(taskConfigService.page(param));
    }

    /**
     * 查询审批任务配置列表
     * <p>
     * 根据查询条件获取审批任务配置列表，用于发起审批时选择任务类型
     * </p>
     *
     * @param param 查询参数，包含筛选条件、排序规则等
     * @return 任务配置列表
     * @throws I18nBusinessException 当查询失败时抛出业务异常
     * @see IWfTaskConfigService#list(WfTaskConfigQueryParam) 列表查询服务方法
     * @see WfTaskConfigDTO 任务配置数据传输对象
     */
    @PostMapping("/list")
    @RequirePerm("wf:execution:start")
    public R<List<WfTaskConfigDTO>> list(@RequestBody WfTaskConfigQueryParam param) {
        return R.ok(taskConfigService.list(param));
    }

    /**
     * 根据 ID 查询审批任务配置详情
     * <p>
     * 通过任务配置 ID 获取详细的配置信息，包含完整的配置数据
     * </p>
     *
     * @param params 请求参数，包含 id（任务配置 ID，必填）
     * @return 任务配置详情，如果不存在返回 NOT_FOUND
     * @throws I18nBusinessException 当参数无效或查询失败时抛出业务异常
     * @see IWfTaskConfigService#getById(Long) 根据 ID 查询服务方法
     * @see WfTaskConfigDTO 任务配置数据传输对象
     */
    @PostMapping("/get")
    @RequirePerm("wf:taskConfig:view")
    public R<WfTaskConfigDTO> get(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        WfTaskConfigDTO config = taskConfigService.getById(id);
        if (config == null) {
            return R.fail(CommonPrompt.NOT_FOUND);
        }
        return R.ok(config);
    }

    /**
     * 根据任务编码查询审批任务配置
     * <p>
     * 通过任务编码（唯一标识）获取任务配置信息，用于发起审批时快速定位配置
     * </p>
     *
     * @param params 请求参数，包含 taskCode（任务编码，必填）
     * @return 任务配置详情，如果不存在返回 NOT_FOUND
     * @throws I18nBusinessException 当参数无效或查询失败时抛出业务异常
     * @see IWfTaskConfigService#getByCode(String) 根据编码查询服务方法
     * @see WfTaskConfigDTO 任务配置数据传输对象
     */
    @PostMapping("/getByCode")
    @RequirePerm("wf:execution:start")
    public R<WfTaskConfigDTO> getByCode(@RequestBody Map<String, Object> params) {
        String taskCode = String.valueOf(params.get("taskCode"));
        WfTaskConfigDTO config = taskConfigService.getByCode(taskCode);
        if (config == null) {
            return R.fail(CommonPrompt.NOT_FOUND);
        }
        return R.ok(config);
    }

    /**
     * 创建审批任务配置
     * <p>
     * 新建一个审批任务配置，包含基础信息、流程节点、规则等
     * </p>
     *
     * @param param 任务配置保存参数，包含配置名称、编码、节点信息等（必填）
     * @return 新创建的任务配置 ID
     * @throws I18nBusinessException 当参数校验失败或创建失败时抛出业务异常
     * @see IWfTaskConfigService#create(WfTaskConfigSaveParam) 创建服务方法
     * @see WfTaskConfigSaveParam 任务配置保存参数
     */
    @PostMapping("/create")
    @RequirePerm("wf:taskConfig:add")
    public R<Long> create(@Validated @RequestBody WfTaskConfigSaveParam param) {
        return R.ok(CommonPrompt.CREATE_SUCCESS, taskConfigService.create(param));
    }

    /**
     * 更新审批任务配置
     * <p>
     * 更新已存在的审批任务配置信息，包括基础信息、流程节点、规则等
     * </p>
     *
     * @param param 任务配置保存参数，包含 ID 和更新后的配置信息（必填）
     * @return 是否更新成功
     * @throws I18nBusinessException 当参数校验失败或更新失败时抛出业务异常
     * @see IWfTaskConfigService#update(WfTaskConfigSaveParam) 更新服务方法
     * @see WfTaskConfigSaveParam 任务配置保存参数
     */
    @PostMapping("/update")
    @RequirePerm("wf:taskConfig:edit")
    public R<Boolean> update(@Validated @RequestBody WfTaskConfigSaveParam param) {
        return R.ok(CommonPrompt.UPDATE_SUCCESS, taskConfigService.update(param));
    }

    /**
     * 删除审批任务配置
     * <p>
     * 根据 ID 删除审批任务配置，执行逻辑删除
     * </p>
     *
     * @param params 请求参数，包含 id（任务配置 ID，必填）
     * @return 是否删除成功
     * @throws I18nBusinessException 当参数无效或删除失败时抛出业务异常
     * @see IWfTaskConfigService#delete(Long) 删除服务方法
     */
    @PostMapping("/delete")
    @RequirePerm("wf:taskConfig:delete")
    public R<Boolean> delete(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        return R.ok(CommonPrompt.DELETE_SUCCESS, taskConfigService.delete(id));
    }

    /**
     * 更新审批任务配置状态
     * <p>
     * 修改任务配置的启用/禁用状态，控制配置是否可用
     * </p>
     *
     * @param params 请求参数，包含 id（任务配置 ID，必填）、status（状态值，必填）
     * @return 是否更新成功
     * @throws I18nBusinessException 当参数无效或更新失败时抛出业务异常
     * @see IWfTaskConfigService#updateStatus(Long, Integer) 更新状态服务方法
     */
    @PostMapping("/updateStatus")
    @RequirePerm("wf:taskConfig:edit")
    public R<Boolean> updateStatus(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Integer status = Integer.valueOf(params.get("status").toString());
        return R.ok(CommonPrompt.UPDATE_SUCCESS, taskConfigService.updateStatus(id, status));
    }

    /**
     * 获取或创建草稿编辑器
     * <p>
     * 获取任务配置的草稿编辑器数据，如果不存在则创建新的草稿
     * </p>
     *
     * @param param 草稿编辑器查询参数，包含任务配置 ID（必填）
     * @return 草稿编辑器数据，包含可编辑的配置信息
     * @throws I18nBusinessException 当参数无效或操作失败时抛出业务异常
     * @see IWfTaskConfigService#getOrCreateDraftEditor(WfTaskDraftEditorQueryParam) 获取或创建草稿服务方法
     * @see WfTaskDraftEditorDTO 草稿编辑器数据传输对象
     */
    @PostMapping("/draft/editor")
    public R<WfTaskDraftEditorDTO> getOrCreateDraftEditor(@RequestBody WfTaskDraftEditorQueryParam param) {
        requireAnyTaskConfigPerm("wf:taskConfig:add", "wf:taskConfig:edit", "wf:taskConfig:config");
        return R.ok(taskConfigService.getOrCreateDraftEditor(param));
    }

    /**
     * 保存草稿基础信息
     * <p>
     * 保存任务配置草稿的基础信息，包括名称、编码、描述等
     * </p>
     *
     * @param param 任务配置保存参数，包含草稿 ID 和基础信息（必填）
     * @return 保存后的草稿编辑器数据
     * @throws I18nBusinessException 当参数校验失败或保存失败时抛出业务异常
     * @see IWfTaskConfigService#saveDraftBaseInfo(WfTaskConfigSaveParam) 保存草稿基础信息服务方法
     * @see WfTaskDraftEditorDTO 草稿编辑器数据传输对象
     */
    @PostMapping("/draft/base/save")
    public R<WfTaskDraftEditorDTO> saveDraftBaseInfo(@Validated @RequestBody WfTaskConfigSaveParam param) {
        requireDraftBasePerm(param);
        return R.ok(CommonPrompt.UPDATE_SUCCESS, taskConfigService.saveDraftBaseInfo(param));
    }

    /**
     * 获取草稿流程图
     * <p>
     * 获取任务配置草稿的流程图数据，包含节点、连线、规则等信息
     * </p>
     *
     * @param param 草稿编辑器查询参数，包含任务配置 ID（必填）
     * @return 草稿流程图数据
     * @throws I18nBusinessException 当参数无效或查询失败时抛出业务异常
     * @see IWfTaskConfigService#getDraftGraph(WfTaskDraftEditorQueryParam) 获取草稿流程图服务方法
     * @see WfTaskGraphDTO 任务流程图数据传输对象
     */
    @PostMapping("/draft/graph/get")
    @RequirePerm("wf:taskConfig:config")
    public R<WfTaskGraphDTO> getDraftGraph(@RequestBody WfTaskDraftEditorQueryParam param) {
        return R.ok(taskConfigService.getDraftGraph(param));
    }

    /**
     * 保存草稿流程图
     * <p>
     * 保存任务配置草稿的流程图数据，包括节点配置、连线关系、规则设置等
     * </p>
     *
     * @param param 流程图保存参数，包含节点、连线、规则等信息（必填）
     * @return 是否保存成功
     * @throws I18nBusinessException 当参数校验失败或保存失败时抛出业务异常
     * @see IWfTaskConfigService#saveDraftGraph(WfTaskGraphSaveParam) 保存草稿流程图服务方法
     * @see WfTaskGraphSaveParam 流程图保存参数
     */
    @PostMapping("/draft/graph/save")
    @RequirePerm("wf:taskConfig:config")
    public R<Boolean> saveDraftGraph(@Validated @RequestBody WfTaskGraphSaveParam param) {
        return R.ok(CommonPrompt.UPDATE_SUCCESS, taskConfigService.saveDraftGraph(param));
    }

    /**
     * 发布草稿
     * <p>
     * 将草稿状态的审批任务配置发布为正式版本，使其可用于发起审批
     * </p>
     *
     * @param param 草稿编辑器查询参数，包含任务配置 ID（必填）
     * @return 是否发布成功
     * @throws I18nBusinessException 当参数无效或发布失败时抛出业务异常
     * @see IWfTaskConfigService#publishDraft(WfTaskDraftEditorQueryParam) 发布草稿服务方法
     */
    @PostMapping("/draft/publish")
    @RequirePerm("wf:taskConfig:config")
    public R<Boolean> publishDraft(@RequestBody WfTaskDraftEditorQueryParam param) {
        return R.ok(CommonPrompt.UPDATE_SUCCESS, taskConfigService.publishDraft(param));
    }

    private void requireDraftBasePerm(WfTaskConfigSaveParam param) {
        String permKey = param != null && param.getId() == null ? "wf:taskConfig:add" : "wf:taskConfig:edit";
        requireAnyTaskConfigPerm(permKey);
    }

    private void requireAnyTaskConfigPerm(String... permKeys) {
        Long userId = CurrentUserUtils.getUserId();
        Long tenantId = CurrentUserUtils.getTenantId();
        if (userId == null || tenantId == null) {
            throw new I18nBusinessException(StatusCode.NOT_LOGIN, CommonPrompt.NOT_LOGIN);
        }
        for (String permKey : permKeys) {
            if (permKeyService.hasAllPerms(userId, tenantId, Set.of(permKey))) {
                return;
            }
        }
        throw new I18nBusinessException(StatusCode.UNAUTHORIZED, CommonPrompt.NO_PERMISSION);
    }
}
