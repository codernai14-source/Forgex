package com.forgex.basic.label.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.basic.label.domain.dto.LabelBarcodeDTO;
import com.forgex.basic.label.domain.entity.LabelBarcode;
import com.forgex.basic.label.domain.param.BarcodeGenerateParam;
import com.forgex.basic.label.domain.param.BarcodeQueryParam;
import com.forgex.basic.label.domain.vo.BarcodeVO;
import com.forgex.basic.label.enums.PrintStatusEnum;
import com.forgex.basic.label.enums.TemplateTypeEnum;
import com.forgex.basic.label.mapper.LabelBarcodeMapper;
import com.forgex.basic.label.service.LabelBarcodeService;
import com.forgex.basic.label.utils.BarcodeGenerator;
import com.forgex.basic.enums.BasicPromptEnum;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.web.StatusCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 标签条码服务实现。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LabelBarcodeServiceImpl extends ServiceImpl<LabelBarcodeMapper, LabelBarcode> implements LabelBarcodeService {

    private final LabelBarcodeMapper labelBarcodeMapper;

    /**
     * 生成条码
     * <p>
     * 根据业务场景选择合适的生成策略：
     * - 来料标签：使用 LOT 号关联策略
     * - 工程卡标签：使用工程卡号关联策略
     * - 其他：使用业务规则策略或时间戳策略
     * </p>
     *
     * @param param 条码生成参数
     * @param userId 用户 ID
     * @param tenantId 租户 ID
     * @return 生成的条码信息
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public LabelBarcodeDTO generateBarcode(BarcodeGenerateParam param, Long userId, Long tenantId) {
        log.info("开始生成条码，业务场景: {}, 模板类型: {}", param.getBusinessScene(), param.getTemplateType());

        // 1. 【使用 BarcodeGenerator】根据业务场景选择生成策略
        String barcodeNo = generateBarcodeByStrategy(param);

        // 2. 确保唯一性（最多重试 10 次）
        int retry = 0;
        while (existsByBarcodeNo(barcodeNo, tenantId) && retry < 10) {
            log.warn("条码号重复，重新生成: {}", barcodeNo);
            barcodeNo = generateBarcodeByStrategy(param);
            retry++;
        }

        if (retry >= 10) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, BasicPromptEnum.BARCODE_GENERATE_FAILED);
        }

        // 3. 创建条码记录
        LabelBarcode barcode = new LabelBarcode();
        barcode.setBarcodeNo(barcodeNo);
        barcode.setTemplateType(param.getTemplateType());
        barcode.setMaterialId(param.getMaterialId());
        barcode.setMaterialCode(getMaterialCode(param.getMaterialId()));
        barcode.setMaterialName(getMaterialName(param.getMaterialId()));
        barcode.setLotNo(param.getLotNo());
        barcode.setEngineeringCardNo(param.getEngineeringCardNo());
        barcode.setBatchNo(param.getBatchNo());
        barcode.setBusinessScene(param.getBusinessScene());
        barcode.setFactoryId(param.getFactoryId());
        barcode.setRemark(param.getRemark());
        barcode.setTenantId(tenantId);
        barcode.setGenerateTime(LocalDateTime.now());
        barcode.setStatus(PrintStatusEnum.SUCCESS.getCode()); // 使用枚举：已生成
        barcode.setCreateBy(String.valueOf(userId));

        labelBarcodeMapper.insert(barcode);

        log.info("生成条码成功，条码号: {}", barcodeNo);
        return convertToDTO(barcode);
    }



    /**
     * 分页查询条码
     * <p>
     * 支持多条件组合查询，包括：
     * - 条码号模糊查询
     * - 模板类型精确匹配
     * - 物料 ID 精确匹配
     * - LOT 号精确匹配
     * - 业务场景精确匹配
     * - 工厂 ID 精确匹配
     * - 状态筛选
     * - 时间范围查询
     * </p>
     *
     * @param param 查询参数
     * @param tenantId 租户 ID
     * @return 分页结果
     */
    @Override
    public IPage<BarcodeVO> pageBarcodes(BarcodeQueryParam param, Long tenantId) {
        log.debug("分页查询条码，页码: {}, 每页: {}", param.getPageNum(), param.getPageSize());

        Page<LabelBarcode> page = new Page<>(param.getPageNum(), param.getPageSize());
        LambdaQueryWrapper<LabelBarcode> wrapper = buildQueryWrapper(param, tenantId);

        IPage<LabelBarcode> result = labelBarcodeMapper.selectPage(page, wrapper);

        // 转换为 VO
        IPage<BarcodeVO> voPage = result.convert(this::convertToVO);

        log.debug("查询完成，共 {} 条记录", voPage.getTotal());
        return voPage;
    }

    /**
     * 根据条码号查询
     * <p>
     * 用于条码反查场景，扫描条码后获取完整信息
     * </p>
     *
     * @param barcodeNo 条码号
     * @param tenantId 租户 ID
     * @return 条码信息
     * @throws I18nBusinessException 条码不存在时抛出异常
     */
    @Override
    public BarcodeVO getByBarcodeNo(String barcodeNo, Long tenantId) {
        log.debug("根据条码号查询: {}", barcodeNo);

        LambdaQueryWrapper<LabelBarcode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LabelBarcode::getBarcodeNo, barcodeNo)
                .eq(LabelBarcode::getTenantId, tenantId)
                .last("LIMIT 1");

        LabelBarcode barcode = labelBarcodeMapper.selectOne(wrapper);
        if (barcode == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, BasicPromptEnum.BARCODE_NOT_FOUND_WITH_NO, barcodeNo);
        }

        return convertToVO(barcode);
    }

    /**
     * 创建条码记录
     *
     * @param barcode 条码实体
     * @return 条码 ID
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long createBarcode(LabelBarcode barcode) {
        labelBarcodeMapper.insert(barcode);
        log.info("创建条码记录成功，ID: {}", barcode.getId());
        return barcode.getId();
    }

    /**
     * 批量创建条码记录
     *
     * @param barcodes 条码列表
     * @param tenantId 租户 ID
     * @return 成功创建的数量
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public int batchCreateBarcodes(List<LabelBarcode> barcodes, Long tenantId) {
        if (barcodes == null || barcodes.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (LabelBarcode barcode : barcodes) {
            barcode.setTenantId(tenantId);
            barcode.setGenerateTime(LocalDateTime.now());
            barcode.setStatus(PrintStatusEnum.SUCCESS.getCode()); // 使用枚举：已生成
            labelBarcodeMapper.insert(barcode);
            count++;
        }

        log.info("批量创建条码记录成功，数量: {}", count);
        return count;
    }

    /**
     * 更新条码状态
     *
     * @param id 条码 ID
     * @param status 状态：0-失效，1-有效
     * @param tenantId 租户 ID
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateStatus(Long id, Integer status, Long tenantId) {
        LabelBarcode barcode = labelBarcodeMapper.selectById(id);
        if (barcode == null || !barcode.getTenantId().equals(tenantId)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, BasicPromptEnum.BARCODE_NOT_FOUND);
        }

        // 验证状态值是否有效
        PrintStatusEnum statusEnum = PrintStatusEnum.getByCode(status);
        if (statusEnum == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, BasicPromptEnum.BARCODE_STATUS_INVALID, status);
        }

        barcode.setStatus(status);
        barcode.setUpdateBy(String.valueOf(getCurrentUserId()));
        barcode.setUpdateTime(LocalDateTime.now());

        labelBarcodeMapper.updateById(barcode);
        log.info("更新条码状态成功，ID: {}, 状态: {}", id, statusEnum.getDescription());
    }

    /**
     * 使条码失效
     *
     * @param barcodeNo 条码号
     * @param tenantId 租户 ID
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void invalidateBarcode(String barcodeNo, Long tenantId) {
        LambdaQueryWrapper<LabelBarcode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LabelBarcode::getBarcodeNo, barcodeNo)
                .eq(LabelBarcode::getTenantId, tenantId);

        LabelBarcode barcode = labelBarcodeMapper.selectOne(wrapper);
        if (barcode == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, BasicPromptEnum.BARCODE_NOT_FOUND_WITH_NO, barcodeNo);
        }

        if (PrintStatusEnum.FAILED.getCode().equals(barcode.getStatus())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, BasicPromptEnum.BARCODE_EXPIRED);
        }

        barcode.setStatus(PrintStatusEnum.FAILED.getCode()); // 使用枚举：已失效
        barcode.setUpdateBy(String.valueOf(getCurrentUserId()));
        barcode.setUpdateTime(LocalDateTime.now());

        labelBarcodeMapper.updateById(barcode);
        log.info("使条码失效成功: {}", barcodeNo);
    }


    /**
     * 查询指定物料的条码列表
     *
     * @param materialCode 物料编码
     * @param limit 限制条数
     * @param tenantId 租户 ID
     * @return 条码记录列表
     */
    @Override
    public List<LabelBarcode> listByMaterialCode(String materialCode, Integer limit, Long tenantId) {
        LambdaQueryWrapper<LabelBarcode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LabelBarcode::getMaterialCode, materialCode)
                .eq(LabelBarcode::getTenantId, tenantId)
                .orderByDesc(LabelBarcode::getGenerateTime);

        if (limit != null && limit > 0) {
            wrapper.last("LIMIT " + limit);
        }

        return labelBarcodeMapper.selectList(wrapper);
    }

    /**
     * 查询指定业务场景的条码列表
     *
     * @param businessScene 业务场景
     * @param limit 限制条数
     * @param tenantId 租户 ID
     * @return 条码记录列表
     */
    @Override
    public List<LabelBarcode> listByBusinessScene(String businessScene, Integer limit, Long tenantId) {
        LambdaQueryWrapper<LabelBarcode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LabelBarcode::getBusinessScene, businessScene)
                .eq(LabelBarcode::getTenantId, tenantId)
                .orderByDesc(LabelBarcode::getGenerateTime);

        if (limit != null && limit > 0) {
            wrapper.last("LIMIT " + limit);
        }

        return labelBarcodeMapper.selectList(wrapper);
    }

    /**
     * 条码反查（根据物料、LOT 号等查询条码）
     * <p>
     * 支持多维度业务数据反查条码，适用于：
     * - 根据物料查询所有相关条码
     * - 根据 LOT 号追溯批次条码
     * - 根据业务场景筛选条码
     * - 根据工厂过滤条码
     * </p>
     *
     * @param materialId 物料 ID
     * @param lotNo LOT 号
     * @param businessScene 业务场景
     * @param factoryId 工厂 ID
     * @param tenantId 租户 ID
     * @return 条码列表
     */
    @Override
    public List<BarcodeVO> queryByBusinessData(Long materialId, String lotNo, String businessScene,
                                               Long factoryId, Long tenantId) {
        log.debug("条码反查，物料ID: {}, LOT号: {}, 业务场景: {}", materialId, lotNo, businessScene);

        LambdaQueryWrapper<LabelBarcode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LabelBarcode::getTenantId, tenantId);

        if (materialId != null) {
            wrapper.eq(LabelBarcode::getMaterialId, materialId);
        }
        if (StringUtils.hasText(lotNo)) {
            wrapper.eq(LabelBarcode::getLotNo, lotNo);
        }
        if (StringUtils.hasText(businessScene)) {
            wrapper.eq(LabelBarcode::getBusinessScene, businessScene);
        }
        if (factoryId != null) {
            wrapper.eq(LabelBarcode::getFactoryId, factoryId);
        }

        wrapper.orderByDesc(LabelBarcode::getGenerateTime);

        List<LabelBarcode> barcodes = labelBarcodeMapper.selectList(wrapper);
        return barcodes.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 作废条码
     * <p>
     * 将条码状态标记为已作废，不可恢复
     * </p>
     *
     * @param barcodeId 条码 ID
     * @param tenantId 租户 ID
     * @throws I18nBusinessException 条码不存在或已作废时抛出异常
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void invalidateBarcode(Long barcodeId, Long tenantId) {
        log.info("作废条码，ID: {}", barcodeId);

        LabelBarcode barcode = labelBarcodeMapper.selectById(barcodeId);
        if (barcode == null || !barcode.getTenantId().equals(tenantId)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, BasicPromptEnum.BARCODE_NOT_FOUND);
        }

        if (PrintStatusEnum.FAILED.getCode().equals(barcode.getStatus())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, BasicPromptEnum.BARCODE_VOIDED);
        }

        barcode.setStatus(PrintStatusEnum.FAILED.getCode()); // 使用枚举：已作废
        barcode.setUpdateBy(String.valueOf(getCurrentUserId()));
        barcode.setUpdateTime(LocalDateTime.now());

        labelBarcodeMapper.updateById(barcode);
        log.info("条码作废成功: {}", barcode.getBarcodeNo());
    }

    /**
     * 检查条码号是否存在
     * <p>
     * 用于确保条码号的唯一性
     * </p>
     *
     * @param barcodeNo 条码号
     * @param tenantId 租户 ID
     * @return true-存在，false-不存在
     */
    @Override
    public boolean existsByBarcodeNo(String barcodeNo, Long tenantId) {
        LambdaQueryWrapper<LabelBarcode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LabelBarcode::getBarcodeNo, barcodeNo)
                .eq(LabelBarcode::getTenantId, tenantId);
        return labelBarcodeMapper.selectCount(wrapper) > 0;
    }

    /**
     * 统计指定业务场景的条码数量
     *
     * @param businessScene 业务场景
     * @param tenantId 租户 ID
     * @return 条码数量
     */
    @Override
    public long countByBusinessScene(String businessScene, Long tenantId) {
        LambdaQueryWrapper<LabelBarcode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LabelBarcode::getBusinessScene, businessScene)
                .eq(LabelBarcode::getTenantId, tenantId);
        return labelBarcodeMapper.selectCount(wrapper);
    }

    /**
     * 删除条码记录（逻辑删除）
     *
     * @param id 条码 ID
     * @param tenantId 租户 ID
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteBarcode(Long id, Long tenantId) {
        LabelBarcode barcode = labelBarcodeMapper.selectById(id);
        if (barcode == null || !barcode.getTenantId().equals(tenantId)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, BasicPromptEnum.BARCODE_NOT_FOUND);
        }

        labelBarcodeMapper.deleteById(id);
        log.info("删除条码记录成功，ID: {}", id);
    }

    /**
     * 根据业务场景选择条码生成策略
     * <p>
     * 策略优先级：
     * 1. LOT 号关联策略（来料标签）
     * 2. 工程卡号关联策略（生产标签）
     * 3. 业务规则组合策略（包含物料信息）
     * 4. 时间戳策略（默认）
     * </p>
     *
     * @param param 生成参数
     * @return 条码号
     */
    private String generateBarcodeByStrategy(BarcodeGenerateParam param) {
        String businessScene = param.getBusinessScene();

        // 策略1：LOT 号关联（来料标签）
        if ("INCOMING".equals(businessScene) && StringUtils.hasText(param.getLotNo())) {
            return BarcodeGenerator.generateByLotNo(param.getLotNo());
        }

        // 策略2：工程卡号关联（生产标签）
        if ("PRODUCTION".equals(businessScene) && StringUtils.hasText(param.getEngineeringCardNo())) {
            return BarcodeGenerator.generateByEngineeringCard(param.getEngineeringCardNo());
        }

        // 策略3：业务规则组合（包含物料信息）
        if (param.getMaterialId() != null && StringUtils.hasText(param.getTemplateType())) {
            return BarcodeGenerator.generateByBusinessRule(param.getTemplateType(), param.getMaterialId());
        }

        // 策略4：时间戳策略（默认）
        return BarcodeGenerator.generateByTimestamp("BC");
    }

    /**
     * 构建查询条件
     * <p>
     * 根据查询参数动态构建 MyBatis-Plus 查询条件
     * </p>
     *
     * @param param 查询参数
     * @param tenantId 租户 ID
     * @return 查询条件
     */
    private LambdaQueryWrapper<LabelBarcode> buildQueryWrapper(BarcodeQueryParam param, Long tenantId) {
        LambdaQueryWrapper<LabelBarcode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LabelBarcode::getTenantId, tenantId);

        // 条码号模糊查询
        if (StringUtils.hasText(param.getBarcodeNo())) {
            wrapper.like(LabelBarcode::getBarcodeNo, param.getBarcodeNo());
        }
        // 模板类型精确匹配
        if (StringUtils.hasText(param.getTemplateType())) {
            wrapper.eq(LabelBarcode::getTemplateType, param.getTemplateType());
        }
        // 物料 ID 精确匹配
        if (param.getMaterialId() != null) {
            wrapper.eq(LabelBarcode::getMaterialId, param.getMaterialId());
        }
        // LOT 号精确匹配
        if (StringUtils.hasText(param.getLotNo())) {
            wrapper.eq(LabelBarcode::getLotNo, param.getLotNo());
        }
        // 业务场景精确匹配
        if (StringUtils.hasText(param.getBusinessScene())) {
            wrapper.eq(LabelBarcode::getBusinessScene, param.getBusinessScene());
        }
        // 工厂 ID 精确匹配
        if (param.getFactoryId() != null) {
            wrapper.eq(LabelBarcode::getFactoryId, param.getFactoryId());
        }
        // 状态筛选
        if (param.getStatus() != null) {
            wrapper.eq(LabelBarcode::getStatus, param.getStatus());
        }
        // 开始时间
        if (param.getStartTime() != null) {
            wrapper.ge(LabelBarcode::getGenerateTime, param.getStartTime());
        }
        // 结束时间
        if (param.getEndTime() != null) {
            wrapper.le(LabelBarcode::getGenerateTime, param.getEndTime());
        }

        // 按生成时间倒序排列
        wrapper.orderByDesc(LabelBarcode::getGenerateTime);
        return wrapper;
    }

    /**
     * 转换为 DTO
     * <p>
     * 将实体对象转换为数据传输对象
     * </p>
     *
     * @param barcode 实体
     * @return DTO
     */
    private LabelBarcodeDTO convertToDTO(LabelBarcode barcode) {
        LabelBarcodeDTO dto = new LabelBarcodeDTO();
        dto.setId(barcode.getId());
        dto.setBarcodeNo(barcode.getBarcodeNo());
        dto.setTemplateType(barcode.getTemplateType());
        dto.setMaterialId(barcode.getMaterialId());
        dto.setMaterialCode(barcode.getMaterialCode());
        dto.setMaterialName(barcode.getMaterialName());
        dto.setSupplierCode(barcode.getSupplierCode());
        dto.setCustomerCode(barcode.getCustomerCode());
        dto.setLotNo(barcode.getLotNo());
        dto.setEngineeringCardNo(barcode.getEngineeringCardNo());
        dto.setBatchNo(barcode.getBatchNo());
        dto.setBusinessScene(barcode.getBusinessScene());
        dto.setFactoryId(barcode.getFactoryId());
        dto.setGenerateTime(barcode.getGenerateTime());
        dto.setStatus(barcode.getStatus());
        dto.setRemark(barcode.getRemark());
        return dto;
    }

    /**
     * 转换为 VO
     * <p>
     * 将实体对象转换为视图对象，补充显示名称等前端展示字段
     * </p>
     *
     * @param barcode 实体
     * @return VO
     */
    private BarcodeVO convertToVO(LabelBarcode barcode) {
        BarcodeVO vo = new BarcodeVO();
        vo.setId(barcode.getId());
        vo.setBarcodeNo(barcode.getBarcodeNo());
        vo.setTemplateType(barcode.getTemplateType());
        vo.setTemplateTypeName(getTemplateName(barcode.getTemplateType()));
        vo.setMaterialId(barcode.getMaterialId());
        vo.setMaterialCode(barcode.getMaterialCode());
        vo.setMaterialName(barcode.getMaterialName());
        vo.setSupplierCode(barcode.getSupplierCode());
        vo.setSupplierName(getSupplierNameByCode(barcode.getSupplierCode()));
        vo.setCustomerCode(barcode.getCustomerCode());
        vo.setCustomerName(getCustomerNameByCode(barcode.getCustomerCode()));
        vo.setLotNo(barcode.getLotNo());
        vo.setEngineeringCardNo(barcode.getEngineeringCardNo());
        vo.setBatchNo(barcode.getBatchNo());
        vo.setBusinessScene(barcode.getBusinessScene());
        vo.setBusinessSceneName(getBusinessSceneName(barcode.getBusinessScene()));
        vo.setFactoryId(barcode.getFactoryId());
        vo.setGenerateTime(barcode.getGenerateTime());
        vo.setStatus(barcode.getStatus());
        vo.setStatusName(getStatusName(barcode.getStatus()));
        vo.setCreateBy(barcode.getCreateBy());
        vo.setCreateTime(barcode.getCreateTime());
        vo.setRemark(barcode.getRemark());
        return vo;
    }

    /**
     * 获取模板类型名称
     * <p>
     * 从 TemplateTypeEnum 枚举中获取模板类型名称
     * </p>
     *
     * @param templateType 模板类型代码
     * @return 模板类型名称
     */
    private String getTemplateName(String templateType) {
        if (templateType == null) {
            return null;
        }
        TemplateTypeEnum typeEnum = TemplateTypeEnum.getByCode(templateType);
        return typeEnum != null ? typeEnum.getName() : templateType;
    }

    /**
     * 获取物料编码
     * <p>
     * TODO: 后续调用 IMaterialService 查询真实物料编码
     * </p>
     *
     * @param materialId 物料 ID
     * @return 物料编码
     */
    private String getMaterialCode(Long materialId) {
        if (materialId == null) {
            return null;
        }
        return "MAT" + materialId;
    }

    /**
     * 获取物料名称
     * <p>
     * TODO: 后续调用 IMaterialService 查询真实物料名称
     * </p>
     *
     * @param materialId 物料 ID
     * @return 物料名称
     */
    private String getMaterialName(Long materialId) {
        if (materialId == null) {
            return null;
        }
        return "物料" + materialId;
    }

    /**
     * 根据供应商编码获取供应商名称
     * <p>
     * TODO: 后续调用 ISupplierService 查询真实供应商名称
     * </p>
     *
     * @param supplierCode 供应商编码
     * @return 供应商名称
     */
    private String getSupplierNameByCode(String supplierCode) {
        if (!StringUtils.hasText(supplierCode)) {
            return null;
        }
        return "供应商-" + supplierCode;
    }

    /**
     * 根据客户编码获取客户名称
     * <p>
     * TODO: 后续调用 ICustomerService 查询真实客户名称
     * </p>
     *
     * @param customerCode 客户编码
     * @return 客户名称
     */
    private String getCustomerNameByCode(String customerCode) {
        if (!StringUtils.hasText(customerCode)) {
            return null;
        }
        return "客户-" + customerCode;
    }

    /**
     * 获取业务场景名称
     * <p>
     * 将业务场景代码转换为中文显示名称
     * </p>
     *
     * @param businessScene 业务场景代码
     * @return 业务场景名称
     */
    private String getBusinessSceneName(String businessScene) {
        if (businessScene == null) {
            return null;
        }
        return switch (businessScene) {
            case "INCOMING" -> "来料";
            case "PRODUCTION" -> "生产";
            case "OUTBOUND" -> "出库";
            case "OTHER" -> "其他";
            default -> businessScene;
        };
    }

    /**
     * 获取状态名称
     * <p>
     * 从 PrintStatusEnum 枚举中获取状态名称
     * </p>
     *
     * @param status 状态代码
     * @return 状态名称
     */
    private String getStatusName(Integer status) {
        if (status == null) {
            return null;
        }
        PrintStatusEnum statusEnum = PrintStatusEnum.getByCode(status);
        return statusEnum != null ? statusEnum.getDescription() : null;
    }


    /**
     * 获取当前用户 ID
     * <p>
     * TODO: 后续从安全上下文（如 Sa-Token）获取真实用户 ID
     * </p>
     *
     * @return 用户 ID
     */
    private Long getCurrentUserId() {
        return 1L;
    }
}
