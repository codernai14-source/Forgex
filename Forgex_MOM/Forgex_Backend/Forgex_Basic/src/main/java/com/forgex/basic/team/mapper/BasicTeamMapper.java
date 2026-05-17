package com.forgex.basic.team.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.basic.team.domain.entity.BasicTeam;
import org.apache.ibatis.annotations.Mapper;

/**
 * 班组主数据 Mapper。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@Mapper
public interface BasicTeamMapper extends BaseMapper<BasicTeam> {
}
