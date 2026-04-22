package com.forgex.basic.label.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.basic.label.domain.entity.LabelPrintRecord;
import com.forgex.basic.label.domain.param.PrintRecordQueryParam;
import com.forgex.basic.label.domain.vo.PrintRecordVO;

/**
 * 标签打印记录 Service 接口
 * <p>
 * 提供标签打印记录管理相关的业务操作，包括：
 * 1. 打印记录查询
 * 2. 打印记录详情
 * 3. 补打记录管理
 * 4. 统计分析
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
public interface LabelRecordService extends IService<LabelPrintRecord> {

    /**
     * 分页查询打印记录列表
     *
     * @param param 查询参数
     * @param tenantId 租户 ID
     * @return 打印记录分页数据
     */
    IPage<PrintRecordVO> pageRecords(PrintRecordQueryParam param, Long tenantId);

    /**
     * 查询打印记录详情
     *
     * @param id 记录 ID
     * @param tenantId 租户 ID
     * @return 打印记录 VO
     */
    PrintRecordVO getRecordDetail(Long id, Long tenantId);

    /**
     * 根据打印流水号查询记录
     *
     * @param printNo 打印流水号
     * @param tenantId 租户 ID
     * @return 打印记录，不存在则返回 null
     */
    LabelPrintRecord getByPrintNo(String printNo, Long tenantId);

    /**
     * 统计指定时间范围内的打印数量
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param tenantId 租户 ID
     * @return 打印数量
     */
    long countByTimeRange(String startTime, String endTime, Long tenantId);

    /**
     * 统计指定模板类型的打印数量
     *
     * @param templateType 模板类型
     * @param tenantId 租户 ID
     * @return 打印数量
     */
    long countByTemplateType(String templateType, Long tenantId);
}
