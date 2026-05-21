package com.forgex.basic.label.service;

import com.forgex.basic.label.domain.param.LabelPrintExecuteParam;
import com.forgex.basic.label.domain.param.LabelPrintRenderParam;
import com.forgex.basic.label.domain.vo.LabelRenderVO;

import java.util.List;

public interface LabelPrintService {

    List<String> executePrint(LabelPrintExecuteParam param, Long userId, Long tenantId);

    List<String> previewPrint(LabelPrintExecuteParam param, Long tenantId);

    List<String> reprintLabel(Long recordId, Integer reprintCount, Long userId, Long tenantId);

    LabelRenderVO render(LabelPrintRenderParam param, Long tenantId);
}
