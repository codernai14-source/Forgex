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
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
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
    
    private final SysMessageTemplateMapper templateMapper;
    private final SysMessageTemplateReceiverMapper receiverMapper;
    private final SysMessageTemplateContentMapper contentMapper;
    private final ObjectMapper objectMapper;
    
    @Override
    public Page<SysMessageTemplateVO> page(SysMessageTemplateParam param) {
        // 构建查询条件
        LambdaQueryWrapper<SysMessageTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(param.getTemplateCode()), 
                    SysMessageTemplate::getTemplateCode, param.getTemplateCode())
               .like(StringUtils.hasText(param.getTemplateName()), 
                    SysMessageTemplate::getTemplateName, param.getTemplateName())
               .eq(StringUtils.hasText(param.getMessageType()), 
                   SysMessageTemplate::getMessageType, param.getMessageType())
               .eq(param.getStatus() != null, 
                   SysMessageTemplate::getStatus, param.getStatus())
               .orderByDesc(SysMessageTemplate::getCreateTime);
        
        // 分页查询
        Page<SysMessageTemplate> page = new Page<>(param.getPageNum(), param.getPageSize());
        Page<SysMessageTemplate> result = templateMapper.selectPage(page, wrapper);
        
        // 转换为VO
        Page<SysMessageTemplateVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<SysMessageTemplateVO> voList = result.getRecords().stream()
                .map(this::entityToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        
        return voPage;
    }
    
    @Override
    public SysMessageTemplateVO getById(Long id) {
        SysMessageTemplate template = templateMapper.selectById(id);
        if (template == null) {
            throw new BusinessException("消息模板不存在");
        }
        
        SysMessageTemplateVO vo = entityToVO(template);
        
        // 查询接收人配置
        List<SysMessageTemplateReceiver> receivers = receiverMapper.selectList(
            new LambdaQueryWrapper<SysMessageTemplateReceiver>()
                .eq(SysMessageTemplateReceiver::getTemplateId, id)
        );
        vo.setReceivers(receivers.stream().map(this::receiverToVO).collect(Collectors.toList()));
        
        // 查询模板内容配置
        List<SysMessageTemplateContent> contents = contentMapper.selectList(
            new LambdaQueryWrapper<SysMessageTemplateContent>()
                .eq(SysMessageTemplateContent::getTemplateId, id)
        );
        vo.setContents(contents.stream().map(this::contentToVO).collect(Collectors.toList()));
        
        return vo;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long save(SysMessageTemplateSaveDTO dto) {
        // 检查模板编号是否重复
        LambdaQueryWrapper<SysMessageTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMessageTemplate::getTemplateCode, dto.getTemplateCode());
        if (dto.getId() != null) {
            wrapper.ne(SysMessageTemplate::getId, dto.getId());
        }
        Long count = templateMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("模板编号已存在");
        }
        
        // 保存主表
        SysMessageTemplate template = new SysMessageTemplate();
        template.setId(dto.getId());
        template.setTemplateCode(dto.getTemplateCode());
        template.setTemplateName(resolveDisplayText(dto.getTemplateName(), dto.getTemplateNameI18nJson()));
        template.setTemplateNameI18nJson(dto.getTemplateNameI18nJson());
        template.setTemplateVersion(dto.getTemplateVersion());
        template.setMessageType(dto.getMessageType());
        template.setStatus(dto.getStatus());
        template.setRemark(dto.getRemark());
        
        if (dto.getId() == null) {
            templateMapper.insert(template);
        } else {
            templateMapper.updateById(template);
            // 删除旧的接收人和内容配置
            receiverMapper.delete(new LambdaQueryWrapper<SysMessageTemplateReceiver>()
                .eq(SysMessageTemplateReceiver::getTemplateId, template.getId()));
            contentMapper.delete(new LambdaQueryWrapper<SysMessageTemplateContent>()
                .eq(SysMessageTemplateContent::getTemplateId, template.getId()));
        }
        
        // 保存接收人配置
        if (dto.getReceivers() != null && !dto.getReceivers().isEmpty()) {
            for (SysMessageTemplateSaveDTO.ReceiverConfig config : dto.getReceivers()) {
                SysMessageTemplateReceiver receiver = new SysMessageTemplateReceiver();
                receiver.setTemplateId(template.getId());
                receiver.setReceiverType(config.getReceiverType());
                try {
                    receiver.setReceiverIds(objectMapper.writeValueAsString(config.getReceiverIds()));
                } catch (Exception e) {
                    throw new BusinessException("接收人ID列表格式错误");
                }
                receiverMapper.insert(receiver);
            }
        }
        
        // 保存模板内容配置
        if (dto.getContents() != null && !dto.getContents().isEmpty()) {
            for (SysMessageTemplateSaveDTO.ContentConfig config : dto.getContents()) {
                SysMessageTemplateContent content = new SysMessageTemplateContent();
                content.setTemplateId(template.getId());
                content.setPlatform(config.getPlatform());
                content.setContentTitle(resolveDisplayText(config.getContentTitle(), config.getContentTitleI18nJson()));
                content.setContentTitleI18nJson(config.getContentTitleI18nJson());
                content.setContentBody(resolveDisplayText(config.getContentBody(), config.getContentBodyI18nJson()));
                content.setContentBodyI18nJson(config.getContentBodyI18nJson());
                content.setLinkUrl(config.getLinkUrl());
                contentMapper.insert(content);
            }
        }
        
        return template.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long id) {
        // 删除主表
        templateMapper.deleteById(id);
        // 删除接收人配置
        receiverMapper.delete(new LambdaQueryWrapper<SysMessageTemplateReceiver>()
            .eq(SysMessageTemplateReceiver::getTemplateId, id));
        // 删除内容配置
        contentMapper.delete(new LambdaQueryWrapper<SysMessageTemplateContent>()
            .eq(SysMessageTemplateContent::getTemplateId, id));
        return true;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        for (Long id : ids) {
            delete(id);
        }
        return true;
    }
    
    @Override
    public boolean existsByCode(String code) {
        LambdaQueryWrapper<SysMessageTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMessageTemplate::getTemplateCode, code);
        Long count = templateMapper.selectCount(wrapper);
        return count > 0;
    }
    
    @Override
    public boolean existsByCodeExcludeId(String code, Long id) {
        LambdaQueryWrapper<SysMessageTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMessageTemplate::getTemplateCode, code);
        wrapper.ne(SysMessageTemplate::getId, id);
        Long count = templateMapper.selectCount(wrapper);
        return count > 0;
    }
    
    /**
     * 实体转VO
     */
    private SysMessageTemplateVO entityToVO(SysMessageTemplate entity) {
        SysMessageTemplateVO vo = new SysMessageTemplateVO();
        vo.setId(entity.getId());
        vo.setTemplateCode(entity.getTemplateCode());
        vo.setTemplateName(entity.getTemplateName());
        vo.setTemplateNameI18nJson(entity.getTemplateNameI18nJson());
        vo.setTemplateVersion(entity.getTemplateVersion());
        vo.setMessageType(entity.getMessageType());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        vo.setCreateBy(entity.getCreateBy());
        vo.setUpdateTime(entity.getUpdateTime());
        vo.setUpdateBy(entity.getUpdateBy());
        return vo;
    }
    
    /**
     * 接收人实体转VO
     */
    private SysMessageTemplateVO.ReceiverVO receiverToVO(SysMessageTemplateReceiver entity) {
        SysMessageTemplateVO.ReceiverVO vo = new SysMessageTemplateVO.ReceiverVO();
        vo.setId(entity.getId());
        vo.setReceiverType(entity.getReceiverType());
        try {
            List<Long> ids = objectMapper.readValue(entity.getReceiverIds(), new TypeReference<List<Long>>() {});
            vo.setReceiverIds(ids);
        } catch (Exception e) {
            vo.setReceiverIds(new ArrayList<>());
        }
        return vo;
    }
    
    /**
     * 内容实体转VO
     */
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
            var node = objectMapper.readTree(i18nJson);
            if (node.hasNonNull("zh-CN") && StringUtils.hasText(node.get("zh-CN").asText())) {
                return node.get("zh-CN").asText().trim();
            }
            var fields = node.fields();
            while (fields.hasNext()) {
                var entry = fields.next();
                if (entry.getValue() == null) {
                    continue;
                }
                String value = entry.getValue().asText();
                if (StringUtils.hasText(value)) {
                    return value.trim();
                }
            }
        } catch (Exception ignored) {
            // Ignore invalid JSON and let the caller keep the plain-text fallback.
        }
        return plainText;
    }
}



