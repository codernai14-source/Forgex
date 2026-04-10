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
package com.forgex.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.common.exception.BusinessException;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.tenant.TenantContextIgnore;
import com.forgex.common.util.CurrentUserUtils;
import com.forgex.sys.domain.dto.SysMessageTemplateSaveDTO;
import com.forgex.sys.domain.entity.SysMessageTemplate;
import com.forgex.sys.domain.entity.SysMessageTemplateContent;
import com.forgex.sys.domain.entity.SysMessageTemplateReceiver;
import com.forgex.sys.domain.param.SysMessageTemplateParam;
import com.forgex.sys.domain.vo.SysMessageTemplateVO;
import com.forgex.sys.mapper.SysMessageTemplateContentMapper;
import com.forgex.sys.mapper.SysMessageTemplateMapper;
import com.forgex.sys.mapper.SysMessageTemplateReceiverMapper;
import com.forgex.sys.service.SysMessageTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 消息模板服务实现
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
public class SysMessageTemplateServiceImpl implements SysMessageTemplateService {

    private static final Long PUBLIC_TENANT_ID = 0L;

    private final SysMessageTemplateMapper templateMapper;
    private final SysMessageTemplateReceiverMapper receiverMapper;
    private final SysMessageTemplateContentMapper contentMapper;
    private final ObjectMapper objectMapper;

