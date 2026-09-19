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
import com.forgex.sys.domain.entity.SysRolePosition;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色职位关联 Mapper 接口
 * <p>
 * 负责角色与职位关联关系的数据访问操作。
 * 继承 MyBatis-Plus 的 BaseMapper，提供基础 CRUD 功能。
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-06
 * @see SysRolePosition
 * @see BaseMapper
 */
@Mapper
@DS("admin")
public interface SysRolePositionMapper extends BaseMapper<SysRolePosition> {
}
