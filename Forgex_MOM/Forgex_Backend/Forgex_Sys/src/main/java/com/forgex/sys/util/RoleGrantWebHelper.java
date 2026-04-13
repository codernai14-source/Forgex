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
package com.forgex.sys.util;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.util.CurrentUserUtils;
import com.forgex.sys.domain.dto.RoleGrantDTO;
import com.forgex.sys.domain.param.RoleGrantQueryDTO;
import com.forgex.sys.domain.param.RoleUserIdsParam;
import com.forgex.sys.domain.param.RoleUserListParam;

/**
 * 角色授权相关 Web 层辅助工具。
 * <p>
 * 统一解析租户 ID、分页结果转换，避免各 Controller 重复实现相同逻辑。
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-06
 * @see RoleGrantQueryDTO
 * @see RoleGrantDTO
 */
public final class RoleGrantWebHelper {

    private RoleGrantWebHelper() {
    }

    /**
     * 填充租户 ID：优先线程上下文 {@link TenantContext}，其次请求体已传值，再次当前登录用户所属租户。
     *
     * @param dto 查询参数，可为 null（方法直接返回）
     */
    public static void fillTenantId(RoleGrantQueryDTO dto) {
        if (dto == null) {
            return;
        }
        Long tid = TenantContext.get();
        if (tid == null) {
            tid = dto.getTenantId();
        }
        if (tid == null) {
            tid = CurrentUserUtils.getTenantId();
        }
        dto.setTenantId(tid);
    }

    /**
     * 填充租户 ID：优先线程上下文，其次请求体，再次当前登录用户。
     *
     * @param dto 授权 DTO，可为 null
     */
    public static void fillTenantId(RoleGrantDTO dto) {
        if (dto == null) {
            return;
        }
        Long tid = TenantContext.get();
        if (tid == null) {
            tid = dto.getTenantId();
        }
        if (tid == null) {
            tid = CurrentUserUtils.getTenantId();
        }
        dto.setTenantId(tid);
    }

    /**
     * 填充租户 ID：用于角色-用户列表/批量绑定场景。
     *
     * @param param 请求参数，可为 null
     */
    public static void fillTenantId(RoleUserListParam param) {
        if (param == null) {
            return;
        }
        Long tid = TenantContext.get();
        if (tid == null) {
            tid = param.getTenantId();
        }
        if (tid == null) {
            tid = CurrentUserUtils.getTenantId();
        }
        param.setTenantId(tid);
    }

    /**
     * 填充租户 ID：用于角色-用户授权/取消授权。
     *
     * @param param 请求参数，可为 null
     */
    public static void fillTenantId(RoleUserIdsParam param) {
        if (param == null) {
            return;
        }
        Long tid = TenantContext.get();
        if (tid == null) {
            tid = param.getTenantId();
        }
        if (tid == null) {
            tid = CurrentUserUtils.getTenantId();
        }
        param.setTenantId(tid);
    }

    /**
     * 规范化角色授权分页查询的页码与每页条数，避免空指针或非正数。
     *
     * @param query 查询参数，可为 null
     */
    public static void normalizePage(RoleGrantQueryDTO query) {
        if (query == null) {
            return;
        }
        if (query.getPageNum() == null || query.getPageNum() < 1) {
            query.setPageNum(1);
        }
        if (query.getPageSize() == null || query.getPageSize() < 1) {
            query.setPageSize(20);
        }
    }

    /**
     * 将 {@link IPage} 转为 {@link Page}，便于 Controller 统一声明返回类型并与前端约定字段一致。
     *
     * @param source MyBatis-Plus 分页结果，可为 null
     * @param <T>    记录类型
     * @return 非 null 的 {@link Page}；{@code source == null} 时返回空页
     */
    public static <T> Page<T> toPage(IPage<T> source) {
        if (source == null) {
            return new Page<>();
        }
        if (source instanceof Page) {
            @SuppressWarnings("unchecked")
            Page<T> page = (Page<T>) source;
            return page;
        }
        Page<T> page = new Page<>(source.getCurrent(), source.getSize(), source.getTotal());
        page.setRecords(source.getRecords());
        return page;
    }
}