    @Override
    public Page<SysMessageTemplateVO> page(SysMessageTemplateParam param) {
        SysMessageTemplateParam queryParam = param == null ? new SysMessageTemplateParam() : param;
        Long targetTenantId = resolveTargetTenantId(queryParam.getPublicConfig());
        return executeWithoutTenantIsolation(() -> {
            LambdaQueryWrapper<SysMessageTemplate> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysMessageTemplate::getTenantId, targetTenantId)
                    .like(StringUtils.hasText(queryParam.getTemplateCode()),
                            SysMessageTemplate::getTemplateCode, queryParam.getTemplateCode())
                    .like(StringUtils.hasText(queryParam.getTemplateName()),
                            SysMessageTemplate::getTemplateName, queryParam.getTemplateName())
                    .eq(StringUtils.hasText(queryParam.getMessageType()),
                            SysMessageTemplate::getMessageType, queryParam.getMessageType())
                    .eq(StringUtils.hasText(queryParam.getBizType()),
                            SysMessageTemplate::getBizType, normalizeOptionalText(queryParam.getBizType()))
                    .eq(queryParam.getStatus() != null,
                            SysMessageTemplate::getStatus, queryParam.getStatus())
                    .orderByDesc(SysMessageTemplate::getCreateTime);

            Page<SysMessageTemplate> page = new Page<>(
                    queryParam.getPageNum() == null ? 1 : queryParam.getPageNum(),
                    queryParam.getPageSize() == null ? 10 : queryParam.getPageSize()
            );
            Page<SysMessageTemplate> result = templateMapper.selectPage(page, wrapper);

            Page<SysMessageTemplateVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
            List<SysMessageTemplateVO> voList = result.getRecords().stream()
                    .map(this::entityToVO)
                    .collect(Collectors.toList());
            voPage.setRecords(voList);
            return voPage;
        });
    }

    @Override
    public SysMessageTemplateVO getById(Long id, Boolean publicConfig) {
        Long targetTenantId = resolveTargetTenantId(publicConfig);
        return executeWithoutTenantIsolation(() -> {
            SysMessageTemplate template = getTemplateByIdAndTenant(id, targetTenantId);
            if (template == null) {
                throw new BusinessException("消息模板不存在");
            }

            SysMessageTemplateVO vo = entityToVO(template);
            List<SysMessageTemplateReceiver> receivers = listReceivers(template.getId(), targetTenantId);
            List<SysMessageTemplateContent> contents = listContents(template.getId(), targetTenantId);

            vo.setReceivers(receivers.stream().map(this::receiverToVO).collect(Collectors.toList()));
            vo.setContents(contents.stream().map(this::contentToVO).collect(Collectors.toList()));
            return vo;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long save(SysMessageTemplateSaveDTO dto) {
        if (dto == null) {
            throw new BusinessException("请求参数不能为空");
        }
        Long targetTenantId = resolveTargetTenantId(dto.getPublicConfig());

        return executeWithoutTenantIsolation(() -> {
            String templateCode = normalizeRequiredText(dto.getTemplateCode(), "模板编号不能为空");

            if (dto.getId() == null) {
                if (existsByCodeInTenant(templateCode, targetTenantId, null)) {
                    throw new BusinessException("模板编号已存在");
                }
            } else if (existsByCodeInTenant(templateCode, targetTenantId, dto.getId())) {
                throw new BusinessException("模板编号已被其他模板使用");
            }

            SysMessageTemplate template;
            if (dto.getId() == null) {
                template = new SysMessageTemplate();
            } else {
                template = getTemplateByIdAndTenant(dto.getId(), targetTenantId);
                if (template == null) {
                    throw new BusinessException("消息模板不存在或不在当前配置范围");
                }
            }

            template.setTemplateCode(templateCode);
            template.setTemplateName(resolveDisplayText(dto.getTemplateName(), dto.getTemplateNameI18nJson()));
            template.setTemplateNameI18nJson(dto.getTemplateNameI18nJson());
            template.setTemplateVersion(dto.getTemplateVersion());
            template.setMessageType(dto.getMessageType());
            template.setBizType(normalizeOptionalText(dto.getBizType()));
            template.setStatus(dto.getStatus());
            template.setRemark(dto.getRemark());
            template.setTenantId(targetTenantId);

            if (dto.getId() == null) {
                templateMapper.insert(template);
            } else {
                templateMapper.updateById(template);
                deleteTemplateDetails(template.getId(), targetTenantId);
            }

            saveReceivers(template.getId(), targetTenantId, dto.getReceivers());
            saveContents(template.getId(), targetTenantId, dto.getContents());

            return template.getId();
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long id, Boolean publicConfig) {
        Long targetTenantId = resolveTargetTenantId(publicConfig);
        return executeWithoutTenantIsolation(() -> {
            SysMessageTemplate template = getTemplateByIdAndTenant(id, targetTenantId);
            if (template == null) {
                throw new BusinessException("消息模板不存在或不在当前配置范围");
            }
            templateMapper.deleteById(id);
            deleteTemplateDetails(id, targetTenantId);
            return true;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteBatch(List<Long> ids, Boolean publicConfig) {
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }
        for (Long id : ids) {
            delete(id, publicConfig);
        }
        return true;
    }

    @Override
    public boolean existsByCode(String code, Boolean publicConfig) {
        Long targetTenantId = resolveTargetTenantId(publicConfig);
        return executeWithoutTenantIsolation(() -> existsByCodeInTenant(code, targetTenantId, null));
    }

    @Override
    public boolean existsByCodeExcludeId(String code, Long id, Boolean publicConfig) {
        Long targetTenantId = resolveTargetTenantId(publicConfig);
        return executeWithoutTenantIsolation(() -> existsByCodeInTenant(code, targetTenantId, id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int pullPublicConfig() {
        Long targetTenantId = requireCurrentTenantId();
        if (PUBLIC_TENANT_ID.equals(targetTenantId)) {
            throw new BusinessException("公共租户无需执行拉取操作");
        }

        return executeWithoutTenantIsolation(() -> {
            List<SysMessageTemplate> publicTemplates = templateMapper.selectList(new LambdaQueryWrapper<SysMessageTemplate>()
                    .eq(SysMessageTemplate::getTenantId, PUBLIC_TENANT_ID)
                    .orderByAsc(SysMessageTemplate::getId));
            if (CollectionUtils.isEmpty(publicTemplates)) {
                return 0;
            }

            List<SysMessageTemplate> tenantTemplates = templateMapper.selectList(new LambdaQueryWrapper<SysMessageTemplate>()
                    .eq(SysMessageTemplate::getTenantId, targetTenantId));
            Map<String, SysMessageTemplate> tenantTemplateMap = new LinkedHashMap<>();
            for (SysMessageTemplate tenantTemplate : tenantTemplates) {
                tenantTemplateMap.put(tenantTemplate.getTemplateCode(), tenantTemplate);
            }

            int changedCount = 0;
            for (SysMessageTemplate publicTemplate : publicTemplates) {
                SysMessageTemplate targetTemplate = tenantTemplateMap.get(publicTemplate.getTemplateCode());

                if (targetTemplate == null) {
                    targetTemplate = copyTemplate(publicTemplate, targetTenantId);
                    targetTemplate.setId(null);
                    templateMapper.insert(targetTemplate);
                    tenantTemplateMap.put(targetTemplate.getTemplateCode(), targetTemplate);
                } else {
                    applyTemplateCoreFields(targetTemplate, publicTemplate, targetTenantId);
                    templateMapper.updateById(targetTemplate);
                    deleteTemplateDetails(targetTemplate.getId(), targetTenantId);
                }

                copyTemplateDetails(publicTemplate.getId(), targetTemplate.getId(), targetTenantId);
                changedCount++;
            }

            return changedCount;
        });
    }

    private void copyTemplateDetails(Long sourceTemplateId, Long targetTemplateId, Long targetTenantId) {
        List<SysMessageTemplateReceiver> publicReceivers = listReceivers(sourceTemplateId, PUBLIC_TENANT_ID);
        List<SysMessageTemplateContent> publicContents = listContents(sourceTemplateId, PUBLIC_TENANT_ID);

        for (SysMessageTemplateReceiver publicReceiver : publicReceivers) {
            SysMessageTemplateReceiver targetReceiver = new SysMessageTemplateReceiver();
            targetReceiver.setTemplateId(targetTemplateId);
            targetReceiver.setTenantId(targetTenantId);
            targetReceiver.setReceiverType(publicReceiver.getReceiverType());
            targetReceiver.setReceiverIds(publicReceiver.getReceiverIds());
            receiverMapper.insert(targetReceiver);
        }

        for (SysMessageTemplateContent publicContent : publicContents) {
            SysMessageTemplateContent targetContent = new SysMessageTemplateContent();
            targetContent.setTemplateId(targetTemplateId);
            targetContent.setTenantId(targetTenantId);
            targetContent.setPlatform(publicContent.getPlatform());
            targetContent.setContentTitle(publicContent.getContentTitle());
            targetContent.setContentTitleI18nJson(publicContent.getContentTitleI18nJson());
            targetContent.setContentBody(publicContent.getContentBody());
            targetContent.setContentBodyI18nJson(publicContent.getContentBodyI18nJson());
            targetContent.setLinkUrl(publicContent.getLinkUrl());
            contentMapper.insert(targetContent);
        }
    }

    private SysMessageTemplate copyTemplate(SysMessageTemplate source, Long tenantId) {
        SysMessageTemplate target = new SysMessageTemplate();
        applyTemplateCoreFields(target, source, tenantId);
        return target;
    }

    private void applyTemplateCoreFields(SysMessageTemplate target, SysMessageTemplate source, Long tenantId) {
        target.setTemplateCode(source.getTemplateCode());
        target.setTemplateName(source.getTemplateName());
        target.setTemplateNameI18nJson(source.getTemplateNameI18nJson());
        target.setTemplateVersion(source.getTemplateVersion());
        target.setMessageType(source.getMessageType());
        target.setBizType(source.getBizType());
        target.setStatus(source.getStatus());
        target.setRemark(source.getRemark());
        target.setTenantId(tenantId);
    }

    private void saveReceivers(Long templateId, Long tenantId, List<SysMessageTemplateSaveDTO.ReceiverConfig> receivers) {
        if (CollectionUtils.isEmpty(receivers)) {
            return;
        }

        for (SysMessageTemplateSaveDTO.ReceiverConfig config : receivers) {
            SysMessageTemplateReceiver receiver = new SysMessageTemplateReceiver();
            receiver.setTemplateId(templateId);
            receiver.setTenantId(tenantId);
            receiver.setReceiverType(config.getReceiverType());
            
            // 自定义类型不需要指定接收人 ID，由后端程序处理时指定
            if ("CUSTOM".equals(config.getReceiverType())) {
                receiver.setReceiverIds("[]");
            } else {
                try {
                    List<Long> receiverIds = config.getReceiverIds() == null ? new ArrayList<>() : config.getReceiverIds();
                    receiver.setReceiverIds(objectMapper.writeValueAsString(receiverIds));
                } catch (Exception e) {
                    throw new BusinessException("接收人 ID 列表格式错误");
                }
            }
            receiverMapper.insert(receiver);
        }
    }

    private void saveContents(Long templateId, Long tenantId, List<SysMessageTemplateSaveDTO.ContentConfig> contents) {
        if (CollectionUtils.isEmpty(contents)) {
            return;
        }

        for (SysMessageTemplateSaveDTO.ContentConfig config : contents) {
            SysMessageTemplateContent content = new SysMessageTemplateContent();
            content.setTemplateId(templateId);
            content.setTenantId(tenantId);
            content.setPlatform(config.getPlatform());
            content.setContentTitle(resolveDisplayText(config.getContentTitle(), config.getContentTitleI18nJson()));
            content.setContentTitleI18nJson(config.getContentTitleI18nJson());
            content.setContentBody(resolveDisplayText(config.getContentBody(), config.getContentBodyI18nJson()));
            content.setContentBodyI18nJson(config.getContentBodyI18nJson());
            content.setLinkUrl(config.getLinkUrl());
            contentMapper.insert(content);
        }
    }

    private void deleteTemplateDetails(Long templateId, Long tenantId) {
        receiverMapper.delete(new LambdaQueryWrapper<SysMessageTemplateReceiver>()
                .eq(SysMessageTemplateReceiver::getTemplateId, templateId)
                .eq(SysMessageTemplateReceiver::getTenantId, tenantId));
        contentMapper.delete(new LambdaQueryWrapper<SysMessageTemplateContent>()
                .eq(SysMessageTemplateContent::getTemplateId, templateId)
                .eq(SysMessageTemplateContent::getTenantId, tenantId));
    }

    private SysMessageTemplate getTemplateByIdAndTenant(Long id, Long tenantId) {
        if (id == null) {
            return null;
        }
        return templateMapper.selectOne(new LambdaQueryWrapper<SysMessageTemplate>()
                .eq(SysMessageTemplate::getId, id)
                .eq(SysMessageTemplate::getTenantId, tenantId)
                .last("LIMIT 1"));
    }

    private List<SysMessageTemplateReceiver> listReceivers(Long templateId, Long tenantId) {
        return receiverMapper.selectList(new LambdaQueryWrapper<SysMessageTemplateReceiver>()
                .eq(SysMessageTemplateReceiver::getTemplateId, templateId)
                .eq(SysMessageTemplateReceiver::getTenantId, tenantId));
    }

    private List<SysMessageTemplateContent> listContents(Long templateId, Long tenantId) {
        return contentMapper.selectList(new LambdaQueryWrapper<SysMessageTemplateContent>()
                .eq(SysMessageTemplateContent::getTemplateId, templateId)
                .eq(SysMessageTemplateContent::getTenantId, tenantId));
    }

    private boolean existsByCodeInTenant(String code, Long tenantId, Long excludeId) {
        if (!StringUtils.hasText(code)) {
            return false;
        }

        LambdaQueryWrapper<SysMessageTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMessageTemplate::getTemplateCode, code.trim())
                .eq(SysMessageTemplate::getTenantId, tenantId)
                .ne(excludeId != null, SysMessageTemplate::getId, excludeId);

        Long count = templateMapper.selectCount(wrapper);
        return count != null && count > 0;
    }

    private Long resolveTargetTenantId(Boolean publicConfig) {
        if (Boolean.TRUE.equals(publicConfig)) {
            return PUBLIC_TENANT_ID;
        }
        return requireCurrentTenantId();
    }

    private Long requireCurrentTenantId() {
        Long tenantId = CurrentUserUtils.getTenantId();
        if (tenantId == null) {
            tenantId = TenantContext.get();
        }
        if (tenantId == null) {
            throw new BusinessException("无法识别当前租户");
        }
        return tenantId;
    }

    private String normalizeRequiredText(String value, String errorMessage) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(errorMessage);
        }
        return value.trim();
    }

    private String normalizeOptionalText(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private SysMessageTemplateVO entityToVO(SysMessageTemplate entity) {
        SysMessageTemplateVO vo = new SysMessageTemplateVO();
        vo.setId(entity.getId());
        vo.setTemplateCode(entity.getTemplateCode());
        vo.setTemplateName(entity.getTemplateName());
        vo.setTemplateNameI18nJson(entity.getTemplateNameI18nJson());
        vo.setTemplateVersion(entity.getTemplateVersion());
        vo.setMessageType(entity.getMessageType());
        vo.setBizType(entity.getBizType());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        vo.setPublicConfig(PUBLIC_TENANT_ID.equals(entity.getTenantId()));
        vo.setCreateTime(entity.getCreateTime());
        vo.setCreateBy(entity.getCreateBy());
        vo.setUpdateTime(entity.getUpdateTime());
        vo.setUpdateBy(entity.getUpdateBy());
        return vo;
    }

    private SysMessageTemplateVO.ReceiverVO receiverToVO(SysMessageTemplateReceiver entity) {
        SysMessageTemplateVO.ReceiverVO vo = new SysMessageTemplateVO.ReceiverVO();
        vo.setId(entity.getId());
        vo.setReceiverType(entity.getReceiverType());
        try {
            List<Long> ids = objectMapper.readValue(entity.getReceiverIds(), new TypeReference<List<Long>>() {
            });
            vo.setReceiverIds(ids);
        } catch (Exception e) {
            vo.setReceiverIds(new ArrayList<>());
        }
        return vo;
    }

    private SysMessageTemplateVO.ContentVO contentToVO(SysMessageTemplateContent entity) {
        SysMessageTemplateVO.ContentVO vo = new SysMessageTemplateVO.ContentVO();
        vo.setId(entity.getId());
        vo.setPlatform(entity.getPlatform());
        vo.setContentTitle(entity.getContentTitle());
        vo.setContentTitleI18nJson(entity.getContentTitleI18nJson());
        vo.setContentBody(entity.getContentBody());
        vo.setContentBodyI18nJson(entity.getContentBodyI18nJson());
        vo.setLinkUrl(entity.getLinkUrl());
        return vo;
    }

    private String resolveDisplayText(String plainText, String i18nJson) {
        if (StringUtils.hasText(plainText)) {
            return plainText.trim();
        }
        if (!StringUtils.hasText(i18nJson)) {
            return plainText;
        }
        try {
            com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(i18nJson);
            if (node.hasNonNull("zh-CN") && StringUtils.hasText(node.get("zh-CN").asText())) {
                return node.get("zh-CN").asText().trim();
            }
            java.util.Iterator<Map.Entry<String, com.fasterxml.jackson.databind.JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, com.fasterxml.jackson.databind.JsonNode> entry = fields.next();
                if (entry.getValue() == null) {
                    continue;
                }
                String value = entry.getValue().asText();
                if (StringUtils.hasText(value)) {
                    return value.trim();
                }
            }
        } catch (Exception ignored) {
            // ignore invalid i18n json
        }
        return plainText;
    }

    private <T> T executeWithoutTenantIsolation(Supplier<T> supplier) {
        boolean oldIgnore = TenantContextIgnore.isIgnore();
        TenantContextIgnore.setIgnore(true);
        try {
            return supplier.get();
        } finally {
            if (oldIgnore) {
                TenantContextIgnore.setIgnore(true);
            } else {
                TenantContextIgnore.clear();
            }
        }
    }
}
