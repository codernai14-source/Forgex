package com.forgex.workflow.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.web.R;
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

@Slf4j
@RestController
@RequestMapping("/wf/task/config")
@RequiredArgsConstructor
public class WfTaskConfigController {

    private final IWfTaskConfigService taskConfigService;

    @PostMapping("/page")
    @RequirePerm("wf:taskConfig:view")
    public R<Page<WfTaskConfigSummaryDTO>> page(@RequestBody WfTaskConfigQueryParam param) {
        return R.ok(taskConfigService.page(param));
    }

    @PostMapping("/list")
    @RequirePerm("wf:execution:start")
    public R<List<WfTaskConfigDTO>> list(@RequestBody WfTaskConfigQueryParam param) {
        return R.ok(taskConfigService.list(param));
    }

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

    @PostMapping("/create")
    @RequirePerm("wf:taskConfig:add")
    public R<Long> create(@Validated @RequestBody WfTaskConfigSaveParam param) {
        return R.ok(CommonPrompt.CREATE_SUCCESS, taskConfigService.create(param));
    }

    @PostMapping("/update")
    @RequirePerm("wf:taskConfig:edit")
    public R<Boolean> update(@Validated @RequestBody WfTaskConfigSaveParam param) {
        return R.ok(CommonPrompt.UPDATE_SUCCESS, taskConfigService.update(param));
    }

    @PostMapping("/delete")
    @RequirePerm("wf:taskConfig:delete")
    public R<Boolean> delete(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        return R.ok(CommonPrompt.DELETE_SUCCESS, taskConfigService.delete(id));
    }

    @PostMapping("/updateStatus")
    @RequirePerm({"wf:taskConfig:add", "wf:taskConfig:edit"})
    public R<Boolean> updateStatus(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Integer status = Integer.valueOf(params.get("status").toString());
        return R.ok(CommonPrompt.UPDATE_SUCCESS, taskConfigService.updateStatus(id, status));
    }

    @PostMapping("/draft/editor")
    @RequirePerm({"wf:taskConfig:add", "wf:taskConfig:edit", "wf:taskConfig:config"})
    public R<WfTaskDraftEditorDTO> getOrCreateDraftEditor(@RequestBody WfTaskDraftEditorQueryParam param) {
        return R.ok(taskConfigService.getOrCreateDraftEditor(param));
    }

    @PostMapping("/draft/base/save")
    @RequirePerm({"wf:taskConfig:add", "wf:taskConfig:edit"})
    public R<WfTaskDraftEditorDTO> saveDraftBaseInfo(@Validated @RequestBody WfTaskConfigSaveParam param) {
        return R.ok(CommonPrompt.UPDATE_SUCCESS, taskConfigService.saveDraftBaseInfo(param));
    }

    @PostMapping("/draft/graph/get")
    @RequirePerm({"wf:taskConfig:edit", "wf:taskConfig:config"})
    public R<WfTaskGraphDTO> getDraftGraph(@RequestBody WfTaskDraftEditorQueryParam param) {
        return R.ok(taskConfigService.getDraftGraph(param));
    }

    @PostMapping("/draft/graph/save")
    @RequirePerm({"wf:taskConfig:edit", "wf:taskConfig:config"})
    public R<Boolean> saveDraftGraph(@Validated @RequestBody WfTaskGraphSaveParam param) {
        return R.ok(CommonPrompt.UPDATE_SUCCESS, taskConfigService.saveDraftGraph(param));
    }

    @PostMapping("/draft/publish")
    @RequirePerm({"wf:taskConfig:edit", "wf:taskConfig:config"})
    public R<Boolean> publishDraft(@RequestBody WfTaskDraftEditorQueryParam param) {
        return R.ok(CommonPrompt.UPDATE_SUCCESS, taskConfigService.publishDraft(param));
    }
}
