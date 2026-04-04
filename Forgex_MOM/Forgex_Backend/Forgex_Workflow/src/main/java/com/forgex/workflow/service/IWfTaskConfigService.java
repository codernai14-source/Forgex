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

public interface IWfTaskConfigService {

    Page<WfTaskConfigSummaryDTO> page(WfTaskConfigQueryParam param);

    List<WfTaskConfigDTO> list(WfTaskConfigQueryParam param);

    WfTaskConfigDTO getById(Long id);

    WfTaskConfigDTO getByCode(String taskCode);

    Long create(WfTaskConfigSaveParam param);

    Boolean update(WfTaskConfigSaveParam param);

    Boolean delete(Long id);

    Boolean updateStatus(Long id, Integer status);

    WfTaskDraftEditorDTO getOrCreateDraftEditor(WfTaskDraftEditorQueryParam param);

    WfTaskDraftEditorDTO saveDraftBaseInfo(WfTaskConfigSaveParam param);

    WfTaskGraphDTO getDraftGraph(WfTaskDraftEditorQueryParam param);

    Boolean saveDraftGraph(WfTaskGraphSaveParam param);

    Boolean publishDraft(WfTaskDraftEditorQueryParam param);
}
