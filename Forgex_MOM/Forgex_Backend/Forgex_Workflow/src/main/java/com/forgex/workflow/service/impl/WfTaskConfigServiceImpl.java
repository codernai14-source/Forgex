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
package com.forgex.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.workflow.domain.dto.WfTaskConfigDTO;
import com.forgex.workflow.domain.entity.WfTaskConfig;
import com.forgex.workflow.domain.param.WfTaskConfigQueryParam;
import com.forgex.workflow.domain.param.WfTaskConfigSaveParam;
import com.forgex.workflow.mapper.WfTaskConfigMapper;
import com.forgex.workflow.service.IWfTaskConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 审批任务配置Service实现。
 * <p>
 * 提供审批任务配置的业务逻辑实现，包含审批任务的增删改查功能。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 * @see com.forgex.workflow.service.IWfTaskConfigService
 * @see com.forgex.workflow.domain.entity.WfTaskConfig
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WfTaskConfigServiceImpl implements IWfTaskConfigService {
    
    private final WfTaskConfigMapper taskConfigMapper;
    
    @Override
    public Page<WfTaskConfigDTO> page(WfTaskConfigQueryParam param) {
        // 构建分页对象
        Page<WfTaskConfig> page = new Page<>(param.getPageNum(), param.getPageSize());
        
        // 构建查询条件
        LambdaQueryWrapper<WfTaskConfig> wrapper = buildQueryWrapper(param);
        wrapper.orderByDesc(WfTaskConfig::getCreateTime);
        
        // 执行分页查询
        Page<WfTaskConfig> configPage = taskConfigMapper.selectPage(page, wrapper);
        
        // 转换为DTO
        Page<WfTaskConfigDTO> dtoPage = new Page<>();
        dtoPage.setCurrent(configPage.getCurrent());
        dtoPage.setSize(configPage.getSize());
        dtoPage.setTotal(configPage.getTotal());
        dtoPage.setRecords(
            configPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList())
        );
        
        return dtoPage;
    }
    
    @Override
    public List<WfTaskConfigDTO> list(WfTaskConfigQueryParam param) {
        LambdaQueryWrapper<WfTaskConfig> wrapper = buildQueryWrapper(param);
        wrapper.orderByDesc(WfTaskConfig::getCreateTime);
        
        List<WfTaskConfig> configs = taskConfigMapper.selectList(wrapper);
        
        return configs.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 构建查询条件
     * 
     * @param param 查询参数
     * @return 查询条件包装器
     */
    private LambdaQueryWrapper<WfTaskConfig> buildQueryWrapper(WfTaskConfigQueryParam param) {
        LambdaQueryWrapper<WfTaskConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WfTaskConfig::getDeleted, false);
        
        // 任务名称模糊查询
        if (StringUtils.hasText(param.getTaskName())) {
            wrapper.like(WfTaskConfig::getTaskName, param.getTaskName());
        }
        
        // 任务编码模糊查询
        if (StringUtils.hasText(param.getTaskCode())) {
            wrapper.like(WfTaskConfig::getTaskCode, param.getTaskCode());
        }
        
        // 状态
        if (param.getStatus() != null) {
            wrapper.eq(WfTaskConfig::getStatus, param.getStatus());
        }
        
        return wrapper;
    }
    
    @Override
    public WfTaskConfigDTO getById(Long id) {
        LambdaQueryWrapper<WfTaskConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WfTaskConfig::getId, id)
               .eq(WfTaskConfig::getDeleted, false);
        
        WfTaskConfig config = taskConfigMapper.selectOne(wrapper);
        
        if (config == null) {
            return null;
        }
        
        return convertToDTO(config);
    }
    
    @Override
    public WfTaskConfigDTO getByCode(String taskCode) {
        LambdaQueryWrapper<WfTaskConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WfTaskConfig::getTaskCode, taskCode)
               .eq(WfTaskConfig::getDeleted, false)
               .last("LIMIT 1");
        
        WfTaskConfig config = taskConfigMapper.selectOne(wrapper);
        
        if (config == null) {
            return null;
        }
        
        return convertToDTO(config);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(WfTaskConfigSaveParam param) {
        // 检查任务编码是否重复
        LambdaQueryWrapper<WfTaskConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WfTaskConfig::getTaskCode, param.getTaskCode())
               .eq(WfTaskConfig::getDeleted, false);
        
        Long count = taskConfigMapper.selectCount(wrapper);
        if (count > 0) {
            throw new RuntimeException("任务编码已存在");
        }
        
        // 创建配置
        WfTaskConfig config = new WfTaskConfig();
        BeanUtils.copyProperties(param, config);
        
        // 设置默认值
        if (config.getStatus() == null) {
            config.setStatus(1);
        }
        if (config.getVersion() == null) {
            config.setVersion(1);
        }
        
        taskConfigMapper.insert(config);
        
        log.info("创建审批任务配置成功，ID：{}，名称：{}，编码：{}", 
                config.getId(), config.getTaskName(), config.getTaskCode());
        
        return config.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(WfTaskConfigSaveParam param) {
        if (param.getId() == null) {
            throw new RuntimeException("配置ID不能为空");
        }
        
        // 检查配置是否存在
        WfTaskConfig existConfig = taskConfigMapper.selectById(param.getId());
        if (existConfig == null || existConfig.getDeleted()) {
            throw new RuntimeException("配置不存在");
        }
        
        // 检查任务编码是否重复（排除自己）
        LambdaQueryWrapper<WfTaskConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WfTaskConfig::getTaskCode, param.getTaskCode())
               .eq(WfTaskConfig::getDeleted, false)
               .ne(WfTaskConfig::getId, param.getId());
        
        Long count = taskConfigMapper.selectCount(wrapper);
        if (count > 0) {
            throw new RuntimeException("任务编码已存在");
        }
        
        // 更新配置
        WfTaskConfig config = new WfTaskConfig();
        BeanUtils.copyProperties(param, config);
        
        int rows = taskConfigMapper.updateById(config);
        
        log.info("更新审批任务配置成功，ID：{}，名称：{}", config.getId(), config.getTaskName());
        
        return rows > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        // 检查配置是否存在
        WfTaskConfig existConfig = taskConfigMapper.selectById(id);
        if (existConfig == null || existConfig.getDeleted()) {
            throw new RuntimeException("配置不存在");
        }
        
        // 逻辑删除
        WfTaskConfig config = new WfTaskConfig();
        config.setId(id);
        config.setDeleted(true);
        
        int rows = taskConfigMapper.updateById(config);
        
        log.info("删除审批任务配置成功，ID：{}", id);
        
        return rows > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateStatus(Long id, Integer status) {
        // 检查配置是否存在
        WfTaskConfig existConfig = taskConfigMapper.selectById(id);
        if (existConfig == null || existConfig.getDeleted()) {
            throw new RuntimeException("配置不存在");
        }
        
        // 更新状态
        WfTaskConfig config = new WfTaskConfig();
        config.setId(id);
        config.setStatus(status);
        
        int rows = taskConfigMapper.updateById(config);
        
        log.info("更新审批任务配置状态成功，ID：{}，状态：{}", id, status);
        
        return rows > 0;
    }
    
    /**
     * 实体转DTO
     * 
     * @param config 配置实体
     * @return 配置DTO
     */
    private WfTaskConfigDTO convertToDTO(WfTaskConfig config) {
        WfTaskConfigDTO dto = new WfTaskConfigDTO();
        BeanUtils.copyProperties(config, dto);
        return dto;
    }
}