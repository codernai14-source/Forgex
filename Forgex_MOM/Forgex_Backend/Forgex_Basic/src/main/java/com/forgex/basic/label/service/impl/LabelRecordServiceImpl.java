package com.forgex.basic.label.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.basic.label.domain.entity.LabelPrintRecord;
import com.forgex.basic.label.domain.entity.LabelTemplate;
import com.forgex.basic.label.mapper.LabelPrintRecordMapper;
import com.forgex.basic.label.service.LabelRecordService;
import com.forgex.basic.label.service.LabelTemplateService;
import com.forgex.basic.label.domain.param.PrintRecordQueryParam;
import com.forgex.basic.label.domain.vo.PrintRecordVO;
import com.forgex.basic.enums.BasicPromptEnum;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.web.StatusCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 标签打印记录 Service 实现类
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
 * @see LabelRecordService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LabelRecordServiceImpl extends ServiceImpl<LabelPrintRecordMapper, LabelPrintRecord> implements LabelRecordService {

    private final LabelPrintRecordMapper labelPrintRecordMapper;
    private final LabelTemplateService labelTemplateService;

    /**
     * 分页查询文件记录。
     *
     * @param param 查询参数
     * @param tenantId 租户 ID
     * @return 打印记录分页数据
     */
    @Override
    public IPage<PrintRecordVO> pageRecords(PrintRecordQueryParam param, Long tenantId) {
        Page<LabelPrintRecord> page = new Page<>(param.getPageNum(), param.getPageSize());

        LambdaQueryWrapper<LabelPrintRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LabelPrintRecord::getTenantId, tenantId)
                .eq(LabelPrintRecord::getDeleted, false);

        if (StringUtils.hasText(param.getPrintNo())) {
            wrapper.like(LabelPrintRecord::getPrintNo, param.getPrintNo());
        }
        if (StringUtils.hasText(param.getTemplateType())) {
            wrapper.eq(LabelPrintRecord::getTemplateType, param.getTemplateType());
        }
        if (StringUtils.hasText(param.getBarcodeNo())) {
            wrapper.like(LabelPrintRecord::getBarcodeNo, param.getBarcodeNo());
        }
        if (StringUtils.hasText(param.getEngineeringCardNo())) {
            wrapper.like(LabelPrintRecord::getEngineeringCardNo, param.getEngineeringCardNo());
        }
        if (StringUtils.hasText(param.getLotNo())) {
            wrapper.like(LabelPrintRecord::getLotNo, param.getLotNo());
        }
        if (StringUtils.hasText(param.getMaterialCode())) {
            wrapper.like(LabelPrintRecord::getMaterialCode, param.getMaterialCode());
        }
        if (param.getOperatorId() != null) {
            wrapper.eq(LabelPrintRecord::getOperatorId, param.getOperatorId());
        }
        if (param.getFactoryId() != null) {
            wrapper.eq(LabelPrintRecord::getFactoryId, param.getFactoryId());
        }

        // 时间范围查询
        if (StringUtils.hasText(param.getPrintTimeStart())) {
            LocalDateTime startTime = LocalDateTime.parse(param.getPrintTimeStart(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            wrapper.ge(LabelPrintRecord::getPrintTime, startTime);
        }
        if (StringUtils.hasText(param.getPrintTimeEnd())) {
            LocalDateTime endTime = LocalDateTime.parse(param.getPrintTimeEnd(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            wrapper.le(LabelPrintRecord::getPrintTime, endTime);
        }

        wrapper.orderByDesc(LabelPrintRecord::getPrintTime);

        IPage<LabelPrintRecord> entityPage = labelPrintRecordMapper.selectPage(page, wrapper);

        // 转换为 VO
        IPage<PrintRecordVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        List<PrintRecordVO> voList = entityPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    /**
     * 查询打印记录详情
     *
     * @param id 记录 ID
     * @param tenantId 租户 ID
     * @return 打印记录 VO
     */
    @Override
    public PrintRecordVO getRecordDetail(Long id, Long tenantId) {
        LabelPrintRecord record = labelPrintRecordMapper.selectById(id);
        if (record == null || !record.getTenantId().equals(tenantId)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, BasicPromptEnum.LABEL_PRINT_RECORD_NOT_FOUND);
        }

        return convertToVO(record);
    }

    /**
     * 根据打印流水号查询记录
     *
     * @param printNo 打印流水号
     * @param tenantId 租户 ID
     * @return 打印记录，不存在则返回 null
     */
    @Override
    public LabelPrintRecord getByPrintNo(String printNo, Long tenantId) {
        LambdaQueryWrapper<LabelPrintRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LabelPrintRecord::getPrintNo, printNo)
                .eq(LabelPrintRecord::getTenantId, tenantId)
                .eq(LabelPrintRecord::getDeleted, false)
                .last("LIMIT 1");
        return labelPrintRecordMapper.selectOne(wrapper);
    }

    /**
     * 统计指定时间范围内的打印数量
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param tenantId 租户 ID
     * @return 打印数量
     */
    @Override
    public long countByTimeRange(String startTime, String endTime, Long tenantId) {
        LambdaQueryWrapper<LabelPrintRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LabelPrintRecord::getTenantId, tenantId)
                .eq(LabelPrintRecord::getDeleted, false);

        if (StringUtils.hasText(startTime)) {
            LocalDateTime start = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            wrapper.ge(LabelPrintRecord::getPrintTime, start);
        }
        if (StringUtils.hasText(endTime)) {
            LocalDateTime end = LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            wrapper.le(LabelPrintRecord::getPrintTime, end);
        }

        return labelPrintRecordMapper.selectCount(wrapper);
    }

    /**
     * 统计指定模板类型的打印数量
     *
     * @param templateType 模板类型
     * @param tenantId 租户 ID
     * @return 打印数量
     */
    @Override
    public long countByTemplateType(String templateType, Long tenantId) {
        LambdaQueryWrapper<LabelPrintRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LabelPrintRecord::getTemplateType, templateType)
                .eq(LabelPrintRecord::getTenantId, tenantId)
                .eq(LabelPrintRecord::getDeleted, false);
        return labelPrintRecordMapper.selectCount(wrapper);
    }

    /**
     * 转换为 VO
     *
     * @param entity 实体对象
     * @return VO 对象
     */
    private PrintRecordVO convertToVO(LabelPrintRecord entity) {
        PrintRecordVO vo = new PrintRecordVO();
        BeanUtils.copyProperties(entity, vo);

        // 查询模板名称
        if (entity.getTemplateId() != null) {
            LabelTemplate template = labelTemplateService.getById(entity.getTemplateId());
            if (template != null) {
                vo.setTemplateName(template.getTemplateName());
            }
        }

        // 设置打印类型名称
        if ("REPRINT".equals(entity.getPrintType())) {
            vo.setPrintTypeName("补打");
        } else {
            vo.setPrintTypeName("正常打印");
        }

        // 设置工厂名称（如果需要）
        // if (entity.getFactoryId() != null) {
        //     // 查询工厂名称
        // }

        return vo;
    }
}
