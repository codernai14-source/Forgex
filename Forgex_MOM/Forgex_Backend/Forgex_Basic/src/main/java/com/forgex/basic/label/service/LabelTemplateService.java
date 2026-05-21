package com.forgex.basic.label.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.basic.label.domain.dto.LabelTemplateDTO;
import com.forgex.basic.label.domain.entity.LabelTemplate;
import com.forgex.basic.label.domain.param.LabelPrintRenderParam;
import com.forgex.basic.label.domain.param.LabelTemplateDesignSaveParam;
import com.forgex.basic.label.domain.param.LabelTemplateQueryParam;
import com.forgex.basic.label.domain.param.LabelTemplateSaveParam;
import com.forgex.basic.label.domain.param.LabelTemplateUpdateParam;
import com.forgex.basic.label.domain.vo.LabelRenderVO;
import com.forgex.basic.label.domain.vo.LabelTemplateDesignVO;
import com.forgex.basic.label.domain.vo.TemplateVO;

import java.util.List;

public interface LabelTemplateService extends IService<LabelTemplate> {

    IPage<TemplateVO> pageTemplates(LabelTemplateQueryParam param, Long tenantId);

    LabelTemplateDTO getTemplateById(Long id, Long tenantId);

    Long addTemplate(LabelTemplateSaveParam param, Long tenantId);

    void updateTemplate(LabelTemplateUpdateParam param, Long tenantId);

    void deleteTemplate(Long id, Long tenantId);

    void batchDeleteTemplates(List<Long> ids, Long tenantId);

    void setDefaultTemplate(Long id, String templateType, Long tenantId);

    boolean existsByCode(String templateCode, Long tenantId);

    boolean existsByCodeExcludeId(String templateCode, Long excludeId, Long tenantId);

    LabelTemplate getDefaultTemplate(String templateType, Long tenantId);

    LabelTemplateDesignVO getDesignDetail(Long id, Long tenantId);

    void saveDesign(LabelTemplateDesignSaveParam param, Long tenantId);

    LabelTemplateDesignVO preview(Long id, Long tenantId);

    LabelRenderVO render(LabelPrintRenderParam param, Long tenantId);
}
