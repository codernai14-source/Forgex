package com.forgex.basic.label.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.basic.label.domain.entity.LabelType;
import com.forgex.basic.label.domain.param.LabelTypeQueryParam;
import com.forgex.basic.label.domain.param.LabelTypeSaveParam;
import com.forgex.basic.label.domain.param.LabelTypeUpdateParam;
import com.forgex.basic.label.domain.vo.LabelTypeVO;

import java.util.List;

public interface LabelTypeService extends IService<LabelType> {

    IPage<LabelTypeVO> pageTypes(LabelTypeQueryParam param, Long tenantId);

    LabelTypeVO getById(Long id, Long tenantId);

    Long addType(LabelTypeSaveParam param, Long tenantId);

    void updateType(LabelTypeUpdateParam param, Long tenantId);

    void deleteType(Long id, Long tenantId);

    void batchDeleteTypes(List<Long> ids, Long tenantId);

    boolean existsByCode(String typeCode, Long tenantId);

    boolean existsByCodeExcludeId(String typeCode, Long excludeId, Long tenantId);

    List<LabelTypeVO> listEnabled(Long tenantId);
}
