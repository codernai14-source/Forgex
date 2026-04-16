package com.forgex.integration.service.impl;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.web.StatusCode;
import com.forgex.integration.domain.dto.ApiParamConfigDTO;
import com.forgex.integration.enums.IntegrationPromptEnum;
import com.forgex.integration.domain.entity.ApiParamConfig;
import com.forgex.integration.domain.param.ApiParamConfigParam;
import com.forgex.integration.domain.vo.ApiParamTreeVO;
import com.forgex.integration.mapper.ApiParamConfigMapper;
import com.forgex.integration.service.IApiParamConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 接口参数配置服务实现类
 * <p>
 * 提供接口参数的树形结构管理、增删改查、JSON 导入解析等功能实现
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiParamConfigServiceImpl extends ServiceImpl<ApiParamConfigMapper, ApiParamConfig>
    implements IApiParamConfigService {

    private final ApiParamConfigMapper apiParamConfigMapper;
    
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<ApiParamTreeVO> listParamTree(Long apiConfigId, String direction) {
        if (apiConfigId == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.ID_REQUIRED);
        }
        
        // 1. 查询所有参数配置
        LambdaQueryWrapper<ApiParamConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiParamConfig::getApiConfigId, apiConfigId);
        wrapper.eq(StringUtils.hasText(direction), ApiParamConfig::getDirection, direction);
        wrapper.orderByAsc(ApiParamConfig::getOrderNum, ApiParamConfig::getId);
        
        List<ApiParamConfig> allConfigs = this.list(wrapper);
        
        // 2. 构建父子关系树
        return buildTree(allConfigs, null);
    }

    @Override
    public List<ApiParamConfigDTO> listChildren(ApiParamConfigParam param) {
        if (param.getApiConfigId() == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.ID_REQUIRED);
        }
        
        // 1. 根据父节点 ID 查询子节点
        List<ApiParamConfig> configs = apiParamConfigMapper.listChildrenByParentId(
            param.getApiConfigId(),
            param.getParentId()
        );
        
        // 2. 转换为 DTO
        return configs.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    @Override
    public ApiParamConfigDTO getById(Long id) {
        if (id == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.ID_REQUIRED);
        }
        
        ApiParamConfig config = this.baseMapper.selectById(id);
        if (config == null || config.getDeleted()) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.PARAM_CONFIG_NOT_FOUND);
        }
        
        return convertToDTO(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(ApiParamConfigDTO dto) {
        // 1. 参数校验
        if (dto.getApiConfigId() == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.ID_REQUIRED);
        }
        if (!StringUtils.hasText(dto.getFieldName())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.PARAM_FIELD_NAME_REQUIRED);
        }
        if (!StringUtils.hasText(dto.getNodeType())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.PARAM_NODE_TYPE_REQUIRED);
        }
        
        // 2. 构建实体
        ApiParamConfig config = convertToEntity(dto);
        config.setCreateBy(getCurrentUsername());
        config.setUpdateBy(getCurrentUsername());
        
        // 3. 计算字段路径
        if (dto.getParentId() != null) {
            ApiParamConfig parent = this.baseMapper.selectById(dto.getParentId());
            if (parent == null) {
                throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.PARAM_PARENT_NOT_FOUND);
            }
            // 子节点路径 = 父节点路径。字段名
            String fieldPath = StringUtils.hasText(parent.getFieldPath()) 
                ? parent.getFieldPath() + "." + dto.getFieldName()
                : dto.getFieldName();
            config.setFieldPath(fieldPath);
        } else {
            // 根节点路径 = 字段名
            config.setFieldPath(dto.getFieldName());
        }
        
        // 4. 计算排序号
        if (config.getOrderNum() == null) {
            config.setOrderNum(getNextOrderNum(dto.getApiConfigId(), dto.getParentId()));
        }
        
        // 5. 插入数据库
        boolean success = this.save(config);
        if (!success) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.PARAM_CONFIG_CREATE_FAILED);
        }
        
        log.info("创建参数配置成功：configId={}, fieldName={}", config.getId(), config.getFieldName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ApiParamConfigDTO dto) {
        if (dto.getId() == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.ID_REQUIRED);
        }
        
        // 1. 检查参数配置是否存在
        ApiParamConfig existing = this.baseMapper.selectById(dto.getId());
        if (existing == null || existing.getDeleted()) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.PARAM_CONFIG_NOT_FOUND);
        }
        
        // 2. 参数校验
        if (!StringUtils.hasText(dto.getFieldName())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.PARAM_FIELD_NAME_REQUIRED);
        }
        if (!StringUtils.hasText(dto.getNodeType())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.PARAM_NODE_TYPE_REQUIRED);
        }
        
        // 3. 构建更新实体
        ApiParamConfig config = convertToEntity(dto);
        config.setUpdateTime(LocalDateTime.now());
        config.setUpdateBy(getCurrentUsername());
        
        // 4. 重新计算字段路径
        if (dto.getParentId() != null) {
            ApiParamConfig parent = this.baseMapper.selectById(dto.getParentId());
            if (parent == null) {
                throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.PARAM_PARENT_NOT_FOUND);
            }
            String fieldPath = StringUtils.hasText(parent.getFieldPath())
                ? parent.getFieldPath() + "." + dto.getFieldName()
                : dto.getFieldName();
            config.setFieldPath(fieldPath);
        } else {
            config.setFieldPath(dto.getFieldName());
        }
        
        // 5. 更新数据库
        boolean success = this.updateById(config);
        if (!success) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.PARAM_CONFIG_UPDATE_FAILED);
        }
        
        // 6. 递归更新所有子节点的路径
        updateChildrenFieldPath(dto.getId(), config.getFieldPath());
        
        log.info("更新参数配置成功：configId={}, fieldName={}", config.getId(), config.getFieldName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (id == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.ID_REQUIRED);
        }
        
        // 1. 检查参数配置是否存在
        ApiParamConfig config = this.baseMapper.selectById(id);
        if (config == null || config.getDeleted()) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.PARAM_CONFIG_NOT_FOUND);
        }
        
        // 2. 级联删除所有子节点
        deleteChildren(id);
        
        // 3. 逻辑删除当前节点
        config.setDeleted(true);
        config.setUpdateTime(LocalDateTime.now());
        config.setUpdateBy(getCurrentUsername());
        
        boolean success = this.updateById(config);
        if (!success) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.PARAM_CONFIG_DELETE_FAILED);
        }
        
        log.info("删除参数配置成功：configId={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.DELETE_IDS_REQUIRED);
        }
        
        for (Long id : ids) {
            try {
                delete(id);
            } catch (I18nBusinessException e) {
                log.warn("删除参数配置失败：ID={}, 原因：{}", id, e.getMessage());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importFromJson(Long apiConfigId, String direction, String jsonString) {
        if (apiConfigId == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.ID_REQUIRED);
        }
        if (!StringUtils.hasText(jsonString)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.PARAM_CONFIG_NOT_FOUND);
        }
        
        // 1. 解析 JSON 为树形结构
        List<ApiParamTreeVO> treeVOs = parseJsonToTree(jsonString);
        
        // 2. 递归保存到数据库
        for (ApiParamTreeVO treeVO : treeVOs) {
            saveTreeFromJson(apiConfigId, direction, treeVO, null);
        }
        
        log.info("从 JSON 导入参数配置成功：apiConfigId={}, direction={}, 节点数={}", 
                 apiConfigId, direction, treeVOs.size());
    }

    @Override
    public List<ApiParamTreeVO> parseJsonToTree(String jsonString) {
        if (!StringUtils.hasText(jsonString)) {
            return Collections.emptyList();
        }
        
        try {
            // 1. 解析 JSON
            JsonNode jsonNode = objectMapper.readTree(jsonString);
            
            // 2. 递归构建 VO 树
            return convertJsonToTree(jsonNode, null, null);
            
        } catch (JsonProcessingException e) {
            log.error("解析 JSON 失败", e);
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.PARAM_JSON_PARSE_FAILED, e.getMessage());
        }
    }

    @Override
    public ApiParamConfigDTO getByFieldPath(Long apiConfigId, String fieldPath, String direction) {
        if (apiConfigId == null || !StringUtils.hasText(fieldPath)) {
            return null;
        }
        
        ApiParamConfig config = apiParamConfigMapper.getByFieldPath(apiConfigId, fieldPath, direction);
        return config != null ? convertToDTO(config) : null;
    }

    /**
     * 构建树形结构
     * <p>
     * 根据 parentId 将扁平列表转换为树形结构
     * </p>
     *
     * @param allConfigs 所有参数配置列表
     * @param parentId 父节点 ID（null 表示根节点）
     * @return 树形结构 VO 列表
     */
    private List<ApiParamTreeVO> buildTree(List<ApiParamConfig> allConfigs, Long parentId) {
        return allConfigs.stream()
            .filter(config -> {
                // 筛选出当前父节点的子节点
                if (parentId == null) {
                    return config.getParentId() == null;
                } else {
                    return parentId.equals(config.getParentId());
                }
            })
            .map(config -> {
                // 转换为 VO
                ApiParamTreeVO vo = convertToTreeVO(config);
                // 递归构建子节点
                List<ApiParamTreeVO> children = buildTree(allConfigs, config.getId());
                vo.setChildren(children);
                return vo;
            })
            .collect(Collectors.toList());
    }

    /**
     * 递归更新子节点的字段路径
     * <p>
     * 当父节点的字段名改变时，需要更新所有子节点的路径
     * </p>
     *
     * @param parentId 父节点 ID
     * @param parentFieldPath 父节点的新路径
     */
    private void updateChildrenFieldPath(Long parentId, String parentFieldPath) {
        // 1. 查询所有子节点
        LambdaQueryWrapper<ApiParamConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiParamConfig::getParentId, parentId);
        List<ApiParamConfig> children = this.list(wrapper);
        
        if (children.isEmpty()) {
            return;
        }
        
        // 2. 更新每个子节点的路径
        for (ApiParamConfig child : children) {
            // 提取子节点的字段名（去掉父路径部分）
            String childFieldName = child.getFieldName();
            String newFieldPath = parentFieldPath + "." + childFieldName;
            
            // 更新子节点路径
            child.setFieldPath(newFieldPath);
            child.setUpdateTime(LocalDateTime.now());
            child.setUpdateBy(getCurrentUsername());
            this.updateById(child);
            
            // 递归更新子节点的子节点
            updateChildrenFieldPath(child.getId(), newFieldPath);
        }
    }

    /**
     * 递归删除所有子节点
     * <p>
     * 级联删除指定节点的所有后代节点
     * </p>
     *
     * @param parentId 父节点 ID
     */
    private void deleteChildren(Long parentId) {
        // 1. 查询所有子节点
        LambdaQueryWrapper<ApiParamConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiParamConfig::getParentId, parentId);
        List<ApiParamConfig> children = this.list(wrapper);
        
        if (children.isEmpty()) {
            return;
        }
        
        // 2. 递归删除子节点的子节点
        for (ApiParamConfig child : children) {
            deleteChildren(child.getId());
            
            // 3. 逻辑删除子节点
            child.setDeleted(true);
            child.setUpdateTime(LocalDateTime.now());
            child.setUpdateBy(getCurrentUsername());
            this.updateById(child);
        }
    }

    /**
     * 从 JSON 递归保存树形结构
     * <p>
     * 将解析后的 JSON 树递归保存到数据库
     * </p>
     *
     * @param apiConfigId 接口配置 ID
     * @param direction 参数方向
     * @param treeVO 树形 VO
     * @param parentId 父节点 ID
     */
    private void saveTreeFromJson(Long apiConfigId, String direction, ApiParamTreeVO treeVO, Long parentId) {
        // 1. 转换为 DTO
        ApiParamConfigDTO dto = convertFromTreeVO(treeVO);
        dto.setApiConfigId(apiConfigId);
        dto.setDirection(direction);
        dto.setParentId(parentId);
        
        // 2. 创建节点
        create(dto);
        
        // 3. 递归处理子节点
        if (treeVO.getChildren() != null && !treeVO.getChildren().isEmpty()) {
            for (ApiParamTreeVO childVO : treeVO.getChildren()) {
                saveTreeFromJson(apiConfigId, direction, childVO, dto.getId());
            }
        }
    }

    /**
     * 将 JSON 节点转换为树形 VO
     * <p>
     * 递归解析 JSON 结构，推断节点类型和字段类型
     * </p>
     *
     * @param jsonNode JSON 节点
     * @param fieldName 字段名
     * @param parentPath 父节点路径
     * @return 树形 VO 列表
     */
    private List<ApiParamTreeVO> convertJsonToTree(JsonNode jsonNode, String fieldName, String parentPath) {
        List<ApiParamTreeVO> result = new ArrayList<>();
        
        if (jsonNode.isObject()) {
            // 对象类型
            ApiParamTreeVO vo = new ApiParamTreeVO();
            vo.setFieldName(fieldName != null ? fieldName : "root");
            vo.setNodeType("OBJECT");
            vo.setFieldType("object");
            vo.setFieldPath(fieldName != null 
                ? (StringUtils.hasText(parentPath) ? parentPath + "." + fieldName : fieldName)
                : "root");
            vo.setRequired(0);
            vo.setOrderNum(0);
            
            // 递归处理子字段
            List<ApiParamTreeVO> children = new ArrayList<>();
            jsonNode.fields().forEachRemaining(entry -> {
                String childFieldName = entry.getKey();
                JsonNode childNode = entry.getValue();
                List<ApiParamTreeVO> childTree = convertJsonToTree(childNode, childFieldName, vo.getFieldPath());
                children.addAll(childTree);
            });
            vo.setChildren(children);
            
            result.add(vo);
            
        } else if (jsonNode.isArray()) {
            // 数组类型
            ApiParamTreeVO vo = new ApiParamTreeVO();
            vo.setFieldName(fieldName != null ? fieldName : "array");
            vo.setNodeType("ARRAY");
            vo.setFieldType("array");
            vo.setFieldPath(fieldName != null
                ? (StringUtils.hasText(parentPath) ? parentPath + "." + fieldName : fieldName)
                : "array");
            vo.setRequired(0);
            vo.setOrderNum(0);
            
            // 处理数组元素（只处理第一个元素作为模板）
            if (jsonNode.size() > 0) {
                JsonNode firstElement = jsonNode.get(0);
                List<ApiParamTreeVO> children = convertJsonToTree(firstElement, "item", vo.getFieldPath());
                vo.setChildren(children);
            }
            
            result.add(vo);
            
        } else {
            // 字段类型
            ApiParamTreeVO vo = new ApiParamTreeVO();
            vo.setFieldName(fieldName);
            vo.setNodeType("FIELD");
            vo.setFieldType(getJsonFieldType(jsonNode));
            vo.setFieldPath(StringUtils.hasText(parentPath) ? parentPath + "." + fieldName : fieldName);
            vo.setRequired(0);
            vo.setOrderNum(0);
            
            // 设置默认值（如果是基本类型）
            if (!jsonNode.isNull()) {
                vo.setDefaultValue(jsonNode.asText());
            }
            
            result.add(vo);
        }
        
        return result;
    }

    /**
     * 获取 JSON 字段的类型
     * <p>
     * 根据 JSON 节点类型推断字段类型
     * </p>
     *
     * @param jsonNode JSON 节点
     * @return 字段类型（string, number, boolean 等）
     */
    private String getJsonFieldType(JsonNode jsonNode) {
        if (jsonNode.isTextual()) {
            return "string";
        } else if (jsonNode.isNumber()) {
            return "number";
        } else if (jsonNode.isBoolean()) {
            return "boolean";
        } else if (jsonNode.isNull()) {
            return "string";
        } else {
            return "string";
        }
    }

    /**
     * 获取下一个排序号
     * <p>
     * 根据父节点 ID 获取最大的排序号并 +1
     * </p>
     *
     * @param apiConfigId 接口配置 ID
     * @param parentId 父节点 ID
     * @return 下一个排序号
     */
    private Integer getNextOrderNum(Long apiConfigId, Long parentId) {
        LambdaQueryWrapper<ApiParamConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiParamConfig::getApiConfigId, apiConfigId);
        wrapper.eq(ApiParamConfig::getParentId, parentId);
        wrapper.select(ApiParamConfig::getOrderNum);
        wrapper.orderByDesc(ApiParamConfig::getOrderNum);
        wrapper.last("LIMIT 1");
        
        ApiParamConfig maxOrderConfig = this.getOne(wrapper);
        return maxOrderConfig != null && maxOrderConfig.getOrderNum() != null 
            ? maxOrderConfig.getOrderNum() + 1 
            : 1;
    }

    /**
     * DTO 转 Entity
     */
    private ApiParamConfig convertToEntity(ApiParamConfigDTO dto) {
        ApiParamConfig config = new ApiParamConfig();
        BeanUtils.copyProperties(dto, config);
        return config;
    }

    /**
     * Entity 转 DTO
     */
    private ApiParamConfigDTO convertToDTO(ApiParamConfig config) {
        ApiParamConfigDTO dto = new ApiParamConfigDTO();
        BeanUtils.copyProperties(config, dto);
        return dto;
    }

    /**
     * TreeVO 转 DTO
     */
    private ApiParamConfigDTO convertFromTreeVO(ApiParamTreeVO treeVO) {
        ApiParamConfigDTO dto = new ApiParamConfigDTO();
        BeanUtils.copyProperties(treeVO, dto);
        return dto;
    }

    /**
     * Entity 转 TreeVO
     */
    private ApiParamTreeVO convertToTreeVO(ApiParamConfig config) {
        ApiParamTreeVO vo = new ApiParamTreeVO();
        BeanUtils.copyProperties(config, vo);
        return vo;
    }

    /**
     * 获取当前登录用户
     */
    private String getCurrentUsername() {
        try {
            return StpUtil.getLoginIdAsString();
        } catch (NotLoginException e) {
            return "system";
        }
    }
}
