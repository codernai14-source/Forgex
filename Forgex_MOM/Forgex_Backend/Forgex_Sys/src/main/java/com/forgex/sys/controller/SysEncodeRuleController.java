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
import com.forgex.common.util.CurrentUserUtils;
import com.forgex.common.web.R;
import com.forgex.sys.domain.param.BatchIdsParam;
import com.forgex.sys.domain.param.EncodeRuleGetParam;
import com.forgex.sys.domain.param.EncodeRulePageParam;
import com.forgex.sys.domain.param.EncodeRuleSaveParam;
import com.forgex.sys.domain.param.IdParam;
import com.forgex.sys.domain.vo.EncodeRuleVO;
import com.forgex.sys.enums.SysPromptEnum;
import com.forgex.sys.service.ISysEncodeRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 编码规则管理控制器
 * <p>
 * 提供编码规则的 RESTful API 接口，包括编码规则的增删改查、分页查询、编码生成等功能。
 * </p>
 * <p>主要接口：</p>
 * <ul>
 *   <li>POST /page - 分页查询编码规则列表</li>
 *   <li>POST /list - 查询编码规则列表（不分页）</li>
 *   <li>POST /get - 根据 ID 获取编码规则详情</li>
 *   <li>POST /save - 保存编码规则（新增或更新）</li>
 *   <li>POST /delete - 删除编码规则</li>
 *   <li>POST /batchDelete - 批量删除编码规则</li>
 *   <li>POST /generate - 根据规则代码生成编码</li>
 *   <li>POST /enable - 启用编码规则</li>
 *   <li>POST /disable - 禁用编码规则</li>
 * </ul>
 * <p>使用说明：</p>
 * <ul>
 *   <li>所有接口都需要登录认证</li>
 *   <li>支持租户隔离，自动从上下文获取租户 ID</li>
 *   <li>编码生成接口支持跨租户调用（用于公共编码规则）</li>
 *   <li>数据源配置：使用 common 库（common 数据源）</li>
 * </ul>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-10
 * @see ISysEncodeRuleService
 */
@RestController
@RequestMapping("/sys/encodeRule")
@RequiredArgsConstructor
public class SysEncodeRuleController {

    /**
     * 编码规则 Service
     */
    private final ISysEncodeRuleService encodeRuleService;

    /**
     * 分页查询编码规则列表
     * <p>
     * 根据查询条件分页查询编码规则列表，支持按规则代码、规则名称、模块等条件过滤。
     * </p>
     *
     * @param param 查询参数
     * @return 分页结果
     */
    @PostMapping("/page")
    public R<IPage<EncodeRuleVO>> page(@RequestBody(required = false) EncodeRulePageParam param) {
        EncodeRulePageParam query = param == null ? new EncodeRulePageParam() : param;
        IPage<com.forgex.sys.domain.dto.EncodeRuleDTO> dtoPage = encodeRuleService.pageEncodeRules(
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(query.getPageNum(), query.getPageSize()),
            query
        );
        IPage<EncodeRuleVO> voPage = dtoPage.convert(dto -> {
            EncodeRuleVO vo = new EncodeRuleVO();
            org.springframework.beans.BeanUtils.copyProperties(dto, vo);
            // TODO: 字典翻译
            vo.setIsEnabledText(dto.getIsEnabled() ? "启用" : "禁用");
            return vo;
        });
        return R.ok(voPage);
    }

    /**
     * 查询编码规则列表（不分页）
     * <p>
     * 根据查询条件查询所有符合条件的编码规则列表。
     * </p>
     *
     * @param param 查询参数
     * @return 编码规则列表
     */
    @PostMapping("/list")
    public R<List<EncodeRuleVO>> list(@RequestBody(required = false) EncodeRulePageParam param) {
        EncodeRulePageParam query = param == null ? new EncodeRulePageParam() : param;
        List<EncodeRuleVO> list = encodeRuleService.listEncodeRules(query).stream()
            .map(dto -> encodeRuleService.getEncodeRuleVOById(dto.getId()))
            .collect(java.util.stream.Collectors.toList());
        return R.ok(list);
    }

