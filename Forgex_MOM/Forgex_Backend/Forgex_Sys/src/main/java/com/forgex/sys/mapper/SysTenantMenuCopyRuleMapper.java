package com.forgex.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.forgex.sys.domain.entity.SysTenantMenuCopyRule;
import org.apache.ibatis.annotations.Mapper;

/**
 * 租户菜单复制规则表 Mapper
 * <p>
 * 对应管理库表 {@code sys_tenant_menu_copy_rule}，用于在租户初始化阶段
 * 读取不同租户类型需要排除的菜单权限前缀配置。
 * </p>
 *
 * @author coder_nai
 * @version 1.0
 * @see com.forgex.sys.domain.entity.SysTenantMenuCopyRule
 */
@Mapper
@DS("admin")
public interface SysTenantMenuCopyRuleMapper extends BaseMapper<SysTenantMenuCopyRule> {
}

