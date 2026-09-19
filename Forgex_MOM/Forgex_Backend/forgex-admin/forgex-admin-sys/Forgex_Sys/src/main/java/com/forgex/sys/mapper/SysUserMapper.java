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

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.forgex.common.dataperm.DataPermission;
import com.forgex.common.security.SecurityLabeled;
import com.forgex.sys.domain.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 管理库用户表 Mapper。
 * <p>
 * 重写 {@link #selectList(Wrapper)} 以便挂载数据权限与安全标记。
 * 必须保留 {@code @Param(Constants.WRAPPER)}，否则 MyBatis-Plus 注入 SQL 中的 {@code ew}
 * 无法从 {@code LambdaQueryWrapper} 取值，{@code selectOne} 会在选租户拉路由时失败。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see DataPermission
 * @see SecurityLabeled
 */
@Mapper
@DS("admin")
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 按条件查询用户列表，并叠加数据权限、安全标记过滤。
     * <p>
     * {@link com.baomidou.mybatisplus.core.mapper.BaseMapper#selectOne(Wrapper)} 会复用本方法，
     * 因此参数名必须仍是 {@code ew}。查当前登录用户自身请用 {@code selectById}，不要走本方法。
     * </p>
     *
     * @param queryWrapper 查询条件，对应 MyBatis-Plus 的 {@code ew}
     * @return 用户列表，无匹配时为空列表
     */
    @Override
    @DataPermission(userColumn = "id", deptColumn = "department_id")
    @SecurityLabeled
    List<SysUser> selectList(@Param(Constants.WRAPPER) Wrapper<SysUser> queryWrapper);
}
