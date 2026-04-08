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
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.util.CurrentUserUtils;
import com.forgex.common.web.R;
import com.forgex.sys.domain.dto.DictDTO;
import com.forgex.sys.domain.param.DictItemsByPathParam;
import com.forgex.sys.domain.param.DictItemsParam;
import com.forgex.sys.domain.param.IdParam;
import com.forgex.sys.domain.vo.DictItemVO;
import com.forgex.sys.domain.vo.DictTreeVO;
import com.forgex.sys.service.IDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据字典 Controller
 * <p>
 * 提供数据字典的树形结构查询、分页查询、字典项查询以及字典的新增、修改、删除等功能。
 * 所有接口自动从 CurrentUserUtils 获取当前租户 ID，无需前端传递。
 * </p>
 * <p>主要功能：</p>
 * <ul>
 *   <li>{@link #tree()} - 获取数据字典树形结构</li>
 *   <li>{@link #page(Map)} - 分页查询数据字典树</li>
 *   <li>{@link #items(DictItemsParam)} - 根据字典编码查询字典项</li>
 *   <li>{@link #itemsByPath(DictItemsByPathParam)} - 根据节点路径查询字典项</li>
 *   <li>{@link #create(DictDTO)} - 新增数据字典</li>
 *   <li>{@link #update(DictDTO)} - 更新数据字典</li>
 *   <li>{@link #delete(IdParam)} - 删除数据字典</li>
 * </ul>
 * <p>接口说明：</p>
 * <ul>
 *   <li>所有接口路径：/sys/dict/*</li>
 *   <li>所有接口均为 POST 请求</li>
 *   <li>需要登录认证</li>
 *   <li>返回格式统一为 {@link R} 类型</li>
 * </ul>
 * 
 * @author coder_nai@163.com
 * @version 1.0.0
 * @date 2025-01-13
 * @see IDictService
 * @see DictTreeVO
 * @see DictItemVO
 */
@RestController
@RequestMapping("/sys/dict")
public class SysDictController {

    @Autowired
    private IDictService dictService;

    /**
     * 获取数据字典树形结构
     * <p>
     * 接口路径：POST /sys/dict/tree
     * </p>
     * <p>认证要求：是</p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>从 CurrentUserUtils 获取当前租户 ID</li>
     *   <li>调用 Service 层获取字典树形结构</li>
     *   <li>返回字典树列表</li>
     * </ol>
     * 
     * @return {@link R} 包含字典树列表的统一返回结构
     *         - code: 状态码（200=成功）
     *         - data: 字典树列表（List&lt;DictTreeVO&gt;）
     *         - message: 提示信息
     * @see DictTreeVO
     */
    @PostMapping("/tree")
    public R<List<DictTreeVO>> tree() {
        // 1. 从 CurrentUserUtils 获取当前租户 ID
        // 2. 调用 Service 层获取字典树形结构
        // 3. 返回字典树列表
        return R.ok(dictService.getDictTree(getCurrentTenantId()));
    }

    /**
     * 分页查询数据字典树
     * <p>
     * 接口路径：POST /sys/dict/page
     * </p>
     * <p>认证要求：是</p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>从请求参数中解析分页参数（pageNum/pageSize）</li>
     *   <li>从 CurrentUserUtils 获取当前租户 ID</li>
     *   <li>调用 Service 层分页查询字典树</li>
     *   <li>返回分页结果</li>
     * </ol>
     * 
     * @param params 请求参数（可选）
     *               - pageNum: 页码（可选，默认 1）
     *               - pageSize: 每页大小（可选，默认 20）
     * @return {@link R} 包含字典树分页结果的统一返回结构
     *         - code: 状态码（200=成功）
     *         - data: 分页结果（IPage&lt;DictTreeVO&gt;）
     *         - message: 提示信息
     * @see DictTreeVO
     */
    @PostMapping("/page")
    public R<IPage<DictTreeVO>> page(@RequestBody(required = false) Map<String, Object> params) {
        // 1. 从请求参数中解析分页参数
        long pageNum = parseLong(params == null ? null : params.get("pageNum"), 1L);
        long pageSize = parseLong(params == null ? null : params.get("pageSize"), 20L);
        // 2. 调用 Service 层分页查询字典树
        // 3. 返回分页结果
        return R.ok(dictService.pageDictTree(getCurrentTenantId(), pageNum, pageSize));
    }

    /**
     * 根据字典编码查询字典项
     * <p>
     * 接口路径：POST /sys/dict/items
     * </p>
     * <p>认证要求：是</p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收字典编码参数</li>
     *   <li>校验字典编码是否为空</li>
     *   <li>从 CurrentUserUtils 获取当前租户 ID</li>
     *   <li>调用 Service 层根据字典编码查询字典项</li>
     *   <li>返回字典项列表</li>
     * </ol>
     * 
     * @param param 字典项查询参数
     *              - dictCode: 字典编码（必填）
     * @return {@link R} 包含字典项列表的统一返回结构
     *         - code: 状态码（200=成功）
     *         - data: 字典项列表（List&lt;DictItemVO&gt;）
     *         - message: 提示信息（字典编码为空时返回错误）
     * @throws BusinessException 字典编码为空时抛出
     * @see DictItemsParam
     * @see DictItemVO
     */
    @PostMapping("/items")
    public R<List<DictItemVO>> items(@RequestBody DictItemsParam param) {
        // 1. 接收字典编码参数
        String dictCode = param.getDictCode();
        // 2. 校验字典编码是否为空
        if (dictCode == null || dictCode.isEmpty()) {
            return R.fail(CommonPrompt.DICT_CODE_CANNOT_BE_EMPTY);
        }
        // 3. 调用 Service 层查询字典项
        // 4. 返回字典项列表
        return R.ok(dictService.getDictItemsByCode(dictCode, getCurrentTenantId()));
    }

    /**
     * 根据节点路径查询字典项
     * <p>
     * 接口路径：POST /sys/dict/itemsByPath
     * </p>
     * <p>认证要求：是</p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收节点路径参数</li>
     *   <li>校验节点路径是否为空</li>
     *   <li>从 CurrentUserUtils 获取当前租户 ID</li>
     *   <li>调用 Service 层根据节点路径查询字典项</li>
     *   <li>返回字典项列表</li>
     * </ol>
     * 
     * @param param 字典项查询参数
     *              - nodePath: 节点路径（必填）
     * @return {@link R} 包含字典项列表的统一返回结构
     *         - code: 状态码（200=成功）
     *         - data: 字典项列表（List&lt;DictItemVO&gt;）
     *         - message: 提示信息（节点路径为空时返回错误）
     * @throws BusinessException 节点路径为空时抛出
     * @see DictItemsByPathParam
     * @see DictItemVO
     */
    @PostMapping("/itemsByPath")
    public R<List<DictItemVO>> itemsByPath(@RequestBody DictItemsByPathParam param) {
        // 1. 接收节点路径参数
        String nodePath = param == null ? null : param.getNodePath();
        // 2. 校验节点路径是否为空
        if (nodePath == null || nodePath.isEmpty()) {
            return R.fail(CommonPrompt.NODE_PATH_CANNOT_BE_EMPTY);
        }
        // 3. 调用 Service 层查询字典项
        // 4. 返回字典项列表
        return R.ok(dictService.getDictItemsByPath(nodePath, getCurrentTenantId()));
    }

    /**
     * 新增数据字典
     * <p>
     * 接口路径：POST /sys/dict/create
     * </p>
     * <p>认证要求：是</p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收字典 DTO 参数</li>
     *   <li>从 CurrentUserUtils 获取当前租户 ID 并设置</li>
     *   <li>调用 Service 层新增字典</li>
     *   <li>返回新增成功提示</li>
     * </ol>
     * 
     * @param dictDTO 字典数据传输对象
     *                - dictCode: 字典编码（必填）
     *                - dictName: 字典名称（必填）
     *                - dictType: 字典类型（可选）
     *                - remark: 备注（可选）
     * @return {@link R} 操作结果
     *         - code: 状态码（200=成功）
     *         - data: true=成功
     *         - message: 提示信息（创建成功）
     * @see DictDTO
     */
    @PostMapping("/create")
    public R<Boolean> create(@RequestBody DictDTO dictDTO) {
        // 1. 从 CurrentUserUtils 获取当前租户 ID 并设置
        dictDTO.setTenantId(getCurrentTenantId());
        // 2. 调用 Service 层新增字典
        dictService.addDict(dictDTO);
        // 3. 返回新增成功提示
        return R.ok(CommonPrompt.CREATE_SUCCESS, true);
    }

    /**
     * 更新数据字典
     * <p>
     * 接口路径：POST /sys/dict/update
     * </p>
     * <p>认证要求：是</p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收字典 DTO 参数</li>
     *   <li>从 CurrentUserUtils 获取当前租户 ID 并设置</li>
     *   <li>调用 Service 层更新字典</li>
     *   <li>返回更新成功提示</li>
     * </ol>
     * 
     * @param dictDTO 字典数据传输对象
     *                - id: 字典 ID（必填）
     *                - dictCode: 字典编码（可选）
     *                - dictName: 字典名称（可选）
     *                - dictType: 字典类型（可选）
     *                - remark: 备注（可选）
     * @return {@link R} 操作结果
     *         - code: 状态码（200=成功）
     *         - data: true=成功
     *         - message: 提示信息（更新成功）
     * @see DictDTO
     */
    @PostMapping("/update")
    public R<Boolean> update(@RequestBody DictDTO dictDTO) {
        // 1. 从 CurrentUserUtils 获取当前租户 ID 并设置
        dictDTO.setTenantId(getCurrentTenantId());
        // 2. 调用 Service 层更新字典
        dictService.updateDict(dictDTO);
        // 3. 返回更新成功提示
        return R.ok(CommonPrompt.UPDATE_SUCCESS, true);
    }

    /**
     * 删除数据字典
     * <p>
     * 接口路径：POST /sys/dict/delete
     * </p>
     * <p>认证要求：是</p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收字典 ID 参数</li>
     *   <li>校验字典 ID 是否为空</li>
     *   <li>从 CurrentUserUtils 获取当前租户 ID</li>
     *   <li>调用 Service 层删除字典</li>
     *   <li>返回删除成功提示</li>
     * </ol>
     * 
     * @param param 字典 ID 参数
     *              - id: 字典 ID（必填）
     * @return {@link R} 操作结果
     *         - code: 状态码（200=成功）
     *         - data: true=成功
     *         - message: 提示信息（删除成功）
     * @throws BusinessException 字典 ID 为空时抛出
     * @see IdParam
     */
    @PostMapping("/delete")
    public R<Boolean> delete(@RequestBody IdParam param) {
        // 1. 接收字典 ID 参数
        Long id = param.getId();
        // 2. 校验字典 ID 是否为空
        if (id == null) {
            return R.fail(CommonPrompt.DICT_ID_CANNOT_BE_EMPTY);
        }
        // 3. 调用 Service 层删除字典
        dictService.deleteDict(id, getCurrentTenantId());
        // 4. 返回删除成功提示
        return R.ok(CommonPrompt.DELETE_SUCCESS, true);
    }

    /**
     * 获取当前租户 ID
     * <p>
     * 从 CurrentUserUtils 获取当前登录用户的租户 ID，如果为空则返回默认租户 ID（1L）。
     * </p>
     *
     * @return 当前租户 ID，获取失败返回默认租户 ID（1L）
     * @see CurrentUserUtils
     */
    private Long getCurrentTenantId() {
        // 1. 从 CurrentUserUtils 获取当前租户 ID
        Long tenantId = CurrentUserUtils.getTenantId();
        // 2. 为空则返回默认租户 ID
        return tenantId != null ? tenantId : 1L;
    }

    /**
     * 解析 Long 类型参数
     * <p>
     * 将 Object 类型的参数转换为 long 类型，支持 Number 和 String 类型。
     * </p>
     *
     * @param value 待转换的对象
     * @param defaultValue 默认值（转换失败时返回）
     * @return 转换后的 long 值，转换失败返回默认值
     */
    private long parseLong(Object value, long defaultValue) {
        // 1. null 值检查
        if (value == null) {
            return defaultValue;
        }
        // 2. 尝试转换为 long 类型
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            // 3. 转换失败返回默认值
            return defaultValue;
        }
    }
}
