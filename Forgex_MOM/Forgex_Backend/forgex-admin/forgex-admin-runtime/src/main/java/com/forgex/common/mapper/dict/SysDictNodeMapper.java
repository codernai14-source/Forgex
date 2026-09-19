package com.forgex.common.mapper.dict;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.common.domain.entity.dict.SysDictNode;
import org.apache.ibatis.annotations.Mapper;

/**
 * 字典节点Mapper接口
 * <p>
 * 提供字典节点数据的数据库访问操作。
 * </p>
 * <p><strong>主要功能：</strong></p>
 * <ul>
 *   <li>继承MyBatis-Plus的BaseMapper，提供基础CRUD操作</li>
 *   <li>使用@DS注解指定数据源为"admin"</li>
 *   <li>使用@Mapper注解标识为MyBatis Mapper接口</li>
 * </ul>
 * <p><strong>数据源说明：</strong></p>
 * <ul>
 *   <li>使用动态数据源"admin"，指向forgex_admin数据库</li>
 *   <li>支持多租户架构，通过tenant_id字段隔离数据</li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see SysDictNode
 * @see com.baomidou.mybatisplus.core.mapper.BaseMapper
 */
@Mapper
@DS("admin")
public interface SysDictNodeMapper extends BaseMapper<SysDictNode> {
}

