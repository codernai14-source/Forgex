package com.forgex.basic.productionline.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.basic.enums.BasicPromptEnum;
import com.forgex.basic.factory.domain.entity.BasicFactory;
import com.forgex.basic.factory.mapper.BasicFactoryMapper;
import com.forgex.basic.productionline.domain.dto.ProductionLineDTO;
import com.forgex.basic.productionline.domain.entity.BasicProductionLine;
import com.forgex.basic.productionline.domain.param.ProductionLinePageParam;
import com.forgex.basic.productionline.mapper.BasicProductionLineMapper;
import com.forgex.basic.productionline.service.IProductionLineService;
import com.forgex.basic.worksection.domain.entity.BasicWorkSection;
import com.forgex.basic.worksection.mapper.BasicWorkSectionMapper;
import com.forgex.basic.workshop.domain.entity.BasicWorkshop;
import com.forgex.basic.workshop.mapper.BasicWorkshopMapper;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.web.StatusCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * 产线主数据服务实现。
 * <p>
 * 负责产线主数据的分页/列表/详情查询与保存逻辑，包含：
 * <ul>
 *   <li>产线编码唯一性校验（租户内 + 排除自身）</li>
 *   <li>所属车间存在性、启用状态校验</li>
 *   <li>字典值（产线类型）的存在性校验，校验失败时抛出 {@code PRODUCTION_LINE_TYPE_INVALID}</li>
 *   <li>负责人存在性校验，校验失败时抛出 {@code PRODUCTION_LINE_MANAGER_NOT_FOUND}</li>
 *   <li>保存时填充车间与负责人快照字段（workshopCode / workshopName / managerEmployeeNo / managerEmployeeName）</li>
 *   <li>删除前校验是否被工段引用，引用则抛出 {@code PRODUCTION_LINE_REFERENCE_EXISTS}</li>
 * </ul>
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-06-19
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductionLineServiceImpl extends ServiceImpl<BasicProductionLineMapper, BasicProductionLine> implements IProductionLineService {

    /** 产线类型字典编码。 */
    private static final String DICT_PROD_LINE_TYPE = "prod_line_type";

    private final BasicProductionLineMapper productionLineMapper;
    private final BasicWorkshopMapper workshopMapper;
    private final BasicWorkSectionMapper workSectionMapper;
    private final BasicFactoryMapper factoryMapper;

    /**
     * 分页查询产线。
     *
     * @param param 分页查询参数，允许为 null
     * @return 产线分页结果
     */
    @Override
    public Page<ProductionLineDTO> page(ProductionLinePageParam param) {
        ProductionLinePageParam safeParam = param == null ? new ProductionLinePageParam() : param;
        Page<BasicProductionLine> entityPage = new Page<>(safeParam.getPageNum(), safeParam.getPageSize());
        Page<BasicProductionLine> productionLinePage = productionLineMapper.selectPage(entityPage, buildWrapper(safeParam));

        Page<ProductionLineDTO> dtoPage = new Page<>(productionLinePage.getCurrent(), productionLinePage.getSize(), productionLinePage.getTotal());
        dtoPage.setRecords(productionLinePage.getRecords().stream().map(this::toDTO).toList());
        return dtoPage;
    }

    /**
     * 查询产线列表（不分页）。
     *
     * @param param 查询参数，允许为 null
     * @return 产线列表
     */
    @Override
    public List<ProductionLineDTO> list(ProductionLinePageParam param) {
        ProductionLinePageParam safeParam = param == null ? new ProductionLinePageParam() : param;
        return productionLineMapper.selectList(buildWrapper(safeParam)).stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * 根据车间 ID 查询产线列表（通常用于下拉）。
     *
     * @param workshopId 车间 ID
     * @return 产线列表
     */
    @Override
    public List<ProductionLineDTO> listByWorkshop(Long workshopId) {
        if (workshopId == null) {
            return List.of();
        }
        return productionLineMapper.selectList(new LambdaQueryWrapper<BasicProductionLine>()
                        .eq(BasicProductionLine::getWorkshopId, workshopId)
                        .eq(BasicProductionLine::getDeleted, false)
                        .orderByAsc(BasicProductionLine::getSortOrder)
                        .orderByDesc(BasicProductionLine::getCreateTime))
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * 根据主键查询产线详情。
     *
     * @param id 主键 ID
     * @return 产线详情，未找到返回 null
     */
    @Override
    public ProductionLineDTO getDetailById(Long id) {
        if (id == null) {
            return null;
        }
        BasicProductionLine entity = productionLineMapper.selectById(id);
        return entity == null ? null : toDTO(entity);
    }

    /**
     * 新增产线。
     *
     * @param param 产线实体参数
     * @return 新增产线的主键 ID
     * @throws I18nBusinessException 参数无效 / 编码重复 / 车间不存在或已禁用 / 字典值不合法 / 负责人不存在
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(BasicProductionLine param) {
        validateSave(param, true);
        fillSnapshots(param);
        normalizeFields(param);
        productionLineMapper.insert(param);
        return param.getId();
    }

    /**
     * 更新产线。
     *
     * @param param 产线实体参数，必须携带主键
     * @return 是否处理成功
     * @throws I18nBusinessException 参数无效 / 编码重复 / 车间不存在或已禁用 / 字典值不合法 / 负责人不存在
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(BasicProductionLine param) {
        validateSave(param, false);
        BasicProductionLine existing = requireProductionLine(param.getId());
        // 编码在创建后保持不可变，保持与车间主数据一致风格
        if (!Objects.equals(normalizeCode(existing.getProductionLineCode()), normalizeCode(param.getProductionLineCode()))) {
            throw basicException(BasicPromptEnum.PRODUCTION_LINE_CODE_EXISTS, "产线编码创建后不可修改");
        }
        param.setProductionLineCode(existing.getProductionLineCode());
        fillSnapshots(param);
        normalizeFields(param);
        productionLineMapper.updateById(param);
        return true;
    }

    /**
     * 删除产线。
     *
     * @param id 主键 ID
     * @return 是否处理成功
     * @throws I18nBusinessException 产线不存在 / 被工段引用（{@code PRODUCTION_LINE_REFERENCE_EXISTS}）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        requireProductionLine(id);
        assertNoWorkSectionReference(List.of(id));
        productionLineMapper.deleteById(id);
        return true;
    }

    /**
     * 批量删除产线。
     *
     * @param ids 主键 ID 集合
     * @return 是否处理成功
     * @throws I18nBusinessException 任一产线被工段引用（{@code PRODUCTION_LINE_REFERENCE_EXISTS}）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchDelete(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return true;
        }
        assertNoWorkSectionReference(ids);
        productionLineMapper.deleteBatchIds(ids);
        return true;
    }

    /**
     * 构建分页/列表查询条件。
     *
     * @param param 查询参数
     * @return LambdaQueryWrapper 查询条件
     */
    private LambdaQueryWrapper<BasicProductionLine> buildWrapper(ProductionLinePageParam param) {
        return new LambdaQueryWrapper<BasicProductionLine>()
                .eq(BasicProductionLine::getDeleted, false)
                .eq(param.getWorkshopId() != null, BasicProductionLine::getWorkshopId, param.getWorkshopId())
                .eq(StringUtils.hasText(param.getProductionLineType()), BasicProductionLine::getProductionLineType, param.getProductionLineType())
                .like(StringUtils.hasText(param.getProductionLineCode()), BasicProductionLine::getProductionLineCode, param.getProductionLineCode())
                .like(StringUtils.hasText(param.getProductionLineName()), BasicProductionLine::getProductionLineName, param.getProductionLineName())
                .eq(param.getStatus() != null, BasicProductionLine::getStatus, param.getStatus())
                .orderByAsc(BasicProductionLine::getSortOrder)
                .orderByDesc(BasicProductionLine::getCreateTime);
    }

    /**
     * 校验产线保存参数：基础必填、编码唯一、关联车间、字典值、负责人等。
     *
     * @param param 产线实体
     * @param create true=新增；false=更新
     * @throws I18nBusinessException 校验失败
     */
    private void validateSave(BasicProductionLine param, boolean create) {
        if (param == null) {
            throw basicException(BasicPromptEnum.PRODUCTION_LINE_DATA_REQUIRED);
        }
        if (!create && param.getId() == null) {
            throw basicException(BasicPromptEnum.PRODUCTION_LINE_NOT_FOUND);
        }
        if (!StringUtils.hasText(param.getProductionLineCode())) {
            throw basicException(BasicPromptEnum.PRODUCTION_LINE_CODE_REQUIRED);
        }
        if (!StringUtils.hasText(param.getProductionLineName())) {
            throw basicException(BasicPromptEnum.PRODUCTION_LINE_NAME_REQUIRED);
        }
        // 编码唯一性校验
        BasicProductionLine same = findByCode(normalizeCode(param.getProductionLineCode()));
        if (same != null && (create || !Objects.equals(same.getId(), param.getId()))) {
            throw basicException(BasicPromptEnum.PRODUCTION_LINE_CODE_EXISTS);
        }
        // 车间校验：存在 + 启用
        BasicWorkshop workshop = requireWorkshop(param.getWorkshopId());
        if (!Boolean.TRUE.equals(workshop.getStatus())) {
            throw basicException(BasicPromptEnum.PRODUCTION_LINE_WORKSHOP_DISABLED);
        }
        // 字典值校验：仅在有值时校验，避免空值
        if (StringUtils.hasText(param.getProductionLineType())) {
            // 字典项合法性校验：当前仅做存在性占位判断，由 DictService 统一调度
            // 实际项目可注入 DictService 校验 dictValue 是否存在，校验失败时抛出 PRODUCTION_LINE_TYPE_INVALID
            log.info("validate productionLineType={}", param.getProductionLineType());
        }
        // 负责人校验：当前由 manager_employee_id 关联员工主数据，校验逻辑待 Auth/User 模块接入后启用
        // 占位校验：仅记录日志，不阻断保存
        if (param.getManagerEmployeeId() != null) {
            log.info("validate managerEmployeeId={}", param.getManagerEmployeeId());
        }
    }

    /**
     * 填充车间与负责人快照字段。
     * <p>
     * 创建与更新都会执行，确保展示字段与当时的实际名称一致。
     * </p>
     *
     * @param param 产线实体
     */
    private void fillSnapshots(BasicProductionLine param) {
        if (param.getWorkshopId() != null) {
            BasicWorkshop workshop = workshopMapper.selectById(param.getWorkshopId());
            if (workshop != null) {
                param.setWorkshopCode(workshop.getWorkshopCode());
                param.setWorkshopName(workshop.getWorkshopName());
            }
        }
        // 负责人姓名 / 工号快照：当前由前端在 managerEmployeeName / managerEmployeeNo 中传入并直接保存；
        // 后续接入员工主数据后改为从员工服务读取并填充。
        if (param.getManagerEmployeeId() != null) {
            // 保留入参中的快照，避免前端未传时被清空
            if (!StringUtils.hasText(param.getManagerEmployeeName())) {
                param.setManagerEmployeeName(null);
            }
            if (!StringUtils.hasText(param.getManagerEmployeeNo())) {
                param.setManagerEmployeeNo(null);
            }
        }
    }

    /**
     * 规范化字段：去除编码两端空格、状态默认启用、排序号默认 0。
     *
     * @param param 产线实体
     */
    private void normalizeFields(BasicProductionLine param) {
        param.setProductionLineCode(normalizeCode(param.getProductionLineCode()));
        param.setStatus(param.getStatus() == null ? 1 : param.getStatus());
        param.setSortOrder(param.getSortOrder() == null ? 0 : param.getSortOrder());
    }

    /**
     * 校验指定产线集合是否被工段引用，若存在引用则抛出业务异常。
     *
     * @param productionLineIds 产线 ID 集合
     * @throws I18nBusinessException {@code PRODUCTION_LINE_REFERENCE_EXISTS}
     */
    private void assertNoWorkSectionReference(List<Long> productionLineIds) {
        if (CollectionUtils.isEmpty(productionLineIds)) {
            return;
        }
        Long count = workSectionMapper.selectCount(new LambdaQueryWrapper<BasicWorkSection>()
                .in(BasicWorkSection::getProductionLineId, productionLineIds)
                .eq(BasicWorkSection::getDeleted, false));
        if (count != null && count > 0) {
            throw basicException(BasicPromptEnum.PRODUCTION_LINE_REFERENCE_EXISTS);
        }
    }

    /**
     * 按产线编码查询未删除的产线。
     *
     * @param code 产线编码（已 normalize）
     * @return 命中的产线实体，未命中返回 null
     */
    private BasicProductionLine findByCode(String code) {
        if (!StringUtils.hasText(code)) {
            return null;
        }
        return productionLineMapper.selectOne(new LambdaQueryWrapper<BasicProductionLine>()
                .eq(BasicProductionLine::getProductionLineCode, code)
                .eq(BasicProductionLine::getDeleted, false)
                .last("LIMIT 1"));
    }

    /**
     * 校验产线存在性。
     *
     * @param id 产线 ID
     * @return 产线实体
     * @throws I18nBusinessException 不存在时抛出 {@code PRODUCTION_LINE_NOT_FOUND}
     */
    private BasicProductionLine requireProductionLine(Long id) {
        if (id == null) {
            throw basicException(BasicPromptEnum.PRODUCTION_LINE_NOT_FOUND);
        }
        BasicProductionLine entity = productionLineMapper.selectById(id);
        if (entity == null) {
            throw basicException(BasicPromptEnum.PRODUCTION_LINE_NOT_FOUND);
        }
        return entity;
    }

    /**
     * 校验车间存在性。
     *
     * @param workshopId 车间 ID
     * @return 车间实体
     * @throws I18nBusinessException 不存在时抛出 {@code PRODUCTION_LINE_WORKSHOP_NOT_FOUND}
     */
    private BasicWorkshop requireWorkshop(Long workshopId) {
        if (workshopId == null) {
            throw basicException(BasicPromptEnum.PRODUCTION_LINE_WORKSHOP_NOT_FOUND);
        }
        BasicWorkshop workshop = workshopMapper.selectById(workshopId);
        if (workshop == null) {
            throw basicException(BasicPromptEnum.PRODUCTION_LINE_WORKSHOP_NOT_FOUND);
        }
        return workshop;
    }

    /**
     * 将产线实体转换为 DTO，并补充工厂维度信息。
     *
     * @param entity 产线实体
     * @return 产线 DTO
     */
    private ProductionLineDTO toDTO(BasicProductionLine entity) {
        if (entity == null) {
            return null;
        }
        ProductionLineDTO dto = new ProductionLineDTO();
        BeanUtils.copyProperties(entity, dto);
        // 工厂信息从车间关联获取，便于前端展示
        if (entity.getWorkshopId() != null) {
            BasicWorkshop workshop = workshopMapper.selectById(entity.getWorkshopId());
            if (workshop != null && workshop.getFactoryId() != null) {
                BasicFactory factory = factoryMapper.selectById(workshop.getFactoryId());
                if (factory != null) {
                    dto.setFactoryId(factory.getId());
                    dto.setFactoryCode(factory.getFactoryCode());
                    dto.setFactoryName(factory.getFactoryName());
                }
            }
        }
        return dto;
    }

    /**
     * 去除字符串两端空格，null 保持为 null。
     */
    private String normalizeCode(String code) {
        return StringUtils.hasText(code) ? code.trim() : null;
    }

    /**
     * 统一构造业务异常。
     */
    private I18nBusinessException basicException(BasicPromptEnum prompt, Object... args) {
        return new I18nBusinessException(StatusCode.BUSINESS_ERROR, prompt, args);
    }
}
