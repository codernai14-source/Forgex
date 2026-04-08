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
import com.forgex.common.web.R;
import com.forgex.sys.domain.dto.SysMenuDTO;
import com.forgex.sys.domain.dto.SysMenuQueryDTO;
import com.forgex.sys.domain.entity.SysMenu;
import com.forgex.sys.domain.vo.MenuTreeVO;
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
 * @date 2025-01-07
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
     * @throws BusinessException 当参数校验失败时抛出
     * @see com.forgex.sys.domain.vo.UserRoutesVO
     * @see com.forgex.sys.service.ISysMenuService#getUserRoutes(String, Long)
     * @see com.forgex.sys.validator.MenuValidator#validateRoutesParams(String, Long)
     */
    @PostMapping("/routes")
    public R<UserRoutesVO> routes(@RequestBody Map<String, Object> body) {
        // 1. 参数解析
        String account = (String) body.get("account");
        Long tenantId = TenantContext.get();
        
        // 2. 参数校验
        menuValidator.validateRoutesParams(account, tenantId);
        
        // 3. 调用Service
        UserRoutesVO routes = menuService.getUserRoutes(account, tenantId);
        
        // 4. 返回结果
        return R.ok(routes);
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
    @PostMapping("/list")
    public R<List<SysMenuDTO>> list(@RequestBody SysMenuQueryDTO query) {
        return R.ok(menuService.listMenus(query));
    }
    
    /**
     * 根据ID获取菜单详情
     */
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
     * @throws BusinessException 当参数校验失败时抛出
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
     * @throws BusinessException 当参数校验失败时抛出
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
     * @throws BusinessException 当 ID 为空、菜单不存在、有子菜单或有角色关联时抛出
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
     * @throws BusinessException 当 IDs 为空、任何一个菜单不存在、有子菜单或有角色关联时抛出
     * @see com.forgex.sys.service.ISysMenuService#batchDeleteMenus(List)
     * @see com.forgex.sys.validator.MenuValidator#validateForDelete(Long)
     */
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
