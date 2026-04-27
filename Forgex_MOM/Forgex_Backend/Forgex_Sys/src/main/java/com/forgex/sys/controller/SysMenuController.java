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
package com.forgex.sys.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.tenant.UserContext;
import com.forgex.common.web.R;
import com.forgex.sys.domain.dto.SysMenuDTO;
import com.forgex.sys.domain.dto.SysMenuQueryDTO;
import com.forgex.sys.domain.entity.SysMenu;
import com.forgex.sys.domain.param.UserMenuPathListParam;
import com.forgex.sys.domain.param.UserMenuPathParam;
import com.forgex.sys.domain.param.UserMenuQueryParam;
import com.forgex.sys.domain.vo.MenuTreeVO;
import com.forgex.sys.domain.vo.UserMenuPreferenceVO;
import com.forgex.sys.domain.vo.UserRoutesVO;
import com.forgex.sys.service.ISysMenuService;
import com.forgex.sys.validator.MenuValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 菜单管理Controller
 *
 * 职责：
 * - 接收HTTP请求
 * - 参数校验（调用Validator）
 * - 调用Service层方法
 * - 返回响应结果
 *
 * 接口规范：
 * - 所有接口统一使用 POST 方法
 * - 参数统一封装为对象
 * - 分页查询使用 BaseGetParam（pageNum/pageSize）
 * 
 * @author coder_nai@163.com
 * @version 1.1
 * @since 2026-04-12
 */
@RestController
@RequestMapping("/sys/menu")
@RequiredArgsConstructor
public class SysMenuController {
    
    private final ISysMenuService menuService;
    private final MenuValidator menuValidator;
    
