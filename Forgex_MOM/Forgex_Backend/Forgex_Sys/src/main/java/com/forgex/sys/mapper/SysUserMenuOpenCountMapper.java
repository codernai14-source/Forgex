/*
 * Copyright 2026 coder_nai@163.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.forgex.sys.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.forgex.sys.domain.entity.SysUserMenuOpenCount;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户菜单打开次数 Mapper。
 * <p>
 * 提供 sys_user_menu_open_count 表的数据访问能力，用于菜单首次打开引导判断。
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-29
 */
@Mapper
@DS("admin")
public interface SysUserMenuOpenCountMapper extends BaseMapper<SysUserMenuOpenCount> {
}
