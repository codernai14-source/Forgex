package com.forgex.basic.team.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.basic.team.domain.dto.TeamDTO;
import com.forgex.basic.team.domain.entity.BasicTeam;
import com.forgex.basic.team.domain.param.TeamPageParam;

import java.util.List;

/**
 * 班组主数据服务。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
public interface ITeamService extends IService<BasicTeam> {

    Page<TeamDTO> page(TeamPageParam param);

    List<TeamDTO> list(TeamPageParam param);

    TeamDTO detail(Long id);

    Long create(TeamDTO param);

    Boolean update(TeamDTO param);

    Boolean delete(Long id);

    Boolean batchDelete(List<Long> ids);
}
