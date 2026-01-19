package com.forgex.sys.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.web.R;
import com.forgex.sys.domain.dto.SysOperationTemplateDTO;
import com.forgex.sys.domain.entity.SysOperationTemplate;
import com.forgex.sys.mapper.SysOperationTemplateMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 操作模板控制器
 * <p>提供操作模板的查询和保存功能。</p>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/sys/operationTemplate")
@RequiredArgsConstructor
public class SysOperationTemplateController {

    /**
     * 操作模板Mapper
     */
    private final SysOperationTemplateMapper templateMapper;

    /**
     * 查询操作模板列表
     * <p>根据查询条件查询操作模板列表。</p>
     * 
     * @param query 查询条件
     * @return 操作模板列表
     */
    @PostMapping("/list")
    public R<List<SysOperationTemplate>> list(@RequestBody(required = false) SysOperationTemplateDTO query) {
        // 获取当前租户ID
        Long tenantId = TenantContext.get();
        
        // 构建查询条件
        LambdaQueryWrapper<SysOperationTemplate> qw = new LambdaQueryWrapper<SysOperationTemplate>()
                .eq(tenantId != null, SysOperationTemplate::getTenantId, tenantId)
                .eq(query != null && StringUtils.hasText(query.getModule()), SysOperationTemplate::getModule, query.getModule())
                .eq(query != null && StringUtils.hasText(query.getOperationType()), SysOperationTemplate::getOperationType, query.getOperationType())
                .orderByDesc(SysOperationTemplate::getUpdateTime);
        
        return R.ok(templateMapper.selectList(qw));
    }

    /**
     * 保存操作模板
     * <p>新增或更新操作模板。</p>
     * 
     * @param dto 操作模板DTO
     * @return 操作模板ID，失败返回0
     */
    @PostMapping("/save")
    public R<Long> save(@RequestBody SysOperationTemplateDTO dto) {
        // 获取当前租户ID
        Long tenantId = TenantContext.get();
        if (tenantId == null) {
            return R.ok(0L);
        }
        
        // 参数校验
        if (dto == null || !StringUtils.hasText(dto.getModule()) || !StringUtils.hasText(dto.getOperationType()) || !StringUtils.hasText(dto.getTemplateCode())) {
            return R.ok(0L);
        }

        // 查询是否已存在
        SysOperationTemplate exist = templateMapper.selectOne(new LambdaQueryWrapper<SysOperationTemplate>()
                .eq(SysOperationTemplate::getTenantId, tenantId)
                .eq(SysOperationTemplate::getModule, dto.getModule())
                .eq(SysOperationTemplate::getOperationType, dto.getOperationType())
                .eq(SysOperationTemplate::getTemplateCode, dto.getTemplateCode())
                .last("limit 1"));
        
        if (exist == null) {
            // 新增操作模板
            SysOperationTemplate t = new SysOperationTemplate();
            t.setTenantId(tenantId);
            t.setModule(dto.getModule());
            t.setOperationType(dto.getOperationType());
            t.setTemplateCode(dto.getTemplateCode());
            t.setTextI18nJson(dto.getTextI18nJson());
            t.setPlaceholdersJson(dto.getPlaceholdersJson());
            t.setDeleted(false);
            templateMapper.insert(t);
            return R.ok(t.getId());
        }
        
        // 更新操作模板
        SysOperationTemplate u = new SysOperationTemplate();
        u.setId(exist.getId());
        u.setTextI18nJson(dto.getTextI18nJson());
        u.setPlaceholdersJson(dto.getPlaceholdersJson());
        templateMapper.updateById(u);
        return R.ok(exist.getId());
    }
}

