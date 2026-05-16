package com.forgex.basic.label.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.basic.label.domain.entity.LabelField;
import com.forgex.basic.label.domain.param.LabelFieldQueryParam;
import com.forgex.basic.label.domain.param.LabelFieldSaveParam;
import com.forgex.basic.label.domain.param.LabelFieldUpdateParam;
import com.forgex.basic.label.domain.vo.LabelFieldVO;
import com.forgex.common.domain.dto.excel.FxExcelImportExecuteParam;
import com.forgex.common.domain.dto.excel.FxExcelImportResultDTO;
import com.forgex.common.domain.dto.excel.TemplateOption;

import java.util.List;

public interface LabelFieldService extends IService<LabelField> {

    IPage<LabelFieldVO> pageFields(LabelFieldQueryParam param, Long tenantId);

    LabelFieldVO getById(Long id, Long tenantId);

    Long addField(LabelFieldSaveParam param, Long tenantId);

    void updateField(LabelFieldUpdateParam param, Long tenantId);

    void deleteField(Long id, Long tenantId);

    boolean existsByCode(String fieldCode, Long moduleId, Long tenantId);

    boolean existsByCodeExcludeId(String fieldCode, Long moduleId, Long excludeId, Long tenantId);

    List<TemplateOption> options(Long tenantId);

    FxExcelImportResultDTO executeCommonImport(FxExcelImportExecuteParam param);
}