    /**
     * 获取用户路由（包含模块、菜单、按钮权限）。
     * <p>
     * 接口信息：
     * </p>
     * <ul>
     *   <li>接口路径：POST /sys/menu/routes</li>
     *   <li>认证要求：是，需要登录状态</li>
     *   <li>权限要求：无特殊权限要求</li>
     * </ul>
     * <p>
     * 执行步骤：
     * </p>
     * <ol>
     *   <li>参数解析：从请求体中提取 account 参数</li>
     *   <li>获取租户 ID：从 TenantContext 中获取当前租户 ID</li>
     *   <li>参数校验：调用 menuValidator.validateRoutesParams() 校验账号和租户 ID</li>
     *   <li>查询路由：调用 menuService.getUserRoutes() 获取用户路由信息</li>
     *   <li>返回结果：将路由信息封装到 R 对象中返回</li>
     * </ol>
     * <p>
     * 参数说明：
     * </p>
     * <ul>
     *   <li>body.account：用户账号，String 类型，必填</li>
     * </ul>
     * <p>
     * 返回值说明：
     * </p>
     * <ul>
     *   <li>code：200 表示成功</li>
     *   <li>data：UserRoutesVO 对象，包含：
     *     <ul>
     *       <li>modules：模块列表</li>
     *       <li>routes：路由树</li>
     *       <li>buttons：按钮权限列表</li>
     *     </ul>
     *   </li>
     *   <li>message：成功或失败提示</li>
     * </ul>
     *
     * @param body 请求体，包含 account 参数
     * @return {@link R} 包含用户路由信息的统一返回结构
     * @throws com.forgex.common.exception.I18nBusinessException 当参数校验失败时抛出
     * @see com.forgex.sys.domain.vo.UserRoutesVO
     * @see com.forgex.sys.service.ISysMenuService#getUserRoutes(String, Long)
     * @see com.forgex.sys.validator.MenuValidator#validateRoutesParams(String, Long)
     */
    @PostMapping("/routes")
    public R<UserRoutesVO> routes(@RequestBody Map<String, Object> body) {
        // 1. 参数解析
        String account = (String) body.get("account");
        Long tenantId = TenantContext.get();
        if (tenantId == null) {
            Object tenantIdRaw = body.get("tenantId");
            if (tenantIdRaw instanceof Number numberValue) {
                tenantId = numberValue.longValue();
            } else if (tenantIdRaw instanceof String tenantIdStr) {
                try {
                    tenantId = Long.valueOf(tenantIdStr);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        
        // 2. 参数校验
        menuValidator.validateRoutesParams(account, tenantId);
        
        // 3. 调用Service
        UserRoutesVO routes = menuService.getUserRoutes(account, tenantId);
        
        // 4. 返回结果
        return R.ok(routes);
    }

    /**
     * 获取当前用户常用菜单。
     * <p>
     * 接口路径：POST /sys/menu/personal/common/list。
     * 需要认证：是，从登录上下文中获取当前用户与租户信息。
     * </p>
     * <p>
     * 执行步骤：
     * 1. 从登录上下文获取当前用户 ID 与租户 ID；
     * 2. 校验登录上下文是否完整，避免匿名请求查询个人数据；
     * 3. 读取兼容保留的 limit 参数；
     * 4. 调用服务层返回固定 Top 6 常用菜单结果。
     * </p>
     *
     * @param param 查询参数，当前 limit 参数仅为兼容保留
     * @return 当前用户常用菜单 Top 6 列表
     * @see ISysMenuService#getUserCommonMenus(Long, Long, Integer)
     */
    @PostMapping("/personal/common/list")
    public R<List<UserMenuPreferenceVO>> personalCommonMenus(@RequestBody(required = false) UserMenuQueryParam param) {
        // 1. 获取当前登录用户信息。
        // 从登录上下文中提取 userId 和 tenantId，为后续个人数据查询提供隔离条件。
        Long userId = UserContext.get();
        Long tenantId = TenantContext.get();

        // 2. 校验登录态是否完整。
        // 如果当前请求未携带有效的用户或租户上下文，直接返回未登录提示，避免误查他人数据。
        if (userId == null || tenantId == null) {
            return R.fail(CommonPrompt.NOT_LOGIN);
        }

        // 3. 解析兼容参数。
        // limit 参数仅为兼容历史前端保留，服务层会统一收敛为固定 Top 6。
        Integer limit = param == null ? null : param.getLimit();

        // 4. 调用服务层查询结果。
        // 使用当前用户与租户维度查询常用菜单，并统一封装为成功响应返回前端。
        return R.ok(menuService.getUserCommonMenus(userId, tenantId, limit));
    }

    /**
     * 获取当前用户收藏菜单。
     * <p>
     * 接口路径：POST /sys/menu/personal/favorites/list。
     * 需要认证：是，从登录上下文中获取当前用户与租户信息。
     * </p>
     * <p>
     * 执行步骤：
     * 1. 获取当前用户与租户信息；
     * 2. 校验登录态，确保收藏数据只在本人上下文下读取；
     * 3. 解析 limit 参数，控制收藏菜单返回数量；
     * 4. 调用服务层返回当前用户收藏菜单列表。
     * </p>
     *
     * @param param 查询参数，当前仅支持 limit 返回条数
     * @return 当前用户收藏菜单列表
     * @see ISysMenuService#getUserFavoriteMenus(Long, Long, Integer)
     */
    @PostMapping("/personal/favorites/list")
    public R<List<UserMenuPreferenceVO>> personalFavoriteMenus(@RequestBody(required = false) UserMenuQueryParam param) {
        // 1. 获取当前登录用户信息。
        // 使用登录上下文中的 userId 与 tenantId 作为收藏数据的查询条件。
        Long userId = UserContext.get();
        Long tenantId = TenantContext.get();

        // 2. 校验登录态。
        // 未登录时不允许读取任何个人收藏信息，直接返回统一未登录提示。
        if (userId == null || tenantId == null) {
            return R.fail(CommonPrompt.NOT_LOGIN);
        }

        // 3. 解析查询参数。
        // 将前端传入的 limit 提取出来，为服务层控制列表长度做准备。
        Integer limit = param == null ? null : param.getLimit();

        // 4. 查询收藏菜单并返回。
        // 服务层会自动过滤无权限菜单，控制器仅负责接收与返回结果。
        return R.ok(menuService.getUserFavoriteMenus(userId, tenantId, limit));
    }

    /**
     * 获取当前用户收藏管理列表。
     * <p>
     * 接口路径：POST /sys/menu/personal/favorites/manage/list。
     * 需要认证：是，从登录上下文中获取当前用户与租户信息。
     * </p>
     * <p>
     * 执行步骤：
     * 1. 获取当前用户与租户信息；
     * 2. 校验登录态，确保只读取当前用户自己的收藏数据；
     * 3. 调用服务层查询全部收藏菜单，并按用户自定义顺序返回。
     * </p>
     *
     * @return 当前用户收藏管理列表
     * @see ISysMenuService#getUserFavoriteManageMenus(Long, Long)
     */
    @PostMapping("/personal/favorites/manage/list")
    public R<List<UserMenuPreferenceVO>> personalFavoriteManageMenus() {
        // 1. 获取当前登录用户信息。
        // 使用登录上下文中的 userId 与 tenantId 作为收藏管理列表的查询条件。
        Long userId = UserContext.get();
        Long tenantId = TenantContext.get();

        // 2. 校验登录态。
        // 未登录时不允许读取个人收藏管理数据，直接返回未登录提示。
        if (userId == null || tenantId == null) {
            return R.fail(CommonPrompt.NOT_LOGIN);
        }

        // 3. 查询收藏管理列表并返回。
        // 服务层会自动过滤已无权限访问的菜单，并按当前排序顺序返回结果。
        return R.ok(menuService.getUserFavoriteManageMenus(userId, tenantId));
    }

    /**
     * 切换当前用户收藏菜单。
     * <p>
     * 接口路径：POST /sys/menu/personal/favorites/toggle。
     * 需要认证：是，从登录上下文中获取当前用户与租户信息。
     * </p>
     * <p>
     * 执行步骤：
     * 1. 获取当前用户与租户信息；
     * 2. 校验登录态与菜单路径参数；
     * 3. 调用服务层切换收藏状态；
     * 4. 返回切换后的收藏状态，true 表示已收藏，false 表示已取消收藏。
     * </p>
     *
     * @param param 请求参数，包含菜单完整路径 path
     * @return 收藏状态，true=已收藏，false=已取消收藏
     * @see ISysMenuService#toggleUserFavoriteMenu(Long, Long, String)
     */
    @PostMapping("/personal/favorites/toggle")
    public R<Boolean> togglePersonalFavorite(@RequestBody UserMenuPathParam param) {
        // 1. 获取当前登录用户信息。
        // 使用登录上下文中的用户与租户标识，保证收藏操作只作用于当前用户本人。
        Long userId = UserContext.get();
        Long tenantId = TenantContext.get();

        // 2. 校验登录态。
        // 登录信息缺失时不继续执行收藏写操作，避免脏数据进入数据库。
        if (userId == null || tenantId == null) {
            return R.fail(CommonPrompt.NOT_LOGIN);
        }

        // 3. 校验请求参数。
        // 菜单路径为空时无法定位具体菜单，因此直接返回参数为空提示。
        if (param == null || param.getPath() == null || param.getPath().trim().isEmpty()) {
            return R.fail(CommonPrompt.PARAM_EMPTY);
        }

        // 4. 调用服务层切换收藏状态并返回结果。
        // 服务层会根据菜单是否已收藏执行新增或删除操作，并返回最终状态。
        return R.ok(menuService.toggleUserFavoriteMenu(userId, tenantId, param.getPath()));
    }

    /**
     * 批量取消当前用户收藏菜单。
     * <p>
     * 接口路径：POST /sys/menu/personal/favorites/manage/batch-cancel。
     * 需要认证：是，从登录上下文中获取当前用户与租户信息。
     * </p>
     * <p>
     * 执行步骤：
     * 1. 获取当前用户与租户信息；
     * 2. 校验登录态与菜单路径列表参数；
     * 3. 调用服务层批量删除收藏记录；
     * 4. 返回实际取消收藏的数量。
     * </p>
     *
     * @param param 请求参数，paths 表示待取消收藏的菜单路径列表
     * @return 实际取消收藏数量
     * @see ISysMenuService#batchCancelUserFavoriteMenus(Long, Long, java.util.List)
     */
    @PostMapping("/personal/favorites/manage/batch-cancel")
    public R<Integer> batchCancelPersonalFavorites(@RequestBody UserMenuPathListParam param) {
        // 1. 获取当前登录用户信息。
        // 使用 userId 和 tenantId 锁定当前用户自己的收藏记录范围。
        Long userId = UserContext.get();
        Long tenantId = TenantContext.get();

        // 2. 校验登录态。
        // 未登录时不允许执行批量删除收藏操作，避免越权写入。
        if (userId == null || tenantId == null) {
            return R.fail(CommonPrompt.NOT_LOGIN);
        }

        // 3. 校验请求参数。
        // 空路径列表无法执行批量取消收藏，因此直接返回参数为空提示。
        if (param == null || param.getPaths() == null || param.getPaths().isEmpty()) {
            return R.fail(CommonPrompt.PARAM_EMPTY);
        }

        // 4. 调用服务层批量取消收藏并返回结果。
        // 服务层会对路径做标准化处理，只删除当前用户命中的收藏记录。
        return R.ok(menuService.batchCancelUserFavoriteMenus(userId, tenantId, param.getPaths()));
    }

    /**
     * 保存当前用户收藏菜单排序。
     * <p>
     * 接口路径：POST /sys/menu/personal/favorites/manage/sort。
     * 需要认证：是，从登录上下文中获取当前用户与租户信息。
     * </p>
     * <p>
     * 执行步骤：
     * 1. 获取当前用户与租户信息；
     * 2. 校验登录态与排序路径列表参数；
     * 3. 调用服务层按前端提交顺序保存排序；
     * 4. 返回布尔成功标记，供前端提示保存结果。
     * </p>
     *
     * @param param 请求参数，paths 的顺序即收藏菜单目标顺序
     * @return 操作结果，true 表示排序保存成功
     * @see ISysMenuService#sortUserFavoriteMenus(Long, Long, java.util.List)
     */
    @PostMapping("/personal/favorites/manage/sort")
    public R<Boolean> sortPersonalFavorites(@RequestBody UserMenuPathListParam param) {
        // 1. 获取当前登录用户信息。
        // 使用用户与租户上下文，确保排序只作用于当前用户自己的收藏列表。
        Long userId = UserContext.get();
        Long tenantId = TenantContext.get();

        // 2. 校验登录态。
        // 登录态缺失时不执行排序持久化，避免无主数据写入。
        if (userId == null || tenantId == null) {
            return R.fail(CommonPrompt.NOT_LOGIN);
        }

        // 3. 校验请求参数。
        // 前端至少需要传入一个路径，服务端才能按该顺序重排收藏菜单。
        if (param == null || param.getPaths() == null || param.getPaths().isEmpty()) {
            return R.fail(CommonPrompt.PARAM_EMPTY);
        }

        // 4. 调用服务层保存排序并返回结果。
        // 服务层会对路径去重和标准化，只更新当前用户已有的收藏记录。
        menuService.sortUserFavoriteMenus(userId, tenantId, param.getPaths());
        return R.ok(Boolean.TRUE);
    }

    /**
     * 上报当前用户菜单访问。
     * <p>
     * 接口路径：POST /sys/menu/personal/visit/report。
     * 需要认证：是，从登录上下文中获取当前用户与租户信息。
     * </p>
     * <p>
     * 执行步骤：
     * 1. 获取当前用户与租户信息；
     * 2. 校验登录态与菜单路径参数；
     * 3. 调用服务层写入或更新访问统计；
     * 4. 返回布尔成功标记，供前端无感埋点使用。
     * </p>
     *
     * @param param 请求参数，包含当前访问菜单的完整路径 path
     * @return 操作结果，true 表示上报成功
     * @see ISysMenuService#reportUserMenuVisit(Long, Long, String)
     */
    @PostMapping("/personal/visit/report")
    public R<Boolean> reportPersonalVisit(@RequestBody UserMenuPathParam param) {
        // 1. 获取当前登录用户信息。
        // 通过 userId 与 tenantId 标识本次访问记录归属，保证多租户与多用户隔离。
        Long userId = UserContext.get();
        Long tenantId = TenantContext.get();

        // 2. 校验登录态。
        // 没有有效登录信息时不写入访问统计，避免无主记录污染常用菜单数据。
        if (userId == null || tenantId == null) {
            return R.fail(CommonPrompt.NOT_LOGIN);
        }

        // 3. 校验菜单路径参数。
        // 空路径无法映射到有效菜单，因此直接返回参数错误提示。
        if (param == null || param.getPath() == null || param.getPath().trim().isEmpty()) {
            return R.fail(CommonPrompt.PARAM_EMPTY);
        }

        // 4. 调用服务层记录访问。
        // 服务层会自动完成路径标准化、授权校验以及访问次数累加。
        menuService.reportUserMenuVisit(userId, tenantId, param.getPath());
        return R.ok(Boolean.TRUE);
    }

    /**
     * 获取菜单树。
     * <p>
     * 接口信息：
     * </p>
     * <ul>
     *   <li>接口路径：POST /sys/menu/tree</li>
     *   <li>认证要求：是，需要登录状态</li>
     *   <li>权限要求：无特殊权限要求</li>
     * </ul>
     * <p>
     * 执行步骤：
     * </p>
     * <ol>
     *   <li>获取租户 ID：从 TenantContext 中获取当前租户 ID</li>
     *   <li>参数解析：从请求体中提取 moduleId 参数（可选）</li>
     *   <li>查询菜单树：调用 menuService.getMenuTree() 获取指定模块下的菜单树</li>
     *   <li>返回结果：将菜单树封装到 R 对象中返回</li>
     * </ol>
     * <p>
     * 参数说明：
     * </p>
     * <ul>
     *   <li>body.moduleId：模块 ID，Long 类型，可选，不传则返回所有菜单</li>
     * </ul>
     * <p>
     * 返回值说明：
     * </p>
     * <ul>
     *   <li>code：200 表示成功</li>
     *   <li>data：List<MenuTreeVO>，菜单树列表，包含：
     *     <ul>
     *       <li>id：菜单 ID</li>
     *       <li>parentId：父级菜单 ID</li>
     *       <li>name：菜单名称</li>
     *       <li>path：路由路径</li>
     *       <li>icon：菜单图标</li>
     *       <li>type：菜单类型（catalog/menu/button）</li>
     *       <li>children：子菜单列表（递归结构）</li>
     *     </ul>
     *   </li>
     *   <li>message：成功或失败提示</li>
     * </ul>
     *
     * @param body 请求体，包含 moduleId 参数（可选）
     * @return {@link R} 包含菜单树列表的统一返回结构
     * @see com.forgex.sys.domain.vo.MenuTreeVO
     * @see com.forgex.sys.service.ISysMenuService#getMenuTree(Long, Long)
     */
    @RequirePerm("sys:menu:view")
    @PostMapping("/tree")
    public R<List<MenuTreeVO>> tree(@RequestBody Map<String, Object> body) {
        // 1. 参数解析
        Long tenantId = TenantContext.get();
        Long moduleId = parseLong(body.get("moduleId"));
        
        // 2. 调用Service
        List<MenuTreeVO> tree = menuService.getMenuTree(tenantId, moduleId);
        
        // 3. 返回结果
        return R.ok(tree);
    }
    
    /**
     * 分页查询菜单列表。
     * <p>
     * 接口信息：
     * </p>
     * <ul>
     *   <li>接口路径：POST /sys/menu/page</li>
     *   <li>认证要求：是，需要登录状态</li>
     *   <li>权限要求：sys:menu:view（建议配置）</li>
     * </ul>
     * <p>
     * 执行步骤：
     * </p>
     * <ol>
     *   <li>构建分页对象：使用 query 中的 pageNum 和 pageSize 构建 Page 对象</li>
     *   <li>分页查询：调用 menuService.pageMenus() 进行分页查询</li>
     *   <li>返回结果：将分页结果封装到 R 对象中返回</li>
     * </ol>
     * <p>
     * 参数说明：
     * </p>
     * <ul>
     *   <li>query.pageNum：页码，Integer 类型，默认 1</li>
     *   <li>query.pageSize：每页条数，Integer 类型，默认 10</li>
     *   <li>query.tenantId：租户 ID，Long 类型，可选</li>
     *   <li>query.moduleId：模块 ID，Long 类型，可选</li>
     *   <li>query.name：菜单名称，String 类型，可选，支持模糊查询</li>
     *   <li>query.type：菜单类型，String 类型，可选</li>
     *   <li>query.status：状态，Boolean 类型，可选</li>
     * </ul>
     * <p>
     * 返回值说明：
     * </p>
     * <ul>
     *   <li>code：200 表示成功</li>
     *   <li>data：IPage<SysMenuDTO> 分页对象，包含：
     *     <ul>
     *       <li>records：菜单 DTO 列表</li>
     *       <li>total：总记录数</li>
     *       <li>size：每页条数</li>
     *       <li>current：当前页码</li>
     *     </ul>
     *   </li>
     *   <li>message：成功或失败提示</li>
     * </ul>
     *
     * @param query 菜单查询参数对象
     * @return {@link R} 包含分页菜单列表的统一返回结构
     * @see com.baomidou.mybatisplus.core.metadata.IPage
     * @see com.forgex.sys.domain.dto.SysMenuDTO
     * @see com.forgex.sys.service.ISysMenuService#pageMenus(Page, SysMenuQueryDTO)
     */
    @RequirePerm("sys:menu:view")
    @PostMapping("/page")
    public R<IPage<SysMenuDTO>> page(@RequestBody SysMenuQueryDTO query) {
        // 使用 BaseGetParam 中的 pageNum 和 pageSize
        Page<SysMenu> page = new Page<>(query.getPageNum(), query.getPageSize());
        return R.ok(menuService.pageMenus(page, query));
    }
    
    /**
     * 查询菜单列表（不分页）。
     * <p>
     * 接口信息：
     * </p>
     * <ul>
     *   <li>接口路径：POST /sys/menu/list</li>
     *   <li>认证要求：是，需要登录状态</li>
     *   <li>权限要求：sys:menu:view（建议配置）</li>
     * </ul>
     * <p>
     * 执行步骤：
     * </p>
     * <ol>
     *   <li>查询菜单：调用 menuService.listMenus() 查询所有符合条件的菜单</li>
     *   <li>返回结果：将菜单列表封装到 R 对象中返回</li>
     * </ol>
     * <p>
     * 参数说明：
     * </p>
     * <ul>
     *   <li>query.tenantId：租户 ID，Long 类型，可选</li>
     *   <li>query.moduleId：模块 ID，Long 类型，可选</li>
     *   <li>query.name：菜单名称，String 类型，可选，支持模糊查询</li>
     *   <li>query.type：菜单类型，String 类型，可选</li>
     *   <li>query.status：状态，Boolean 类型，可选</li>
     * </ul>
     * <p>
     * 返回值说明：
     * </p>
     * <ul>
     *   <li>code：200 表示成功</li>
     *   <li>data：List<SysMenuDTO>，菜单 DTO 列表</li>
     *   <li>message：成功或失败提示</li>
     * </ul>
     *
     * @param query 菜单查询参数对象
     * @return {@link R} 包含菜单列表的统一返回结构
     * @see com.forgex.sys.domain.dto.SysMenuDTO
     * @see com.forgex.sys.service.ISysMenuService#listMenus(SysMenuQueryDTO)
     */
    @RequirePerm("sys:menu:view")
    @PostMapping("/list")
    public R<List<SysMenuDTO>> list(@RequestBody SysMenuQueryDTO query) {
        return R.ok(menuService.listMenus(query));
    }
    
    /**
     * 根据ID获取菜单详情
     */
    @RequirePerm("sys:menu:view")
    @PostMapping("/detail")
    public R<SysMenuDTO> detail(@RequestBody Map<String, Object> body) {
        Long id = parseLong(body.get("id"));
        menuValidator.validateId(id);
        return R.ok(menuService.getMenuById(id));
    }
    
    /**
     * 新增菜单。
     * <p>
     * 接口信息：
     * </p>
     * <ul>
     *   <li>接口路径：POST /sys/menu/create</li>
     *   <li>认证要求：是，需要登录状态</li>
     *   <li>权限要求：sys:menu:add</li>
     * </ul>
     * <p>
     * 执行步骤：
     * </p>
     * <ol>
     *   <li>数据校验：调用 menuValidator.validateForAdd() 校验新增数据的完整性和合法性</li>
     *   <li>调用 Service：调用 menuService.addMenu() 新增菜单</li>
     *   <li>返回结果：返回成功提示</li>
     * </ol>
     * <p>
     * 参数说明：
     * </p>
     * <ul>
     *   <li>menuDTO.tenantId：租户 ID，Long 类型，必填</li>
     *   <li>menuDTO.moduleId：所属模块 ID，Long 类型，必填</li>
     *   <li>menuDTO.parentId：父级菜单 ID，Long 类型，可选</li>
     *   <li>menuDTO.type：菜单类型，String 类型，必填（catalog/menu/button）</li>
     *   <li>menuDTO.path：路由路径，String 类型，可选</li>
     *   <li>menuDTO.name：菜单名称，String 类型，必填</li>
     *   <li>menuDTO.nameI18nJson：国际化名称 JSON，String 类型，可选</li>
     *   <li>menuDTO.icon：菜单图标，String 类型，可选</li>
     *   <li>menuDTO.componentKey：前端组件 Key，String 类型，可选</li>
     *   <li>menuDTO.permKey：权限标识，String 类型，可选</li>
     *   <li>menuDTO.orderNum：排序号，Integer 类型，可选</li>
     *   <li>menuDTO.visible：是否可见，Boolean 类型，可选</li>
     *   <li>menuDTO.status：状态，Boolean 类型，可选</li>
     * </ul>
     * <p>
     * 返回值说明：
     * </p>
     * <ul>
     *   <li>code：200 表示成功</li>
     *   <li>data：null</li>
     *   <li>message：创建成功提示</li>
     * </ul>
     *
     * @param menuDTO 菜单 DTO 对象
     * @return {@link R} 空内容的统一返回结构
     * @throws com.forgex.common.exception.I18nBusinessException 当参数校验失败时抛出
     * @see com.forgex.sys.domain.dto.SysMenuDTO
     * @see com.forgex.sys.service.ISysMenuService#addMenu(SysMenuDTO)
     * @see com.forgex.sys.validator.MenuValidator#validateForAdd(SysMenuDTO)
     */
    @RequirePerm("sys:menu:add")
    @PostMapping("/create")
    public R<Void> create(@RequestBody @Validated SysMenuDTO menuDTO) {
        // 1. 数据校验
        menuValidator.validateForAdd(menuDTO);
        
        // 2. 调用Service
        menuService.addMenu(menuDTO);
        
        // 3. 返回结果
        return R.ok(CommonPrompt.CREATE_SUCCESS);
    }
    
    /**
     * 更新菜单。
     * <p>
     * 接口信息：
     * </p>
     * <ul>
     *   <li>接口路径：POST /sys/menu/update</li>
     *   <li>认证要求：是，需要登录状态</li>
     *   <li>权限要求：sys:menu:edit</li>
     * </ul>
     * <p>
     * 执行步骤：
     * </p>
     * <ol>
     *   <li>数据校验：调用 menuValidator.validateForUpdate() 校验更新数据的完整性和合法性</li>
     *   <li>调用 Service：调用 menuService.updateMenu() 更新菜单</li>
     *   <li>返回结果：返回成功提示</li>
     * </ol>
     * <p>
     * 参数说明：
     * </p>
     * <ul>
     *   <li>menuDTO.id：菜单 ID，Long 类型，必填</li>
     *   <li>menuDTO.tenantId：租户 ID，Long 类型，必填</li>
     *   <li>menuDTO.moduleId：所属模块 ID，Long 类型，必填</li>
     *   <li>menuDTO.parentId：父级菜单 ID，Long 类型，可选</li>
     *   <li>menuDTO.type：菜单类型，String 类型，必填</li>
     *   <li>menuDTO.path：路由路径，String 类型，可选</li>
     *   <li>menuDTO.name：菜单名称，String 类型，必填</li>
     *   <li>menuDTO.nameI18nJson：国际化名称 JSON，String 类型，可选</li>
     *   <li>menuDTO.icon：菜单图标，String 类型，可选</li>
     *   <li>menuDTO.componentKey：前端组件 Key，String 类型，可选</li>
     *   <li>menuDTO.permKey：权限标识，String 类型，可选</li>
     *   <li>menuDTO.orderNum：排序号，Integer 类型，可选</li>
     *   <li>menuDTO.visible：是否可见，Boolean 类型，可选</li>
     *   <li>menuDTO.status：状态，Boolean 类型，可选</li>
     * </ul>
     * <p>
     * 返回值说明：
     * </p>
     * <ul>
     *   <li>code：200 表示成功</li>
     *   <li>data：null</li>
     *   <li>message：更新成功提示</li>
     * </ul>
     *
     * @param menuDTO 菜单 DTO 对象
     * @return {@link R} 空内容的统一返回结构
     * @throws com.forgex.common.exception.I18nBusinessException 当参数校验失败时抛出
     * @see com.forgex.sys.domain.dto.SysMenuDTO
     * @see com.forgex.sys.service.ISysMenuService#updateMenu(SysMenuDTO)
     * @see com.forgex.sys.validator.MenuValidator#validateForUpdate(SysMenuDTO)
     */
    @RequirePerm("sys:menu:edit")
    @PostMapping("/update")
    public R<Void> update(@RequestBody @Validated SysMenuDTO menuDTO) {
        // 1. 数据校验
        menuValidator.validateForUpdate(menuDTO);
        
        // 2. 调用Service
        menuService.updateMenu(menuDTO);
        
        // 3. 返回结果
        return R.ok(CommonPrompt.UPDATE_SUCCESS);
    }
    
    /**
     * 删除菜单。
     * <p>
     * 接口信息：
     * </p>
     * <ul>
     *   <li>接口路径：POST /sys/menu/delete</li>
     *   <li>认证要求：是，需要登录状态</li>
     *   <li>权限要求：sys:menu:delete</li>
     * </ul>
     * <p>
     * 执行步骤：
     * </p>
     * <ol>
     *   <li>参数解析：从请求体中提取 id 参数</li>
     *   <li>数据校验：调用 menuValidator.validateForDelete() 校验 ID 是否有效，检查是否有子菜单和角色关联</li>
     *   <li>调用 Service：调用 menuService.deleteMenu() 删除菜单（包括子菜单）</li>
     *   <li>返回结果：返回成功提示</li>
     * </ol>
     * <p>
     * 参数说明：
     * </p>
     * <ul>
     *   <li>body.id：菜单 ID，Long 类型，必填</li>
     * </ul>
     * <p>
     * 返回值说明：
     * </p>
     * <ul>
     *   <li>code：200 表示成功</li>
     *   <li>data：null</li>
     *   <li>message：删除成功提示</li>
     * </ul>
     * <p>
     * 注意事项：
     * </p>
     * <ul>
     *   <li>删除菜单会同时删除其所有子菜单</li>
     *   <li>如果菜单已被角色关联，则不允许删除</li>
     * </ul>
     *
     * @param body 请求体，包含 id 参数
     * @return {@link R} 空内容的统一返回结构
     * @throws com.forgex.common.exception.I18nBusinessException 当 ID 为空、菜单不存在、有子菜单或有角色关联时抛出
     * @see com.forgex.sys.service.ISysMenuService#deleteMenu(Long)
     * @see com.forgex.sys.validator.MenuValidator#validateForDelete(Long)
     */
    @RequirePerm("sys:menu:delete")
    @PostMapping("/delete")
    public R<Void> delete(@RequestBody Map<String, Object> body) {
        // 1. 解析参数
        Long id = parseLong(body.get("id"));
        
        // 2. 数据校验
        menuValidator.validateForDelete(id);
        
        // 3. 调用Service
        menuService.deleteMenu(id);
        
        // 4. 返回结果
        return R.ok(CommonPrompt.DELETE_SUCCESS);
    }
    
    /**
     * 批量删除菜单。
     * <p>
     * 接口信息：
     * </p>
     * <ul>
     *   <li>接口路径：POST /sys/menu/batchDelete</li>
     *   <li>认证要求：是，需要登录状态</li>
     *   <li>权限要求：sys:menu:delete</li>
     * </ul>
     * <p>
     * 执行步骤：
     * </p>
     * <ol>
     *   <li>参数解析：从请求体中提取 ids 参数（菜单 ID 列表）</li>
     *   <li>数据校验：遍历每个 ID，调用 menuValidator.validateForDelete() 校验 ID 是否有效</li>
     *   <li>调用 Service：调用 menuService.batchDeleteMenus() 批量删除菜单</li>
     *   <li>返回结果：返回成功提示</li>
     * </ol>
     * <p>
     * 参数说明：
     * </p>
     * <ul>
     *   <li>body.ids：菜单 ID 列表，List<Long> 类型，必填</li>
     * </ul>
     * <p>
     * 返回值说明：
     * </p>
     * <ul>
     *   <li>code：200 表示成功</li>
     *   <li>data：null</li>
     *   <li>message：删除成功提示</li>
     * </ul>
     * <p>
     * 注意事项：
     * </p>
     * <ul>
     *   <li>批量删除会对每个 ID 执行单独的删除逻辑（包括子菜单）</li>
     *   <li>如果任何一个 ID 校验失败，整个操作会回滚</li>
     *   <li>如果任何菜单已被角色关联，则不允许删除</li>
     * </ul>
     *
     * @param body 请求体，包含 ids 参数
     * @return {@link R} 空内容的统一返回结构
     * @throws com.forgex.common.exception.I18nBusinessException 当 IDs 为空、任何一个菜单不存在、有子菜单或有角色关联时抛出
     * @see com.forgex.sys.service.ISysMenuService#batchDeleteMenus(List)
     * @see com.forgex.sys.validator.MenuValidator#validateForDelete(Long)
     */
    @RequirePerm("sys:menu:delete")
    @PostMapping("/batchDelete")
    public R<Void> batchDelete(@RequestBody Map<String, Object> body) {
        // 1. 解析参数
        @SuppressWarnings("unchecked")
        List<Long> ids = (List<Long>) body.get("ids");
        
        // 2. 校验每个ID
        for (Long id : ids) {
            menuValidator.validateForDelete(id);
        }
        
        // 3. 调用Service
        menuService.batchDeleteMenus(ids);
        
        // 4. 返回结果
        return R.ok(CommonPrompt.DELETE_SUCCESS);
    }
    
    /**
     * 解析Long类型参数
     */
    private Long parseLong(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        if (obj instanceof String) {
            try {
                return Long.parseLong((String) obj);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
