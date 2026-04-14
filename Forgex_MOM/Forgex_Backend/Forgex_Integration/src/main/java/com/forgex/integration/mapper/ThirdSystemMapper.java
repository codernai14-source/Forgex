package com.forgex.integration.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.github.yulichang.base.MPJBaseMapper;
import com.forgex.integration.domain.entity.ThirdSystem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 第三方系统 Mapper 接口
 * <p>
 * 负责第三方系统信息的数据访问，支持多租户和逻辑删除
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Mapper
public interface ThirdSystemMapper extends MPJBaseMapper<ThirdSystem> {

    /**
     * 根据系统编码查询第三方系统
     * <p>
     * 用于校验系统编码唯一性
     * </p>
     *
     * @param systemCode 系统编码
     * @param tenantId 租户 ID
     * @return 第三方系统信息，不存在返回 null
     */
    @InterceptorIgnore(tenantLine = "true")
    ThirdSystem selectBySystemCode(@Param("systemCode") String systemCode, @Param("tenantId") Long tenantId);

    /**
     * 查询所有启用的第三方系统
     * <p>
     * 用于授权时选择第三方系统
     * </p>
     *
     * @return 启用的第三方系统列表
     */
    List<ThirdSystem> selectEnabledSystems();
}
