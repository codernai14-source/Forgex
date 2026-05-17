package com.forgex.basic.team.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.basic.team.domain.dto.TeamDTO;
import com.forgex.basic.team.domain.param.TeamPageParam;
import com.forgex.basic.team.service.ITeamService;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.web.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 班组主数据控制器。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@RestController
@RequestMapping("/team")
@RequiredArgsConstructor
public class TeamController {

    private final ITeamService teamService;

    @RequirePerm("basic:team:query")
    @PostMapping("/page")
    public R<Page<TeamDTO>> page(@RequestBody(required = false) TeamPageParam param) {
        return R.ok(teamService.page(param));
    }

    @PostMapping("/list")
    public R<List<TeamDTO>> list(@RequestBody(required = false) TeamPageParam param) {
        return R.ok(teamService.list(param));
    }

    @RequirePerm("basic:team:query")
    @PostMapping("/detail")
    public R<TeamDTO> detail(@RequestBody Map<String, Object> params) {
        return R.ok(teamService.detail(Long.valueOf(String.valueOf(params.get("id")))));
    }

    @RequirePerm("basic:team:add")
    @PostMapping("/create")
    public R<Long> create(@RequestBody TeamDTO param) {
        return R.ok(CommonPrompt.CREATE_SUCCESS, teamService.create(param));
    }

    @RequirePerm("basic:team:edit")
    @PostMapping("/update")
    public R<Boolean> update(@RequestBody TeamDTO param) {
        return R.ok(CommonPrompt.UPDATE_SUCCESS, teamService.update(param));
    }

    @RequirePerm("basic:team:delete")
    @PostMapping("/delete")
    public R<Boolean> delete(@RequestBody Map<String, Object> params) {
        return R.ok(CommonPrompt.DELETE_SUCCESS, teamService.delete(Long.valueOf(String.valueOf(params.get("id")))));
    }

    @RequirePerm("basic:team:delete")
    @PostMapping("/batchDelete")
    public R<Boolean> batchDelete(@RequestBody Map<String, List<Long>> params) {
        return R.ok(CommonPrompt.DELETE_SUCCESS, teamService.batchDelete(params.get("ids")));
    }
}
