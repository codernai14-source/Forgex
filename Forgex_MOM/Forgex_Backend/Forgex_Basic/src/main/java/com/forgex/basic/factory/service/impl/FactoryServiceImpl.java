package com.forgex.basic.factory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.basic.factory.domain.dto.FactoryDTO;
import com.forgex.basic.factory.domain.entity.BasicFactory;
import com.forgex.basic.factory.mapper.BasicFactoryMapper;
import com.forgex.basic.factory.service.FactoryService;
import com.forgex.basic.factory.domain.param.FactoryPageParam;
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
public class FactoryServiceImpl extends ServiceImpl<BasicFactoryMapper, BasicFactory> implements FactoryService {

    private final BasicFactoryMapper factoryMapper;

    /**
     * 分页查询工厂列表
     *
     * @param param 查询参数
     * @return 工厂分页列表
     */
    @Override
    public Page<FactoryDTO> page(FactoryPageParam param) {
        Page<BasicFactory> entityPage = new Page<>(param.getPageNum(), param.getPageSize());

        LambdaQueryWrapper<BasicFactory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BasicFactory::getDeleted, 0);

        if (StringUtils.hasText(param.getFactoryCode())) {
            wrapper.like(BasicFactory::getFactoryCode, param.getFactoryCode());
        }
        if (StringUtils.hasText(param.getFactoryName())) {
            wrapper.like(BasicFactory::getFactoryName, param.getFactoryName());
        }
        if (StringUtils.hasText(param.getFactoryType())) {
            wrapper.eq(BasicFactory::getFactoryType, param.getFactoryType());
        }
        if (param.getStatus() != null) {
            wrapper.eq(BasicFactory::getStatus, param.getStatus());
        }

        Page<BasicFactory> factoryPage = factoryMapper.selectPage(entityPage, wrapper);

        Page<FactoryDTO> dtoPage = new Page<>(factoryPage.getCurrent(), factoryPage.getSize(), factoryPage.getTotal());
        List<FactoryDTO> dtoList = factoryPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        dtoPage.setRecords(dtoList);

        return dtoPage;
    }

    /**
     * 查询工厂列表（不分页）
     *
     * @param param 查询参数
     * @return 工厂列表
     */
    @Override
    public List<FactoryDTO> list(FactoryPageParam param) {
        LambdaQueryWrapper<BasicFactory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BasicFactory::getDeleted, 0);

        if (param != null) {
            if (StringUtils.hasText(param.getFactoryCode())) {
                wrapper.like(BasicFactory::getFactoryCode, param.getFactoryCode());
            }
            if (StringUtils.hasText(param.getFactoryName())) {
                wrapper.like(BasicFactory::getFactoryName, param.getFactoryName());
            }
            if (StringUtils.hasText(param.getFactoryType())) {
                wrapper.eq(BasicFactory::getFactoryType, param.getFactoryType());
            }
            if (param.getStatus() != null) {
                wrapper.eq(BasicFactory::getStatus, param.getStatus());
            }
        }

        List<BasicFactory> factoryList = factoryMapper.selectList(wrapper);
        return factoryList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 根据 ID 获取工厂详情
     *
     * @param id 工厂 ID
     * @return 工厂详情
     */
    @Override
    public FactoryDTO getDetailById(Long id) {
        BasicFactory factory = factoryMapper.selectById(id);
        if (factory == null) {
            return null;
        }
        return convertToDTO(factory);
    }

    /**
     * 转换为 FactoryDTO
     */
    private FactoryDTO convertToDTO(BasicFactory factory) {
        FactoryDTO dto = new FactoryDTO();
        BeanUtils.copyProperties(factory, dto);
        return dto;
    }
}
