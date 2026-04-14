package com.forgex.report.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.common.exception.BusinessException;
import com.forgex.report.domain.dto.ReportCategoryDTO;
import com.forgex.report.domain.entity.ReportCategory;
import com.forgex.report.domain.param.ReportCategoryParam;
import com.forgex.report.mapper.ReportCategoryMapper;
import com.forgex.report.service.IReportCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 报表分类服务实现类
 * <p>
 * 实现报表分类的业务逻辑
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-09
 * @see ServiceImpl
 * @see IReportCategoryService
 * @see ReportCategory
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportCategoryServiceImpl extends ServiceImpl<ReportCategoryMapper, ReportCategory> 
        implements IReportCategoryService {

    /**
     * 分页查询报表分类
     * <p>
     * 根据查询条件动态构建查询语句，返回分页结果
     * </p>
     *
     * @param param 查询参数
     * @return 分页结果
     * @throws IllegalArgumentException 当参数为空时抛出
     */
    @Override
    public Page<ReportCategoryDTO> pageByParam(ReportCategoryParam param) {
        if (param == null) {
            throw new IllegalArgumentException("查询参数不能为空");
        }

        Page<ReportCategory> page = new Page<>(param.getPageNum(), param.getPageSize());
        
        // 构建动态查询条件
        LambdaQueryWrapper<ReportCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(param.getName()), ReportCategory::getName, param.getName())
               .eq(StrUtil.isNotBlank(param.getCode()), ReportCategory::getCode, param.getCode())
               .eq(param.getParentId() != null, ReportCategory::getParentId, param.getParentId())
               .eq(param.getStatus() != null, ReportCategory::getStatus, param.getStatus())
               .orderByAsc(ReportCategory::getSortOrder);

        Page<ReportCategory> resultPage = this.page(page, wrapper);
        
        // 转换为 DTO
        List<ReportCategoryDTO> dtoList = resultPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        Page<ReportCategoryDTO> dtoPage = new Page<>(param.getPageNum(), param.getPageSize(), resultPage.getTotal());
        dtoPage.setRecords(dtoList);
        
        return dtoPage;
    }

    /**
     * 查询所有分类列表
     * <p>
     * 按排序号升序排列
     * </p>
     *
     * @return 分类列表
     */
    @Override
    public List<ReportCategoryDTO> listAll() {
        List<ReportCategory> list = this.list(new LambdaQueryWrapper<ReportCategory>()
                .orderByAsc(ReportCategory::getSortOrder));
        return list.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * 根据 ID 获取分类详情
     *
     * @param id 分类 ID
     * @return 分类 DTO
     * @throws BusinessException 当分类不存在时抛出
     */
    @Override
    public ReportCategoryDTO getById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("分类 ID 不能为空");
        }

        ReportCategory category = super.getById(id);
        if (category == null) {
            throw new BusinessException("报表分类不存在");
        }

        return convertToDTO(category);
    }

    /**
     * 保存分类
     * <p>
     * 支持新增和更新，根据 ID 判断
     * </p>
     *
     * @param dto 分类 DTO
     * @return 保存后的分类 DTO
     * @throws IllegalArgumentException 当参数为空时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReportCategoryDTO save(ReportCategoryDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("分类数据不能为空");
        }

        // 校验编码唯一性
        if (StrUtil.isNotBlank(dto.getCode())) {
            long count = this.count(new LambdaQueryWrapper<ReportCategory>()
                    .eq(ReportCategory::getCode, dto.getCode())
                    .ne(dto.getId() != null, ReportCategory::getId, dto.getId()));
            if (count > 0) {
                throw new BusinessException("分类编码已存在");
            }
        }

        // 转换为实体
        ReportCategory entity = new ReportCategory();
        BeanUtils.copyProperties(dto, entity);

        // 保存
        this.saveOrUpdate(entity);

        // 返回 DTO
        return convertToDTO(entity);
    }

    /**
     * 批量删除分类
     *
     * @param ids 分类 ID 列表
     * @throws IllegalArgumentException 当 ID 列表为空时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            throw new IllegalArgumentException("删除的 ID 列表不能为空");
        }

        // 检查是否有子分类
        for (Long id : ids) {
            long count = this.count(new LambdaQueryWrapper<ReportCategory>()
                    .eq(ReportCategory::getParentId, id));
            if (count > 0) {
                throw new BusinessException("存在子分类，无法删除");
            }
        }

        this.removeByIds(ids);
    }

    /**
     * 将实体转换为 DTO
     *
     * @param entity 实体对象
     * @return DTO 对象
     */
    private ReportCategoryDTO convertToDTO(ReportCategory entity) {
        if (entity == null) {
            return null;
        }

        ReportCategoryDTO dto = new ReportCategoryDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
