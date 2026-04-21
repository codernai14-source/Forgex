package com.forgex.workflow.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.workflow.domain.dto.WfTaskConfigDTO;
import com.forgex.workflow.domain.dto.WfTaskConfigSummaryDTO;
import com.forgex.workflow.domain.dto.WfTaskDraftEditorDTO;
import com.forgex.workflow.domain.dto.WfTaskGraphDTO;
import com.forgex.workflow.domain.param.WfTaskConfigQueryParam;
import com.forgex.workflow.domain.param.WfTaskConfigSaveParam;
import com.forgex.workflow.domain.param.WfTaskDraftEditorQueryParam;
import com.forgex.workflow.domain.param.WfTaskGraphSaveParam;

import java.util.List;

/**
 * 审批任务配置服务接口。
 * <p>
 * 提供审批任务配置的查询、创建、更新、删除等功能，支持草稿编辑和发布流程管理。
 * </p>
 * <p>
 * 主要职责：
 * </p>
 * <ul>
 *   <li>任务配置的分页查询和列表查询</li>
 *   <li>任务配置的创建、更新、删除操作</li>
 *   <li>草稿编辑器管理（基础信息和流程图）</li>
 *   <li>任务配置的发布和状态管理</li>
 * </ul>
 *
 * @author ForGexTeam
 * @version 1.0.0
 * @since 2026-04-01
 * @see WfTaskConfigDTO
 * @see WfTaskConfigSummaryDTO
 * @see WfTaskDraftEditorDTO
 * @see WfTaskGraphDTO
 */
public interface IWfTaskConfigService {

    /**
     * 分页查询任务配置列表。
     * <p>
     * 根据查询条件分页返回任务配置摘要信息，仅包含草稿和已发布状态的配置。
     * </p>
     *
     * @param param 查询参数，包含任务名称、任务编码、状态等过滤条件
     * @return 任务配置摘要分页结果
     * @throws BusinessException 当查询参数不合法时抛出业务异常
     * @see WfTaskConfigSummaryDTO
     * @see WfTaskConfigQueryParam
     */
    Page<WfTaskConfigSummaryDTO> page(WfTaskConfigQueryParam param);

    /**
     * 查询任务配置列表。
     * <p>
     * 根据查询条件返回已发布的任务配置完整信息列表。
     * </p>
     *
     * @param param 查询参数，包含任务名称、任务编码、状态等过滤条件
     * @return 任务配置 DTO 列表
     * @throws BusinessException 当查询参数不合法时抛出业务异常
     * @see WfTaskConfigDTO
     * @see WfTaskConfigQueryParam
     */
    List<WfTaskConfigDTO> list(WfTaskConfigQueryParam param);

    /**
     * 根据 ID 查询任务配置。
     * <p>
     * 通过任务配置 ID 查询完整的配置信息。
     * </p>
     *
     * @param id 任务配置 ID
     * @return 任务配置 DTO
     * @throws BusinessException 当任务配置不存在时抛出业务异常
     * @see WfTaskConfigDTO
     */
    WfTaskConfigDTO getById(Long id);

    /**
     * 根据任务编码查询已发布的任务配置。
     * <p>
     * 通过任务编码查询最新已发布版本的配置信息。
     * </p>
     *
     * @param taskCode 任务编码
     * @return 任务配置 DTO，如果不存在返回 null
     * @throws BusinessException 当查询参数不合法时抛出业务异常
     * @see WfTaskConfigDTO
     */
    WfTaskConfigDTO getByCode(String taskCode);

    /**
     * 创建任务配置。
     * <p>
     * 创建新的任务配置草稿，返回草稿 ID。
     * </p>
     *
     * @param param 任务配置保存参数
     * @return 草稿 ID
     * @throws BusinessException 当保存参数不合法时抛出业务异常
     * @see WfTaskConfigSaveParam
     */
    Long create(WfTaskConfigSaveParam param);

    /**
     * 更新任务配置。
     * <p>
     * 更新任务配置草稿的基础信息。
     * </p>
     *
     * @param param 任务配置保存参数
     * @return 更新结果，成功返回 true
     * @throws BusinessException 当任务配置不存在或参数不合法时抛出业务异常
     * @see WfTaskConfigSaveParam
     */
    Boolean update(WfTaskConfigSaveParam param);

    /**
     * 删除任务配置。
     * <p>
     * 删除指定任务配置的草稿版本，并将已发布版本标记为归档。
     * </p>
     *
     * @param id 任务配置 ID
     * @return 删除结果，成功返回 true
     * @throws BusinessException 当任务配置不存在时无抛出业务异常
     */
    Boolean delete(Long id);

    /**
     * 更新任务配置状态。
     * <p>
     * 更新草稿版本的任务配置状态（启用/禁用）。
     * </p>
     *
     * @param id 任务配置 ID
     * @param status 新状态：0-禁用，1-启用
     * @return 更新结果，成功返回 true
     * @throws BusinessException 当任务配置不存在或不是草稿状态时抛出业务异常
     */
    Boolean updateStatus(Long id, Integer status);

    /**
     * 获取或创建草稿编辑器。
     * <p>
     * 根据任务编码获取草稿编辑器的完整信息，如果不存在则创建新的草稿。
     * </p>
     *
     * @param param 草稿编辑器查询参数
     * @return 草稿编辑器 DTO
     * @throws BusinessException 当查询参数不合法时抛出业务异常
     * @see WfTaskDraftEditorDTO
     * @see WfTaskDraftEditorQueryParam
     */
    WfTaskDraftEditorDTO getOrCreateDraftEditor(WfTaskDraftEditorQueryParam param);

    /**
     * 保存草稿基础信息。
     * <p>
     * 保存任务配置草稿的基础信息（名称、编码、表单类型等）。
     * </p>
     *
     * @param param 任务配置保存参数
     * @return 草稿编辑器 DTO，包含草稿 ID 等完整信息
     * @throws BusinessException 当保存参数不合法时抛出业务异常
     * @see WfTaskDraftEditorDTO
     * @see WfTaskConfigSaveParam
     */
    WfTaskDraftEditorDTO saveDraftBaseInfo(WfTaskConfigSaveParam param);

    /**
     * 获取草稿流程图。
     * <p>
     * 获取任务配置草稿的流程图配置（节点和边的完整信息）。
     * </p>
     *
     * @param param 草稿编辑器查询参数
     * @return 流程图 DTO
     * @throws BusinessException 当草稿不存在时抛出业务异常
     * @see WfTaskGraphDTO
     * @see WfTaskDraftEditorQueryParam
     */
    WfTaskGraphDTO getDraftGraph(WfTaskDraftEditorQueryParam param);

    /**
     * 保存草稿流程图。
     * <p>
     * 保存任务配置草稿的流程图配置，包括节点和边的增删改操作。
     * </p>
     *
     * @param param 流程图保存参数，包含节点和边的完整配置
     * @return 保存结果，成功返回 true
     * @throws BusinessException 当草稿不存在或参数不合法时抛出业务异常
     * @see WfTaskGraphSaveParam
     */
    Boolean saveDraftGraph(WfTaskGraphSaveParam param);

    /**
     * 发布草稿。
     * <p>
     * 将草稿版本的任務配置发布为正式版本，同时归档旧的已发布版本。
     * </p>
     *
     * @param param 草稿编辑器查询参数
     * @return 发布结果，成功返回 true
     * @throws BusinessException 当草稿不存在或发布失败时抛出业务异常
     * @see WfTaskDraftEditorQueryParam
     */
    Boolean publishDraft(WfTaskDraftEditorQueryParam param);
}
