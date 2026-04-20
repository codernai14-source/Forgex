package com.forgex.basic.supplier.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.basic.supplier.domain.dto.SupplierDTO;
import com.forgex.basic.supplier.domain.entity.BasicSupplier;
import com.forgex.basic.supplier.mapper.BasicSupplierMapper;
import com.forgex.basic.supplier.service.ISupplierService;
import com.forgex.basic.supplier.domain.param.SupplierPageParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierServiceImpl extends ServiceImpl<BasicSupplierMapper, BasicSupplier> implements ISupplierService {

    private final BasicSupplierMapper supplierMapper;

    /**
     * 分页查询供应商列表
     *
     * @param param 查询参数
     * @return 供应商分页列表
     */
    @Override
    public Page<SupplierDTO> page(SupplierPageParam param) {
        Page<BasicSupplier> entityPage = new Page<>(param.getPageNum(), param.getPageSize());

        LambdaQueryWrapper<BasicSupplier> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BasicSupplier::getDeleted, 0);

        if (StringUtils.hasText(param.getSupplierCode())) {
            wrapper.like(BasicSupplier::getSupplierCode, param.getSupplierCode());
        }
        if (StringUtils.hasText(param.getSupplierName())) {
            wrapper.like(BasicSupplier::getSupplierName, param.getSupplierName());
        }
        if (StringUtils.hasText(param.getSupplierType())) {
            wrapper.eq(BasicSupplier::getSupplierType, param.getSupplierType());
        }
        if (param.getStatus() != null) {
            wrapper.eq(BasicSupplier::getStatus, param.getStatus());
        }

        Page<BasicSupplier> supplierPage = supplierMapper.selectPage(entityPage, wrapper);

        Page<SupplierDTO> dtoPage = new Page<>(supplierPage.getCurrent(), supplierPage.getSize(), supplierPage.getTotal());
        List<SupplierDTO> dtoList = supplierPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        dtoPage.setRecords(dtoList);

        return dtoPage;
    }

    /**
     * 查询供应商列表（不分页）
     *
     * @param param 查询参数
     * @return 供应商列表
     */
    @Override
    public List<SupplierDTO> list(SupplierPageParam param) {
        LambdaQueryWrapper<BasicSupplier> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BasicSupplier::getDeleted, 0);

        if (param != null) {
            if (StringUtils.hasText(param.getSupplierCode())) {
                wrapper.like(BasicSupplier::getSupplierCode, param.getSupplierCode());
            }
            if (StringUtils.hasText(param.getSupplierName())) {
                wrapper.like(BasicSupplier::getSupplierName, param.getSupplierName());
            }
            if (StringUtils.hasText(param.getSupplierType())) {
                wrapper.eq(BasicSupplier::getSupplierType, param.getSupplierType());
            }
            if (param.getStatus() != null) {
                wrapper.eq(BasicSupplier::getStatus, param.getStatus());
            }
        }

        List<BasicSupplier> supplierList = supplierMapper.selectList(wrapper);
        return supplierList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 根据 ID 获取供应商详情
     *
     * @param id 供应商 ID
     * @return 供应商详情
     */
    @Override
    public SupplierDTO getDetailById(Long id) {
        BasicSupplier supplier = supplierMapper.selectById(id);
        if (supplier == null) {
            return null;
        }
        return convertToDTO(supplier);
    }

    /**
     * 转换为 SupplierDTO
     */
    private SupplierDTO convertToDTO(BasicSupplier supplier) {
        SupplierDTO dto = new SupplierDTO();
        BeanUtils.copyProperties(supplier, dto);
        return dto;
    }
}
