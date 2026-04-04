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
package com.forgex.common.service.template;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forgex.common.domain.entity.template.*;
import com.forgex.common.mapper.template.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 模板配置服务实现类
 * <p>
 * 提供模板配置的查询和同步功能，支持租户级别配置。
 * 查询优先级：租户配置 → 公共配置（tenant_id=0）
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateConfigServiceImpl implements TemplateConfigService {
    
    private final SysResponseMessageTemplateMapper responseMessageTemplateMapper;
    private final SysMqMessageTemplateMapper mqMessageTemplateMapper;
    private final FxExcelImportConfigTemplateMapper importConfigTemplateMapper;
    private final FxExcelExportConfigTemplateMapper exportConfigTemplateMapper;
    
    @Override
    public String getTemplateContent(Long tenantId, String templateCode, Map<String, Object> params) {
        // 先查询租户级别的模板
        SysResponseMessageTemplate template = queryResponseMessageTemplate(tenantId, templateCode);
        
        // 如果租户级别不存在，查询公共配置
        if (template == null && !tenantId.equals(0L)) {
            template = queryResponseMessageTemplate(0L, templateCode);
        }
        
        if (template != null && template.getEnabled()) {
            // 渲染模板
            return renderTemplate(template.getTemplateContent(), params);
        }
        
        // 如果没有找到模板，返回默认内容
        log.warn("未找到模板配置，tenantId: {}, templateCode: {}", tenantId, templateCode);
        return templateCode; // 返回模板编码作为默认值
    }
    
    /**
     * 查询接口返回消息模板
     *
     * @param tenantId 租户 ID
     * @param templateCode 模板编码
     * @return 接口返回消息模板
     */
    private SysResponseMessageTemplate queryResponseMessageTemplate(Long tenantId, String templateCode) {
        LambdaQueryWrapper<SysResponseMessageTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysResponseMessageTemplate::getTenantId, tenantId)
               .eq(SysResponseMessageTemplate::getTemplateCode, templateCode)
               .eq(SysResponseMessageTemplate::getDeleted, false)
               .last("limit 1");
        
        return responseMessageTemplateMapper.selectOne(wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncTemplatesToTenant(Long sourceTenantId, Long targetTenantId, String... templateCodes) {
        log.info("开始同步模板配置，源租户 ID：{}，目标租户 ID：{}，模板数量：{}", 
                sourceTenantId, targetTenantId, templateCodes != null ? templateCodes.length : "全部");
        
        // 1. 同步接口返回消息模板
        syncResponseMessageTemplates(sourceTenantId, targetTenantId, templateCodes);
        
        // 2. 同步 MQ 消息模板
        syncMqMessageTemplates(sourceTenantId, targetTenantId, templateCodes);
        
        // 3. 同步导入配置模板
        syncImportConfigTemplates(sourceTenantId, targetTenantId, templateCodes);
        
        // 4. 同步导出配置模板
        syncExportConfigTemplates(sourceTenantId, targetTenantId, templateCodes);
        
        log.info("模板配置同步完成，目标租户 ID：{}", targetTenantId);
    }
    
    /**
     * 同步接口返回消息模板
     */
    private void syncResponseMessageTemplates(Long sourceTenantId, Long targetTenantId, String... templateCodes) {
        LambdaQueryWrapper<SysResponseMessageTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysResponseMessageTemplate::getTenantId, sourceTenantId)
               .eq(SysResponseMessageTemplate::getDeleted, false)
               .eq(SysResponseMessageTemplate::getEnabled, true);
        
        if (templateCodes != null && templateCodes.length > 0) {
            wrapper.in(SysResponseMessageTemplate::getTemplateCode, templateCodes);
        }
        
        List<SysResponseMessageTemplate> sourceTemplates = responseMessageTemplateMapper.selectList(wrapper);
        
        for (SysResponseMessageTemplate source : sourceTemplates) {
            // 检查目标租户是否已存在
            LambdaQueryWrapper<SysResponseMessageTemplate> targetWrapper = new LambdaQueryWrapper<>();
            targetWrapper.eq(SysResponseMessageTemplate::getTenantId, targetTenantId)
                        .eq(SysResponseMessageTemplate::getTemplateCode, source.getTemplateCode())
                        .eq(SysResponseMessageTemplate::getLang, source.getLang())
                        .eq(SysResponseMessageTemplate::getDeleted, false);
            
            SysResponseMessageTemplate existing = responseMessageTemplateMapper.selectOne(targetWrapper);
            
            if (existing != null) {
                // 更新现有模板
                existing.setTemplateContent(source.getTemplateContent());
                existing.setTemplateType(source.getTemplateType());
                existing.setParams(source.getParams());
                responseMessageTemplateMapper.updateById(existing);
            } else {
                // 创建新模板
                SysResponseMessageTemplate newTemplate = new SysResponseMessageTemplate();
                BeanUtils.copyProperties(source, newTemplate);
                newTemplate.setId(null);
                newTemplate.setTenantId(targetTenantId);
                responseMessageTemplateMapper.insert(newTemplate);
            }
        }
        
        log.info("同步接口返回消息模板完成，数量：{}", sourceTemplates.size());
    }
    
    /**
     * 同步 MQ 消息模板
     */
    private void syncMqMessageTemplates(Long sourceTenantId, Long targetTenantId, String... templateCodes) {
        LambdaQueryWrapper<SysMqMessageTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMqMessageTemplate::getTenantId, sourceTenantId)
               .eq(SysMqMessageTemplate::getDeleted, false)
               .eq(SysMqMessageTemplate::getEnabled, true);
        
        if (templateCodes != null && templateCodes.length > 0) {
            wrapper.in(SysMqMessageTemplate::getTemplateCode, templateCodes);
        }
        
        List<SysMqMessageTemplate> sourceTemplates = mqMessageTemplateMapper.selectList(wrapper);
        
        for (SysMqMessageTemplate source : sourceTemplates) {
            LambdaQueryWrapper<SysMqMessageTemplate> targetWrapper = new LambdaQueryWrapper<>();
            targetWrapper.eq(SysMqMessageTemplate::getTenantId, targetTenantId)
                        .eq(SysMqMessageTemplate::getTemplateCode, source.getTemplateCode())
                        .eq(SysMqMessageTemplate::getDeleted, false);
            
            SysMqMessageTemplate existing = mqMessageTemplateMapper.selectOne(targetWrapper);
            
            if (existing != null) {
                existing.setMessageTitle(source.getMessageTitle());
                existing.setMessageContent(source.getMessageContent());
                existing.setMessageType(source.getMessageType());
                existing.setSendChannel(source.getSendChannel());
                existing.setParams(source.getParams());
                mqMessageTemplateMapper.updateById(existing);
            } else {
                SysMqMessageTemplate newTemplate = new SysMqMessageTemplate();
                BeanUtils.copyProperties(source, newTemplate);
                newTemplate.setId(null);
                newTemplate.setTenantId(targetTenantId);
                mqMessageTemplateMapper.insert(newTemplate);
            }
        }
        
        log.info("同步 MQ 消息模板完成，数量：{}", sourceTemplates.size());
    }
    
    /**
     * 同步导入配置模板
     */
    private void syncImportConfigTemplates(Long sourceTenantId, Long targetTenantId, String... templateCodes) {
        LambdaQueryWrapper<FxExcelImportConfigTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FxExcelImportConfigTemplate::getTenantId, sourceTenantId)
               .eq(FxExcelImportConfigTemplate::getDeleted, false)
               .eq(FxExcelImportConfigTemplate::getEnabled, true);
        
        if (templateCodes != null && templateCodes.length > 0) {
            wrapper.in(FxExcelImportConfigTemplate::getTemplateCode, templateCodes);
        }
        
        List<FxExcelImportConfigTemplate> sourceTemplates = importConfigTemplateMapper.selectList(wrapper);
        
        for (FxExcelImportConfigTemplate source : sourceTemplates) {
            LambdaQueryWrapper<FxExcelImportConfigTemplate> targetWrapper = new LambdaQueryWrapper<>();
            targetWrapper.eq(FxExcelImportConfigTemplate::getTenantId, targetTenantId)
                        .eq(FxExcelImportConfigTemplate::getTemplateCode, source.getTemplateCode())
                        .eq(FxExcelImportConfigTemplate::getDeleted, false);
            
            FxExcelImportConfigTemplate existing = importConfigTemplateMapper.selectOne(targetWrapper);
            
            if (existing != null) {
                existing.setBizType(source.getBizType());
                existing.setConfigData(source.getConfigData());
                importConfigTemplateMapper.updateById(existing);
            } else {
                FxExcelImportConfigTemplate newTemplate = new FxExcelImportConfigTemplate();
                BeanUtils.copyProperties(source, newTemplate);
                newTemplate.setId(null);
                newTemplate.setTenantId(targetTenantId);
                importConfigTemplateMapper.insert(newTemplate);
            }
        }
        
        log.info("同步导入配置模板完成，数量：{}", sourceTemplates.size());
    }
    
    /**
     * 同步导出配置模板
     */
    private void syncExportConfigTemplates(Long sourceTenantId, Long targetTenantId, String... templateCodes) {
        LambdaQueryWrapper<FxExcelExportConfigTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FxExcelExportConfigTemplate::getTenantId, sourceTenantId)
               .eq(FxExcelExportConfigTemplate::getDeleted, false)
               .eq(FxExcelExportConfigTemplate::getEnabled, true);
        
        if (templateCodes != null && templateCodes.length > 0) {
            wrapper.in(FxExcelExportConfigTemplate::getTemplateCode, templateCodes);
        }
        
        List<FxExcelExportConfigTemplate> sourceTemplates = exportConfigTemplateMapper.selectList(wrapper);
        
        for (FxExcelExportConfigTemplate source : sourceTemplates) {
            LambdaQueryWrapper<FxExcelExportConfigTemplate> targetWrapper = new LambdaQueryWrapper<>();
            targetWrapper.eq(FxExcelExportConfigTemplate::getTenantId, targetTenantId)
                        .eq(FxExcelExportConfigTemplate::getTemplateCode, source.getTemplateCode())
                        .eq(FxExcelExportConfigTemplate::getDeleted, false);
            
            FxExcelExportConfigTemplate existing = exportConfigTemplateMapper.selectOne(targetWrapper);
            
            if (existing != null) {
                existing.setBizType(source.getBizType());
                existing.setConfigData(source.getConfigData());
                exportConfigTemplateMapper.updateById(existing);
            } else {
                FxExcelExportConfigTemplate newTemplate = new FxExcelExportConfigTemplate();
                BeanUtils.copyProperties(source, newTemplate);
                newTemplate.setId(null);
                newTemplate.setTenantId(targetTenantId);
                exportConfigTemplateMapper.insert(newTemplate);
            }
        }
        
        log.info("同步导出配置模板完成，数量：{}", sourceTemplates.size());
    }
    
    /**
     * 渲染模板内容
     * <p>
     * 替换模板中的参数占位符。
     * </p>
     *
     * @param templateContent 模板内容
     * @param params 参数
     * @return 渲染后的内容
     */
    private String renderTemplate(String templateContent, Map<String, Object> params) {
        if (StrUtil.isEmpty(templateContent) || params == null || params.isEmpty()) {
            return templateContent;
        }
        
        Matcher matcher = PARAM_PATTERN.matcher(templateContent);
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()) {
            String paramName = matcher.group(1);
            Object value = params.get(paramName);
            String replacement = value != null ? value.toString() : "";
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        
        return sb.toString();
    }
    
    /**
     * 参数占位符正则表达式：{paramName}
     */
    private static final Pattern PARAM_PATTERN = Pattern.compile("\\{([^}]+)}");
}
