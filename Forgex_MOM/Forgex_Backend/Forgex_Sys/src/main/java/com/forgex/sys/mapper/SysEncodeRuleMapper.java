/*Copyright 2026 coder_nai@163.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package com.forgex.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.forgex.sys.domain.entity.SysEncodeRule;
import org.apache.ibatis.annotations.Mapper;

/**
 * 编码规则 Mapper 接口
 * <p>
 * 提供编码规则的数据访问操作，继承 MyBatis-Plus 的 BaseMapper，
 * 支持基础的 CRUD 操作和自定义复杂查询。
 * </p>
 * <p>
 * 主要功能：
 * <ul>
 *   <li>基础的增删改查操作（继承自 BaseMapper）</li>
 *   <li>支持一对多关联查询（通过 XML 配置）</li>
 *   <li>支持按规则代码查询</li>
 *   <li>支持按模块分类查询</li>
 * </ul>
 * </p>
 * <p>
 * 数据源配置：使用 {@code @DS("admin")} 注解指定访问 common 库数据源。
 * </p>
 *
 * @author LiDaoMoM
 * @version 1.0
 * @since 2026-04-10
 * @see SysEncodeRule 编码规则实体
 * @see BaseMapper MyBatis-Plus 基础 Mapper
 * @see DS 多数据源注解
 */
@Mapper
@DS("common")
public interface SysEncodeRuleMapper extends BaseMapper<SysEncodeRule> {

    // 基础 CRUD 操作由 BaseMapper 提供，无需额外定义
    // 如需复杂查询，可在对应的 XML 文件中定义 SQL
}
