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
import com.forgex.sys.domain.dto.department.SysDepartmentDTO;
import com.forgex.sys.domain.dto.department.SysDepartmentQueryDTO;
import com.forgex.sys.domain.dto.department.SysDepartmentSaveParam;
import com.forgex.sys.domain.entity.SysDepartment;
import com.forgex.sys.mapper.SysDepartmentMapper;
import com.forgex.sys.service.SysDepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 部门Service实现
 * 
 * 提供部门管理的业务逻辑实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysDepartmentServiceImpl implements SysDepartmentService {
    
    private final SysDepartmentMapper departmentMapper;
    
    @Override
    public List<SysDepartmentDTO> getDepartmentTree(Long tenantId) {
        // 查询所有部门
        LambdaQueryWrapper<SysDepartment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDepartment::getDeleted, false)
               .orderByAsc(SysDepartment::getOrderNum);
        
        List<SysDepartment> departments = departmentMapper.selectList(wrapper);
        
        // 转换为DTO
        List<SysDepartmentDTO> dtoList = departments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        // 构建树形结构
        return buildTree(dtoList);
    }
    
    @Override
    public List<SysDepartmentDTO> list(SysDepartmentQueryDTO queryDTO) {
        LambdaQueryWrapper<SysDepartment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDepartment::getDeleted, false);
        
        // 父部门ID
        if (queryDTO.getParentId() != null) {
            wrapper.eq(SysDepartment::getParentId, queryDTO.getParentId());
        }
        
        // 部门名称模糊查询
        if (StringUtils.hasText(queryDTO.getDeptName())) {
            wrapper.like(SysDepartment::getDeptName, queryDTO.getDeptName());
        }
        
        // 部门编码模糊查询
        if (StringUtils.hasText(queryDTO.getDeptCode())) {
            wrapper.like(SysDepartment::getDeptCode, queryDTO.getDeptCode());
        }
        
        // 组织类型
        if (StringUtils.hasText(queryDTO.getOrgType())) {
            wrapper.eq(SysDepartment::getOrgType, queryDTO.getOrgType());
        }
        
        // 状态
        if (queryDTO.getStatus() != null) {
            wrapper.eq(SysDepartment::getStatus, queryDTO.getStatus());
        }
        
        wrapper.orderByAsc(SysDepartment::getOrderNum);
        
        List<SysDepartment> departments = departmentMapper.selectList(wrapper);
        
        return departments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public SysDepartmentDTO getById(Long id, Long tenantId) {
        LambdaQueryWrapper<SysDepartment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDepartment::getId, id)
               .eq(SysDepartment::getDeleted, false);
        
        SysDepartment department = departmentMapper.selectOne(wrapper);
        
        if (department == null) {
            return null;
        }
        
        return convertToDTO(department);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(SysDepartmentSaveParam param) {
        // 检查部门编码是否重复
        LambdaQueryWrapper<SysDepartment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDepartment::getDeptCode, param.getDeptCode())
               .eq(SysDepartment::getDeleted, false);
        
        Long count = departmentMapper.selectCount(wrapper);
        if (count > 0) {
            throw new RuntimeException("部门编码已存在");
        }
        
        // 创建部门
        SysDepartment department = new SysDepartment();
        BeanUtils.copyProperties(param, department);
        
        // 设置默认值
        if (department.getParentId() == null) {
            department.setParentId(0L);
        }
        if (department.getOrderNum() == null) {
            department.setOrderNum(0);
        }
        if (department.getStatus() == null) {
            department.setStatus(true);
        }
        
        departmentMapper.insert(department);
        
        log.info("创建部门成功，部门ID：{}，部门名称：{}", department.getId(), department.getDeptName());
        
        return department.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(SysDepartmentSaveParam param) {
        if (param.getId() == null) {
            throw new RuntimeException("部门ID不能为空");
        }
        
        // 检查部门是否存在
        SysDepartment existDept = departmentMapper.selectById(param.getId());
        if (existDept == null || existDept.getDeleted()) {
            throw new RuntimeException("部门不存在");
        }
        
        // 检查部门编码是否重复（排除自己）
        LambdaQueryWrapper<SysDepartment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDepartment::getDeptCode, param.getDeptCode())
               .eq(SysDepartment::getDeleted, false)
               .ne(SysDepartment::getId, param.getId());
        
        Long count = departmentMapper.selectCount(wrapper);
        if (count > 0) {
            throw new RuntimeException("部门编码已存在");
        }
        
        // 检查是否将父部门设置为自己或自己的子部门
        if (param.getParentId() != null && param.getParentId().equals(param.getId())) {
            throw new RuntimeException("不能将父部门设置为自己");
        }
        
        // 更新部门
        SysDepartment department = new SysDepartment();
        BeanUtils.copyProperties(param, department);
        
        int rows = departmentMapper.updateById(department);
        
        log.info("更新部门成功，部门ID：{}，部门名称：{}", department.getId(), department.getDeptName());
        
        return rows > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id, Long tenantId) {
        // 检查是否有子部门
        LambdaQueryWrapper<SysDepartment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDepartment::getParentId, id)
               .eq(SysDepartment::getDeleted, false);
        
        Long count = departmentMapper.selectCount(wrapper);
        if (count > 0) {
            throw new RuntimeException("该部门下存在子部门，无法删除");
        }
        
        // TODO: 检查是否有用户关联
        
        // 逻辑删除
        SysDepartment department = new SysDepartment();
        department.setId(id);
        department.setDeleted(true);
        
        int rows = departmentMapper.updateById(department);
        
        log.info("删除部门成功，部门ID：{}", id);
        
        return rows > 0;
    }
    
    /**
     * 实体转DTO
     */
    private SysDepartmentDTO convertToDTO(SysDepartment department) {
        SysDepartmentDTO dto = new SysDepartmentDTO();
        BeanUtils.copyProperties(department, dto);
        return dto;
    }
    
    /**
     * 构建树形结构
     */
    private List<SysDepartmentDTO> buildTree(List<SysDepartmentDTO> dtoList) {
        // 按ID分组
        Map<Long, SysDepartmentDTO> dtoMap = dtoList.stream()
                .collect(Collectors.toMap(SysDepartmentDTO::getId, dto -> dto));
        
        // 构建树
        List<SysDepartmentDTO> tree = new ArrayList<>();
        
        for (SysDepartmentDTO dto : dtoList) {
            Long parentId = dto.getParentId();
            
            if (parentId == null || parentId == 0) {
                // 根节点
                tree.add(dto);
            } else {
                // 子节点
                SysDepartmentDTO parent = dtoMap.get(parentId);
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(dto);
                } else {
                    // 父节点不存在，作为根节点
                    tree.add(dto);
                }
            }
        }
        
        return tree;
    }
}
