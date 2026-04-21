package com.forgex.basic.factory.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.basic.factory.domain.dto.FactoryDTO;
import com.forgex.basic.factory.domain.entity.BasicFactory;
import com.forgex.basic.factory.domain.param.FactoryPageParam;

import java.util.List;

public interface FactoryService extends IService<BasicFactory> {

    /**
     * 分页查询工厂列表
     *
     * @param param 查询参数
     * @return 工厂分页列表
     */
    Page<FactoryDTO> page(FactoryPageParam param);

    /**
     * 查询工厂列表（不分页）
     *
     * @param param 查询参数
     * @return 工厂列表
     */
    List<FactoryDTO> list(FactoryPageParam param);

    /**
     * 根据 ID 获取工厂详情
     *
     * @param id 工厂 ID
     * @return 工厂详情
     */
    FactoryDTO getDetailById(Long id);
}
