package com.forgex.basic.supplier.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.basic.supplier.domain.dto.SupplierDTO;
import com.forgex.basic.supplier.domain.entity.BasicSupplier;
import com.forgex.basic.supplier.domain.param.SupplierPageParam;

import java.util.List;

public interface ISupplierService extends IService<BasicSupplier> {

    /**
     * 分页查询供应商列表
     *
     * @param param 查询参数
     * @return 供应商分页列表
     */
    Page<SupplierDTO> page(SupplierPageParam param);

    /**
     * 查询供应商列表（不分页）
     *
     * @param param 查询参数
     * @return 供应商列表
     */
    List<SupplierDTO> list(SupplierPageParam param);

    /**
     * 根据 ID 获取供应商详情
     *
     * @param id 供应商 ID
     * @return 供应商详情
     */
    SupplierDTO getDetailById(Long id);
}
