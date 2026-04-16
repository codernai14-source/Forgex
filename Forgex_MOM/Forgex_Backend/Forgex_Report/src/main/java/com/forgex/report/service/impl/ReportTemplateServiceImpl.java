package com.forgex.report.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.web.StatusCode;
import com.forgex.report.domain.dto.ReportCategoryDTO;
import com.forgex.report.enums.ReportPromptEnum;
import com.forgex.report.domain.dto.ReportDatasourceDTO;
import com.forgex.report.domain.dto.ReportTemplateDTO;
import com.forgex.report.domain.entity.ReportCategory;
import com.forgex.report.domain.entity.ReportDatasource;
import com.forgex.report.domain.entity.ReportTemplate;
import com.forgex.report.domain.param.ReportTemplateParam;
import com.forgex.report.mapper.ReportTemplateMapper;
import com.forgex.report.service.IReportCategoryService;
import com.forgex.report.service.IReportDatasourceService;
import com.forgex.report.service.IReportTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 报表模板服务实现类
 * <p>
 * 实现报表模板的业务逻辑
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-09
 * @see ServiceImpl
 * @see IReportTemplateService
 * @see ReportTemplate
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportTemplateServiceImpl extends ServiceImpl<ReportTemplateMapper, ReportTemplate> 
        implements IReportTemplateService {

    private final IReportCategoryService categoryService;
    private final IReportDatasourceService datasourceService;

    /**
     * 分页查询报表模板
     * <p>
     * 根据查询条件动态构建查询语句，返回分页结果
     * 支持关联查询分类名称和数据源名称
     * </p>
     *
     * @param param 查询参数
     * @return 分页结果
     * @throws IllegalArgumentException 当参数为空时抛出
     */
    @Override
    public Page<ReportTemplateDTO> pageByParam(ReportTemplateParam param) {
        if (param == null) {
            throw new IllegalArgumentException("查询参数不能为空");
        }

        Page<ReportTemplate> page = new Page<>(param.getPageNum(), param.getPageSize());
        
        // 构建动态查询条件
        LambdaQueryWrapper<ReportTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(param.getName()), ReportTemplate::getName, param.getName())
               .eq(StrUtil.isNotBlank(param.getCode()), ReportTemplate::getCode, param.getCode())
               .eq(StrUtil.isNotBlank(param.getEngineType()), ReportTemplate::getEngineType, param.getEngineType())
               .eq(param.getCategoryId() != null, ReportTemplate::getCategoryId, param.getCategoryId())
               .eq(param.getStatus() != null, ReportTemplate::getStatus, param.getStatus())
               .orderByDesc(ReportTemplate::getCreateTime);

        Page<ReportTemplate> resultPage = this.page(page, wrapper);
        
        // 转换为 DTO 并填充关联信息
        List<ReportTemplateDTO> dtoList = resultPage.getRecords().stream()
                .map(this::convertToDTOWithRelations)
                .collect(Collectors.toList());
        
        Page<ReportTemplateDTO> dtoPage = new Page<>(param.getPageNum(), param.getPageSize(), resultPage.getTotal());
        dtoPage.setRecords(dtoList);
        
        return dtoPage;
    }

    /**
     * 根据 ID 获取模板详情
     *
     * @param id 模板 ID
     * @return 模板 DTO
     * @throws BusinessException 当模板不存在时抛出
     */
    @Override
    public ReportTemplateDTO getById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("模板 ID 不能为空");
        }

        ReportTemplate template = super.getById(id);
        if (template == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, ReportPromptEnum.REPORT_TEMPLATE_NOT_FOUND);
        }

        return convertToDTOWithRelations(template);
    }

    /**
     * 根据编码获取模板
     *
     * @param code 模板编码
     * @param engineType 引擎类型
     * @return 模板 DTO
     * @throws BusinessException 当模板不存在时抛出
     */
    @Override
    public ReportTemplateDTO getByCode(String code, String engineType) {
        if (StrUtil.isBlank(code)) {
            throw new IllegalArgumentException("模板编码不能为空");
        }

        LambdaQueryWrapper<ReportTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReportTemplate::getCode, code);
        if (StrUtil.isNotBlank(engineType)) {
            wrapper.eq(ReportTemplate::getEngineType, engineType);
        }

        ReportTemplate template = this.getOne(wrapper);
        if (template == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, ReportPromptEnum.REPORT_TEMPLATE_NOT_FOUND);
        }

        return convertToDTOWithRelations(template);
    }

    /**
     * 保存模板
     * <p>
     * 支持新增和更新，根据 ID 判断
     * 编码唯一性校验
     * </p>
     *
     * @param dto 模板 DTO
     * @return 保存后的模板 DTO
     * @throws IllegalArgumentException 当参数为空时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReportTemplateDTO save(ReportTemplateDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("模板数据不能为空");
        }

        // 校验编码唯一性
        if (StrUtil.isNotBlank(dto.getCode())) {
            long count = this.count(new LambdaQueryWrapper<ReportTemplate>()
                    .eq(ReportTemplate::getCode, dto.getCode())
                    .eq(StrUtil.isNotBlank(dto.getEngineType()), ReportTemplate::getEngineType, dto.getEngineType())
                    .ne(dto.getId() != null, ReportTemplate::getId, dto.getId()));
            if (count > 0) {
                throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, ReportPromptEnum.REPORT_TEMPLATE_CODE_EXISTS);
            }
        }

        // 转换为实体
        ReportTemplate entity = new ReportTemplate();
        BeanUtils.copyProperties(dto, entity);

        // 保存
        this.saveOrUpdate(entity);

        // 返回 DTO
        return convertToDTOWithRelations(entity);
    }

    /**
     * 更新模板内容
     * <p>
     * 用于报表设计器保存时调用
     * </p>
     *
     * @param id 模板 ID
     * @param content 模板内容
     * @throws BusinessException 当模板不存在时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateContent(Long id, String content) {
        if (id == null) {
            throw new IllegalArgumentException("模板 ID 不能为空");
        }

        ReportTemplate template = super.getById(id);
        if (template == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, ReportPromptEnum.REPORT_TEMPLATE_NOT_FOUND);
        }

        template.setContent(content);
        this.updateById(template);
        
        log.info("更新报表模板内容成功，ID: {}", id);
    }

    /**
     * 批量删除模板
     *
     * @param ids 模板 ID 列表
     * @throws IllegalArgumentException 当 ID 列表为空时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            throw new IllegalArgumentException("删除的 ID 列表不能为空");
        }

        this.removeByIds(ids);
    }

    /**
     * 导出模板文件
     * <p>
     * 将模板内容导出为文件
     * </p>
     *
     * @param id 模板 ID
     * @return 文件路径
     * @throws BusinessException 当模板不存在或导出失败时抛出
     */
    @Override
    public String exportTemplate(Long id) {
        ReportTemplate template = super.getById(id);
        if (template == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, ReportPromptEnum.REPORT_TEMPLATE_NOT_FOUND);
        }

        if (StrUtil.isBlank(template.getContent())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, ReportPromptEnum.REPORT_TEMPLATE_CONTENT_EMPTY);
        }

        try {
            // 生成文件路径
            String fileName = template.getCode() + (template.getEngineType().equals("UREPORT") ? ".ureport.xml" : ".json");
            String filePath = "temp/templates/" + fileName;
            
            // 写入文件
            FileUtil.writeUtf8String(template.getContent(), filePath);
            
            log.info("导出报表模板成功：{}", filePath);
            return filePath;
        } catch (Exception e) {
            log.error("导出报表模板失败", e);
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, ReportPromptEnum.REPORT_TEMPLATE_EXPORT_FAILED, e.getMessage());
        }
    }

    /**
     * 导入模板文件
     * <p>
     * 从文件导入模板内容
     * </p>
     *
     * @param filePath 文件路径
     * @param engineType 引擎类型
     * @return 导入后的模板 DTO
     * @throws IllegalArgumentException 当文件不存在或格式错误时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReportTemplateDTO importTemplate(String filePath, String engineType) {
        if (StrUtil.isBlank(filePath)) {
            throw new IllegalArgumentException("文件路径不能为空");
        }

        File file = new File(filePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("文件不存在");
        }

        try {
            // 读取文件内容
            String content = FileUtil.readUtf8String(filePath);
            
            // 从文件名提取编码
            String fileName = file.getName();
            String code = fileName.substring(0, fileName.lastIndexOf("."));
            
            // 创建模板
            ReportTemplateDTO dto = new ReportTemplateDTO();
            dto.setCode(code);
            dto.setName(code);
            dto.setEngineType(engineType);
            dto.setContent(content);
            dto.setStatus(1);
            
            return this.save(dto);
        } catch (Exception e) {
            log.error("导入报表模板失败", e);
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, ReportPromptEnum.REPORT_TEMPLATE_IMPORT_FAILED, e.getMessage());
        }
    }

    /**
     * 将实体转换为 DTO（不包含关联信息）
     *
     * @param entity 实体对象
     * @return DTO 对象
     */
    private ReportTemplateDTO convertToDTO(ReportTemplate entity) {
        if (entity == null) {
            return null;
        }

        ReportTemplateDTO dto = new ReportTemplateDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    /**
     * 将实体转换为 DTO（包含关联信息）
     *
     * @param entity 实体对象
     * @return DTO 对象
     */
    private ReportTemplateDTO convertToDTOWithRelations(ReportTemplate entity) {
        if (entity == null) {
            return null;
        }

        ReportTemplateDTO dto = convertToDTO(entity);
        
        // 填充分类名称
        if (entity.getCategoryId() != null) {
            try {
                ReportCategoryDTO category = categoryService.getById(entity.getCategoryId());
                if (category != null) {
                    dto.setCategoryName(category.getName());
                }
            } catch (Exception e) {
                log.warn("获取分类信息失败", e);
            }
        }
        
        // 填充数据源名称
        if (entity.getDatasourceId() != null) {
            try {
                ReportDatasourceDTO datasource = datasourceService.getById(entity.getDatasourceId());
                if (datasource != null) {
                    dto.setDatasourceName(datasource.getName());
                }
            } catch (Exception e) {
                log.warn("获取数据源信息失败", e);
            }
        }
        
        return dto;
    }
}
