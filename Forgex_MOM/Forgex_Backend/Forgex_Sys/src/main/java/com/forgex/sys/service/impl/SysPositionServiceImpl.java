/*Copyright 2026 coder_nai@163.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package com.forgex.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.sys.domain.dto.position.SysPositionDTO;
import com.forgex.sys.domain.dto.position.SysPositionQueryDTO;
import com.forgex.sys.domain.dto.position.SysPositionSaveParam;
import com.forgex.sys.domain.entity.SysPosition;
import com.forgex.sys.mapper.SysPositionMapper;
import com.forgex.sys.service.SysPositionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 职位Service实现
 * 
 * 提供职位管理的业务逻辑实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysPositionServiceImpl implements SysPositionService {
    
    private final SysPositionMapper positionMapper;
    
    @Override
    public List<SysPositionDTO> list(SysPositionQueryDTO queryDTO) {
        LambdaQueryWrapper<SysPosition> wrapper = buildQueryWrapper(queryDTO);
        
        List<SysPosition> positions = positionMapper.selectList(wrapper);
        
        return positions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 分页查询职位列表
     *
     * @param page     分页参数
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    @Override
    public IPage<SysPositionDTO> pagePositions(Page<SysPosition> page, SysPositionQueryDTO queryDTO) {
        LambdaQueryWrapper<SysPosition> wrapper = buildQueryWrapper(queryDTO);
        IPage<SysPosition> positionPage = positionMapper.selectPage(page, wrapper);
        return positionPage.convert(this::convertToDTO);
    }
    
    @Override
    public SysPositionDTO getById(Long id, Long tenantId) {
        LambdaQueryWrapper<SysPosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysPosition::getId, id)
               .eq(SysPosition::getDeleted, false);
        
        if (tenantId != null) {
            wrapper.eq(SysPosition::getTenantId, tenantId);
        }
        
        SysPosition position = positionMapper.selectOne(wrapper);
        
        if (position == null) {
            return null;
        }
        
        return convertToDTO(position);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(SysPositionSaveParam param) {
        // 检查职位编码是否重复
        LambdaQueryWrapper<SysPosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysPosition::getPositionCode, param.getPositionCode())
               .eq(SysPosition::getDeleted, false);
        
        Long count = positionMapper.selectCount(wrapper);
        if (count > 0) {
            throw new RuntimeException("职位编码已存在");
        }
        
        // 创建职位
        SysPosition position = new SysPosition();
        BeanUtils.copyProperties(param, position);
        
        // 设置默认值
        if (position.getOrderNum() == null) {
            position.setOrderNum(0);
        }
        if (position.getStatus() == null) {
            position.setStatus(true);
        }
        
        positionMapper.insert(position);
        
        log.info("创建职位成功，职位ID：{}，职位名称：{}", position.getId(), position.getPositionName());
        
        return position.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(SysPositionSaveParam param) {
        if (param.getId() == null) {
            throw new RuntimeException("职位ID不能为空");
        }
        
        // 检查职位是否存在
        SysPosition existPosition = positionMapper.selectById(param.getId());
        if (existPosition == null || existPosition.getDeleted()) {
            throw new RuntimeException("职位不存在");
        }
        
        // 检查职位编码是否重复（排除自己）
        LambdaQueryWrapper<SysPosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysPosition::getPositionCode, param.getPositionCode())
               .eq(SysPosition::getDeleted, false)
               .ne(SysPosition::getId, param.getId());
        
        Long count = positionMapper.selectCount(wrapper);
        if (count > 0) {
            throw new RuntimeException("职位编码已存在");
        }
        
        // 更新职位
        SysPosition position = new SysPosition();
        BeanUtils.copyProperties(param, position);
        
        int rows = positionMapper.updateById(position);
        
        log.info("更新职位成功，职位ID：{}，职位名称：{}", position.getId(), position.getPositionName());
        
        return rows > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id, Long tenantId) {
        // TODO: 检查是否有用户关联
        
        // 逻辑删除
        SysPosition position = new SysPosition();
        position.setId(id);
        position.setDeleted(true);
        
        int rows = positionMapper.updateById(position);
        
        log.info("删除职位成功，职位ID：{}", id);
        
        return rows > 0;
    }
    
    /**
     * 实体转DTO
     */
    private SysPositionDTO convertToDTO(SysPosition position) {
        SysPositionDTO dto = new SysPositionDTO();
        BeanUtils.copyProperties(position, dto);
        return dto;
    }
    
    /**
     * 构建职位查询条件
     *
     * @param queryDTO 查询参数
     * @return 查询条件
     */
    private LambdaQueryWrapper<SysPosition> buildQueryWrapper(SysPositionQueryDTO queryDTO) {
        LambdaQueryWrapper<SysPosition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysPosition::getDeleted, false);
        
        if (queryDTO.getTenantId() != null) {
            wrapper.eq(SysPosition::getTenantId, queryDTO.getTenantId());
        }
        
        // 职位名称模糊查询
        if (StringUtils.hasText(queryDTO.getPositionName())) {
            wrapper.like(SysPosition::getPositionName, queryDTO.getPositionName());
        }
        
        // 职位编码模糊查询
        if (StringUtils.hasText(queryDTO.getPositionCode())) {
            wrapper.like(SysPosition::getPositionCode, queryDTO.getPositionCode());
        }
        
        // 状态
        if (queryDTO.getStatus() != null) {
            wrapper.eq(SysPosition::getStatus, queryDTO.getStatus());
        }
        
        // 部门ID过滤
        if (queryDTO.getDepartmentId() != null) {
            wrapper.eq(SysPosition::getDepartmentId, queryDTO.getDepartmentId());
        }
        
        wrapper.orderByAsc(SysPosition::getOrderNum);
        
        return wrapper;
    }
}