    /**
     * 根据 ID 获取编码规则详情
     * <p>
     * 根据编码规则 ID 查询详细信息，包含主表和明细表的完整数据。
     * </p>
     *
     * @param param 查询参数
     * @return 编码规则详情
     */
    @PostMapping("/get")
    public R<EncodeRuleVO> get(@RequestBody EncodeRuleGetParam param) {
        EncodeRuleVO vo = encodeRuleService.getEncodeRuleVOById(param.getId());
        if (vo == null) {
            return R.fail(SysPromptEnum.ENCODE_RULE_NOT_FOUND);
        }
        return R.ok(vo);
    }

    /**
     * 保存编码规则（新增或更新）
     * <p>
     * 根据参数中是否包含 ID 来判断是新增还是更新操作。
     * </p>
     *
     * @param param 保存参数
     * @return 保存结果
     */
    @PostMapping("/save")
    public R<Long> save(@RequestBody EncodeRuleSaveParam param) {
        Long ruleId = encodeRuleService.saveEncodeRule(param);
        return R.ok(SysPromptEnum.ENCODE_RULE_CREATE_SUCCESS, ruleId);
    }

    /**
     * 删除编码规则
     * <p>
     * 根据编码规则 ID 删除规则及其关联的明细数据。
     * </p>
     *
     * @param param 删除参数
     * @return 删除结果
     */
    @PostMapping("/delete")
    public R<Void> delete(@RequestBody IdParam param) {
        encodeRuleService.deleteEncodeRule(param.getId());
        return R.ok(SysPromptEnum.ENCODE_RULE_DELETE_SUCCESS);
    }

    /**
     * 批量删除编码规则
     * <p>
     * 批量删除多个编码规则及其关联的明细数据。
     * </p>
     *
     * @param param 批量删除参数
     * @return 批量删除结果
     */
    @PostMapping("/batchDelete")
    public R<Void> batchDelete(@RequestBody BatchIdsParam param) {
        encodeRuleService.batchDeleteEncodeRules(param.getIds());
        return R.ok(SysPromptEnum.ENCODE_RULE_DELETE_SUCCESS);
    }

    /**
     * 根据规则代码生成编码
     * <p>
     * 根据指定的规则代码生成唯一的业务编码，支持序列号原子递增。
     * </p>
     * <p>编码格式：前缀 + 日期格式 + 序列号</p>
     * <p>示例：SO202604100001</p>
     *
     * @param param 规则代码参数
     * @return 生成的编码
     */
    @PostMapping("/generate")
    public R<String> generate(@RequestBody GenerateCodeParam param) {
        String code = encodeRuleService.generateCode(param.getRuleCode());
        return R.ok(SysPromptEnum.ENCODE_RULE_GENERATE_SUCCESS, code);
    }

    /**
     * 启用编码规则
     * <p>
     * 将指定编码规则的状态设置为启用。
     * </p>
     *
     * @param param 启用参数
     * @return 启用结果
     */
    @PostMapping("/enable")
    public R<Void> enable(@RequestBody IdParam param) {
        encodeRuleService.enableEncodeRule(param.getId());
        return R.ok(SysPromptEnum.ENCODE_RULE_ENABLE_SUCCESS);
    }

    /**
     * 禁用编码规则
     * <p>
     * 将指定编码规则的状态设置为禁用。
     * </p>
     *
     * @param param 禁用参数
     * @return 禁用结果
     */
    @PostMapping("/disable")
    public R<Void> disable(@RequestBody IdParam param) {
        encodeRuleService.disableEncodeRule(param.getId());
        return R.ok(SysPromptEnum.ENCODE_RULE_DISABLE_SUCCESS);
    }

    /**
     * 生成编码参数类
     */
    @lombok.Data
    public static class GenerateCodeParam {
        /**
         * 规则代码（必填）
         */
        private String ruleCode;
    }
}
