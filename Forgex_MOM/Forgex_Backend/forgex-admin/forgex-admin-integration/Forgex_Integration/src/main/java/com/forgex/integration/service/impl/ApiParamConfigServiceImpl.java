package com.forgex.integration.service.impl;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.common.api.dto.UserThirdPartySyncDTO;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.web.StatusCode;
import com.forgex.integration.domain.dto.ApiParamConfigDTO;
import com.forgex.integration.domain.entity.ApiConfig;
import com.forgex.integration.domain.entity.ApiParamConfig;
import com.forgex.integration.domain.param.ApiParamConfigParam;
import com.forgex.integration.domain.vo.ApiParamTreeVO;
import com.forgex.integration.enums.IntegrationPromptEnum;
import com.forgex.integration.mapper.ApiConfigMapper;
import com.forgex.integration.mapper.ApiParamConfigMapper;
import com.forgex.integration.service.IApiParamConfigService;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 接口参数配置服务实现。
 * <p>
 * 负责接口参数树的节点维护、整树保存、JSON 导入、JSON/Java 实体解析和字段路径定位。
 * 参数树默认以 {@code root} 作为根节点，子节点字段路径按父子层级自动生成。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 * @see IApiParamConfigService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiParamConfigServiceImpl extends ServiceImpl<ApiParamConfigMapper, ApiParamConfig>
    implements IApiParamConfigService {

    /**
     * 接口参数配置 Mapper。
     */
    private final ApiParamConfigMapper apiParamConfigMapper;

    private final ApiConfigMapper apiConfigMapper;

    /**
     * JSON 解析器。
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 参数树根节点字段名。
     */
    private static final String ROOT_FIELD = "root";
    private static final String USER_THIRD_PARTY_INBOUND_INTERPRETER = "userThirdPartyInboundInterpreter";
    private static final String THIRD_PARTY_PUSH_USERS_IDENTITY = "thirdpartypushusers";
    private static final Set<String> SIMPLE_JAVA_TYPE_NAMES = Set.of(
        "String", "Integer", "Long", "Double", "Float", "Boolean",
        "BigDecimal", "BigInteger", "LocalDate", "LocalDateTime",
        "LocalTime", "Date", "Short", "Byte", "Character"
    );
    private static final Set<String> COLLECTION_TYPE_NAMES = Set.of(
        "List", "Collection", "Set", "ArrayList", "LinkedList", "HashSet"
    );

    /**
     * 查询参数树。
     *
     * @param apiConfigId      接口配置 ID
     * @param outboundTargetId 出站目标 ID，可为空
     * @param direction        参数方向
     * @return 参数树
     */
    @Override
    public List<ApiParamTreeVO> listParamTree(Long apiConfigId, Long outboundTargetId, String direction) {
        if (apiConfigId == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.ID_REQUIRED);
        }
        LambdaQueryWrapper<ApiParamConfig> wrapper = baseWrapper(apiConfigId, outboundTargetId, direction);
        wrapper.orderByAsc(ApiParamConfig::getOrderNum, ApiParamConfig::getId);
        List<ApiParamConfig> allConfigs = this.list(wrapper);
        return completeBuiltInParamTree(apiConfigId, outboundTargetId, direction, buildTree(allConfigs, null));
    }

    /**
     * 查询指定父节点下的子节点。
     *
     * @param param 查询参数
     * @return 子节点 DTO 列表
     */
    @Override
    public List<ApiParamConfigDTO> listChildren(ApiParamConfigParam param) {
        if (param.getApiConfigId() == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.ID_REQUIRED);
        }
        LambdaQueryWrapper<ApiParamConfig> wrapper = baseWrapper(param.getApiConfigId(), param.getOutboundTargetId(), param.getDirection());
        if (param.getParentId() != null) {
            wrapper.eq(ApiParamConfig::getParentId, param.getParentId());
        } else {
            wrapper.isNull(ApiParamConfig::getParentId);
        }
        wrapper.orderByAsc(ApiParamConfig::getOrderNum, ApiParamConfig::getId);
        return this.list(wrapper).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * 根据 ID 查询参数节点详情。
     *
     * @param id 参数节点 ID
     * @return 参数节点 DTO
     */
    @Override
    public ApiParamConfigDTO getById(Long id) {
        ApiParamConfig config = this.baseMapper.selectById(id);
        if (config == null || Boolean.TRUE.equals(config.getDeleted())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.PARAM_CONFIG_NOT_FOUND);
        }
        return convertToDTO(config);
    }

    /**
     * 新增参数节点。
     *
     * @param dto 参数节点 DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(ApiParamConfigDTO dto) {
        validate(dto);
        ApiParamConfig entity = convertToEntity(dto);
        entity.setFieldName(normalizeFieldName(entity.getFieldName(), dto.getParentId() == null));
        normalizeFieldType(entity);
        buildFieldPath(entity);
        if (entity.getOrderNum() == null) {
            entity.setOrderNum(getNextOrderNum(entity.getApiConfigId(), entity.getOutboundTargetId(), entity.getParentId()));
        }
        entity.setCreateBy(resolveOperator());
        entity.setUpdateBy(resolveOperator());
        if (!this.save(entity)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.PARAM_CONFIG_CREATE_FAILED);
        }
        dto.setId(entity.getId());
    }

    /**
     * 更新参数节点。
     * <p>
     * 节点名称或父节点变化后，需要同步刷新所有子节点字段路径。
     * </p>
     *
     * @param dto 参数节点 DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ApiParamConfigDTO dto) {
        if (dto.getId() == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.ID_REQUIRED);
        }
        ApiParamConfig existing = this.baseMapper.selectById(dto.getId());
        if (existing == null || Boolean.TRUE.equals(existing.getDeleted())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.PARAM_CONFIG_NOT_FOUND);
        }
        validate(dto);
        ApiParamConfig entity = convertToEntity(dto);
        entity.setFieldName(normalizeFieldName(entity.getFieldName(), entity.getParentId() == null));
        normalizeFieldType(entity);
        buildFieldPath(entity);
        entity.setUpdateTime(LocalDateTime.now());
        entity.setUpdateBy(resolveOperator());
        if (!this.updateById(entity)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.PARAM_CONFIG_UPDATE_FAILED);
        }
        updateChildrenFieldPath(entity.getId(), entity.getFieldPath());
    }

    /**
     * 删除参数节点及其子节点。
     *
     * @param id 参数节点 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        ApiParamConfig config = this.baseMapper.selectById(id);
        if (config == null || Boolean.TRUE.equals(config.getDeleted())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.PARAM_CONFIG_NOT_FOUND);
        }
        deleteChildren(id);
        config.setDeleted(Boolean.TRUE);
        config.setUpdateTime(LocalDateTime.now());
        config.setUpdateBy(resolveOperator());
        if (!this.updateById(config)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.PARAM_CONFIG_DELETE_FAILED);
        }
    }

    /**
     * 批量删除数据。
     *
     * @param ids 参数节点 ID 列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.DELETE_IDS_REQUIRED);
        }
        for (Long id : ids) {
            delete(id);
        }
    }

    /**
     * 整树替换参数配置。
     * <p>
     * 先删除当前接口、目标和方向下的旧树，再按传入树重新落库。
     * </p>
     *
     * @param apiConfigId      接口配置 ID
     * @param outboundTargetId 出站目标 ID，可为空
     * @param direction        参数方向
     * @param tree             参数树
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceTree(Long apiConfigId, Long outboundTargetId, String direction, List<ApiParamConfigDTO> tree) {
        if (apiConfigId == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.ID_REQUIRED);
        }
        if (!StringUtils.hasText(direction)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.PARAM_DIRECTION_REQUIRED);
        }
        LambdaQueryWrapper<ApiParamConfig> removeWrapper = baseWrapper(apiConfigId, outboundTargetId, direction);
        this.remove(removeWrapper);

        ApiParamConfigDTO root = ensureRoot(tree, apiConfigId, outboundTargetId, direction);
        persistTreeNode(apiConfigId, outboundTargetId, direction, root, null, 1);
    }

    /**
     * 从 JSON 导入参数树。
     *
     * @param apiConfigId      接口配置 ID
     * @param outboundTargetId 出站目标 ID，可为空
     * @param direction        参数方向
     * @param jsonString       JSON 文本
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importFromJson(Long apiConfigId, Long outboundTargetId, String direction, String jsonString) {
        List<ApiParamTreeVO> tree = parseJsonToTree(jsonString);
        List<ApiParamConfigDTO> dtos = tree.stream().map(this::treeToDto).collect(Collectors.toList());
        replaceTree(apiConfigId, outboundTargetId, direction, dtos);
    }

    /**
     * 解析 JSON 文本为参数树。
     *
     * @param jsonString JSON 文本
     * @return 参数树
     */
    @Override
    public List<ApiParamTreeVO> parseJsonToTree(String jsonString) {
        if (!StringUtils.hasText(jsonString)) {
            return Collections.emptyList();
        }
        try {
            JsonNode jsonNode = OBJECT_MAPPER.readTree(jsonString);
            List<ApiParamTreeVO> children = convertJsonToTree(jsonNode, ROOT_FIELD, null);
            return Collections.singletonList(buildRootTree(children));
        } catch (JsonProcessingException ex) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.PARAM_JSON_PARSE_FAILED, ex.getMessage());
        }
    }

    /**
     * 解析 Java 实体源码为参数树。
     * <p>
     * 以源码中的第一个类作为根对象，解析非 static 字段，并递归展开自定义类型、数组和 List。
     * </p>
     *
     * @param javaSource Java 源码
     * @return 参数树
     */
    @Override
    public List<ApiParamTreeVO> parseJavaToTree(String javaSource) {
        if (!StringUtils.hasText(javaSource)) {
            return Collections.emptyList();
        }
        try {
            CompilationUnit cu = StaticJavaParser.parse(javaSource);
            Map<String, ClassOrInterfaceDeclaration> classMap = cu.findAll(ClassOrInterfaceDeclaration.class)
                .stream()
                .collect(Collectors.toMap(
                    ClassOrInterfaceDeclaration::getNameAsString,
                    item -> item,
                    (left, right) -> left,
                    LinkedHashMap::new
                ));
            Map<String, String> importMap = buildImportMap(cu);
            String currentPackage = cu.getPackageDeclaration()
                .map(declaration -> declaration.getNameAsString())
                .orElse("");
            ClassOrInterfaceDeclaration rootClass = cu.findFirst(ClassOrInterfaceDeclaration.class)
                .orElseThrow(() -> new IllegalArgumentException("No class declaration found"));

            List<ApiParamTreeVO> children = new ArrayList<>();
            int order = 1;
            for (FieldDeclaration field : rootClass.getFields()) {
                if (field.isStatic()) {
                    continue;
                }
                for (VariableDeclarator variable : field.getVariables()) {
                    children.add(buildTreeFromJavaField(
                        variable,
                        variable.getNameAsString(),
                        ROOT_FIELD,
                        order++,
                        classMap,
                        importMap,
                        currentPackage,
                        new java.util.HashSet<>()
                    ));
                }
            }
            return Collections.singletonList(buildRootTree(children));
        } catch (Exception ex) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.PARAM_JSON_PARSE_FAILED, ex.getMessage());
        }
    }

    /**
     * 按字段路径查询参数节点。
     *
     * @param apiConfigId      接口配置 ID
     * @param outboundTargetId 出站目标 ID，可为空
     * @param fieldPath        字段路径
     * @param direction        参数方向
     * @return 参数节点 DTO，不存在时返回 null
     */
    @Override
    public ApiParamConfigDTO getByFieldPath(Long apiConfigId, Long outboundTargetId, String fieldPath, String direction) {
        if (apiConfigId == null || !StringUtils.hasText(fieldPath)) {
            return null;
        }
        LambdaQueryWrapper<ApiParamConfig> wrapper = baseWrapper(apiConfigId, outboundTargetId, direction);
        wrapper.eq(ApiParamConfig::getFieldPath, fieldPath);
        wrapper.last("LIMIT 1");
        ApiParamConfig config = this.getOne(wrapper);
        return config == null ? null : convertToDTO(config);
    }

    /**
     * 构建参数配置基础查询条件。
     *
     * @param apiConfigId      接口配置 ID
     * @param outboundTargetId 出站目标 ID，可为空
     * @param direction        参数方向
     * @return 查询包装器
     */
    private LambdaQueryWrapper<ApiParamConfig> baseWrapper(Long apiConfigId, Long outboundTargetId, String direction) {
        LambdaQueryWrapper<ApiParamConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiParamConfig::getApiConfigId, apiConfigId);
        wrapper.eq(ApiParamConfig::getDeleted, false);
        wrapper.eq(StringUtils.hasText(direction), ApiParamConfig::getDirection, direction);
        if (outboundTargetId == null) {
            wrapper.isNull(ApiParamConfig::getOutboundTargetId);
        } else {
            wrapper.eq(ApiParamConfig::getOutboundTargetId, outboundTargetId);
        }
        return wrapper;
    }

    /**
     * 根据扁平节点列表构建树。
     *
     * @param allConfigs 所有节点
     * @param parentId   父节点 ID
     * @return 树节点列表
     */
    private List<ApiParamTreeVO> buildTree(List<ApiParamConfig> allConfigs, Long parentId) {
        return allConfigs.stream()
            .filter(config -> Objects.equals(parentId, config.getParentId()))
            .map(config -> {
                ApiParamTreeVO vo = convertToTreeVO(config);
                vo.setChildren(buildTree(allConfigs, config.getId()));
                return vo;
            })
            .collect(Collectors.toList());
    }

    /**
     * 参数实体转换为树节点 VO。
     *
     * @param config 参数实体
     * @return 树节点 VO
     */
    private ApiParamTreeVO convertToTreeVO(ApiParamConfig config) {
        ApiParamTreeVO vo = new ApiParamTreeVO();
        BeanUtils.copyProperties(config, vo);
        return vo;
    }

    private List<ApiParamTreeVO> completeBuiltInParamTree(Long apiConfigId,
                                                          Long outboundTargetId,
                                                          String direction,
                                                          List<ApiParamTreeVO> tree) {
        if (!shouldCompleteThirdPartyPushUsers(apiConfigId, direction)) {
            return tree;
        }
        ApiParamTreeVO root = ensureTreeRoot(tree, apiConfigId, outboundTargetId, direction);
        List<ApiParamTreeVO> children = root.getChildren();
        ensureScalarChild(children, apiConfigId, outboundTargetId, direction, root.getId(), "tenantId", "number");
        ApiParamTreeVO usersNode = ensureArrayChild(children, apiConfigId, outboundTargetId, direction, root.getId(), "users");
        ensureThirdPartyUserChildren(usersNode, apiConfigId, outboundTargetId, direction);
        reorderChildren(children);
        return Collections.singletonList(root);
    }

    private boolean shouldCompleteThirdPartyPushUsers(Long apiConfigId, String direction) {
        if (apiConfigId == null || !"REQUEST".equalsIgnoreCase(direction)) {
            return false;
        }
        ApiConfig apiConfig = apiConfigMapper.selectById(apiConfigId);
        if (apiConfig == null || Boolean.TRUE.equals(apiConfig.getDeleted())) {
            return false;
        }
        if (USER_THIRD_PARTY_INBOUND_INTERPRETER.equals(apiConfig.getProcessorBean())) {
            return true;
        }
        return containsIdentity(apiConfig.getApiCode())
            || containsIdentity(apiConfig.getApiName())
            || containsIdentity(apiConfig.getApiPath());
    }

    private boolean containsIdentity(String value) {
        if (!StringUtils.hasText(value)) {
            return false;
        }
        String normalized = value.replaceAll("[^A-Za-z0-9]", "").toLowerCase(Locale.ROOT);
        return normalized.contains(THIRD_PARTY_PUSH_USERS_IDENTITY);
    }

    private ApiParamTreeVO ensureTreeRoot(List<ApiParamTreeVO> tree,
                                          Long apiConfigId,
                                          Long outboundTargetId,
                                          String direction) {
        if (tree != null && !tree.isEmpty()) {
            ApiParamTreeVO first = tree.get(0);
            if (ROOT_FIELD.equalsIgnoreCase(first.getFieldName())) {
                if (first.getChildren() == null) {
                    first.setChildren(new ArrayList<>());
                }
                return first;
            }
        }
        ApiParamTreeVO root = createTreeNode(ROOT_FIELD, "OBJECT", "object", ROOT_FIELD, 1);
        root.setApiConfigId(apiConfigId);
        root.setOutboundTargetId(outboundTargetId);
        root.setDirection(direction);
        root.setParentId(null);
        root.setChildren(tree == null ? new ArrayList<>() : new ArrayList<>(tree));
        return root;
    }

    private void ensureThirdPartyUserChildren(ApiParamTreeVO usersNode,
                                              Long apiConfigId,
                                              Long outboundTargetId,
                                              String direction) {
        if (usersNode.getChildren() == null) {
            usersNode.setChildren(new ArrayList<>());
        }
        List<ApiParamTreeVO> children = usersNode.getChildren();
        for (java.lang.reflect.Field field : UserThirdPartySyncDTO.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                continue;
            }
            ensureRuntimeFieldChild(children, apiConfigId, outboundTargetId, direction, usersNode.getId(), usersNode.getFieldPath(), field);
        }
        reorderChildren(children);
    }

    private void ensureRuntimeFieldChild(List<ApiParamTreeVO> siblings,
                                         Long apiConfigId,
                                         Long outboundTargetId,
                                         String direction,
                                         Long parentId,
                                         String parentPath,
                                         java.lang.reflect.Field runtimeField) {
        String fieldName = runtimeField.getName();
        ApiParamTreeVO existing = findChildByFieldName(siblings, fieldName);
        if (existing == null) {
            existing = createTreeNode(
                fieldName,
                resolveRuntimeNodeType(runtimeField),
                resolveRuntimeFieldType(runtimeField),
                buildPath(parentPath, fieldName),
                siblings.size() + 1
            );
            existing.setApiConfigId(apiConfigId);
            existing.setOutboundTargetId(outboundTargetId);
            existing.setDirection(direction);
            existing.setParentId(parentId);
            siblings.add(existing);
        } else {
            existing.setFieldName(fieldName);
            existing.setFieldLabel(defaultText(existing.getFieldLabel(), fieldName));
            existing.setFieldPath(buildPath(parentPath, fieldName));
            if (!StringUtils.hasText(existing.getNodeType())) {
                existing.setNodeType(resolveRuntimeNodeType(runtimeField));
            }
            if (!StringUtils.hasText(existing.getFieldType())) {
                existing.setFieldType(resolveRuntimeFieldType(runtimeField));
            }
            if (existing.getChildren() == null) {
                existing.setChildren(new ArrayList<>());
            }
        }
        syncRuntimeChildren(existing, apiConfigId, outboundTargetId, direction, runtimeField);
    }

    private void syncRuntimeChildren(ApiParamTreeVO node,
                                     Long apiConfigId,
                                     Long outboundTargetId,
                                     String direction,
                                     java.lang.reflect.Field runtimeField) {
        Class<?> runtimeType = runtimeField.getType();
        if (runtimeType.isArray()) {
            Class<?> componentType = runtimeType.getComponentType();
            if (componentType != null && !isSimpleRuntimeType(componentType)) {
                ensureRuntimeObjectChildren(node, apiConfigId, outboundTargetId, direction, componentType);
            }
            return;
        }
        if (java.util.Collection.class.isAssignableFrom(runtimeType)) {
            Class<?> itemType = resolveCollectionItemClass(runtimeField.getGenericType());
            if (itemType != null && itemType != Object.class && !isSimpleRuntimeType(itemType)) {
                ensureRuntimeObjectChildren(node, apiConfigId, outboundTargetId, direction, itemType);
            }
            return;
        }
        if (!isSimpleRuntimeType(runtimeType)) {
            ensureRuntimeObjectChildren(node, apiConfigId, outboundTargetId, direction, runtimeType);
        }
    }

    private void ensureRuntimeObjectChildren(ApiParamTreeVO node,
                                             Long apiConfigId,
                                             Long outboundTargetId,
                                             String direction,
                                             Class<?> runtimeType) {
        if (node.getChildren() == null) {
            node.setChildren(new ArrayList<>());
        }
        List<ApiParamTreeVO> children = node.getChildren();
        for (java.lang.reflect.Field childField : runtimeType.getDeclaredFields()) {
            if (Modifier.isStatic(childField.getModifiers()) || childField.isSynthetic()) {
                continue;
            }
            ensureRuntimeFieldChild(children, apiConfigId, outboundTargetId, direction, node.getId(), node.getFieldPath(), childField);
        }
        reorderChildren(children);
    }

    private ApiParamTreeVO ensureScalarChild(List<ApiParamTreeVO> siblings,
                                             Long apiConfigId,
                                             Long outboundTargetId,
                                             String direction,
                                             Long parentId,
                                             String fieldName,
                                             String fieldType) {
        ApiParamTreeVO node = findChildByFieldName(siblings, fieldName);
        if (node == null) {
            node = createTreeNode(fieldName, "FIELD", fieldType, buildPath(ROOT_FIELD, fieldName), siblings.size() + 1);
            node.setApiConfigId(apiConfigId);
            node.setOutboundTargetId(outboundTargetId);
            node.setDirection(direction);
            node.setParentId(parentId);
            siblings.add(node);
        } else {
            node.setFieldName(fieldName);
            node.setFieldLabel(defaultText(node.getFieldLabel(), fieldName));
            node.setFieldPath(buildPath(ROOT_FIELD, fieldName));
            node.setNodeType("FIELD");
            node.setFieldType(fieldType);
            if (node.getChildren() == null) {
                node.setChildren(new ArrayList<>());
            }
        }
        return node;
    }

    private ApiParamTreeVO ensureArrayChild(List<ApiParamTreeVO> siblings,
                                            Long apiConfigId,
                                            Long outboundTargetId,
                                            String direction,
                                            Long parentId,
                                            String fieldName) {
        ApiParamTreeVO node = findChildByFieldName(siblings, fieldName);
        if (node == null) {
            node = createTreeNode(fieldName, "ARRAY", "array", buildPath(ROOT_FIELD, fieldName), siblings.size() + 1);
            node.setApiConfigId(apiConfigId);
            node.setOutboundTargetId(outboundTargetId);
            node.setDirection(direction);
            node.setParentId(parentId);
            siblings.add(node);
        } else {
            node.setFieldName(fieldName);
            node.setFieldLabel(defaultText(node.getFieldLabel(), fieldName));
            node.setFieldPath(buildPath(ROOT_FIELD, fieldName));
            node.setNodeType("ARRAY");
            node.setFieldType("array");
            if (node.getChildren() == null) {
                node.setChildren(new ArrayList<>());
            }
        }
        return node;
    }

    private ApiParamTreeVO findChildByFieldName(List<ApiParamTreeVO> siblings, String fieldName) {
        if (siblings == null || !StringUtils.hasText(fieldName)) {
            return null;
        }
        return siblings.stream()
            .filter(item -> item != null && fieldName.equals(item.getFieldName()))
            .findFirst()
            .orElse(null);
    }

    private String resolveRuntimeNodeType(java.lang.reflect.Field runtimeField) {
        Class<?> fieldClass = runtimeField.getType();
        if (fieldClass.isArray() || java.util.Collection.class.isAssignableFrom(fieldClass)) {
            return "ARRAY";
        }
        return isSimpleRuntimeType(fieldClass) ? "FIELD" : "OBJECT";
    }

    private String resolveRuntimeFieldType(java.lang.reflect.Field runtimeField) {
        Class<?> fieldClass = runtimeField.getType();
        if (fieldClass.isArray() || java.util.Collection.class.isAssignableFrom(fieldClass)) {
            return "array";
        }
        return isSimpleRuntimeType(fieldClass) ? normalizeRuntimeType(fieldClass) : "object";
    }

    private void reorderChildren(List<ApiParamTreeVO> children) {
        if (children == null) {
            return;
        }
        for (int i = 0; i < children.size(); i++) {
            ApiParamTreeVO child = children.get(i);
            if (child != null) {
                child.setOrderNum(i + 1);
            }
        }
    }

    /**
     * 参数实体转换为 DTO。
     *
     * @param config 参数实体
     * @return 参数 DTO
     */
    private ApiParamConfigDTO convertToDTO(ApiParamConfig config) {
        ApiParamConfigDTO dto = new ApiParamConfigDTO();
        BeanUtils.copyProperties(config, dto);
        return dto;
    }

    /**
     * 参数 DTO 转换为实体。
     *
     * @param dto 参数 DTO
     * @return 参数实体
     */
    private ApiParamConfig convertToEntity(ApiParamConfigDTO dto) {
        ApiParamConfig entity = new ApiParamConfig();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }

    /**
     * 校验参数节点基础字段。
     *
     * @param dto 参数 DTO
     */
    private void validate(ApiParamConfigDTO dto) {
        if (dto.getApiConfigId() == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.ID_REQUIRED);
        }
        if (!StringUtils.hasText(dto.getFieldName())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.PARAM_FIELD_NAME_REQUIRED);
        }
        if (!StringUtils.hasText(dto.getNodeType())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.PARAM_NODE_TYPE_REQUIRED);
        }
    }

    /**
     * 构建字段路径。
     * <p>
     * 根节点固定为 root，子节点字段路径等于父路径加当前字段名。
     * </p>
     *
     * @param entity 参数实体
     */
    private void buildFieldPath(ApiParamConfig entity) {
        if (entity.getParentId() == null) {
            entity.setFieldPath(ROOT_FIELD);
            return;
        }
        ApiParamConfig parent = this.baseMapper.selectById(entity.getParentId());
        if (parent == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.PARAM_PARENT_NOT_FOUND);
        }
        entity.setOutboundTargetId(parent.getOutboundTargetId());
        entity.setFieldPath(parent.getFieldPath() + "." + entity.getFieldName());
    }

    /**
     * 规范化字段名称。
     *
     * @param fieldName 字段名
     * @param isRoot    是否根节点
     * @return 规范化后的字段名
     */
    private String normalizeFieldName(String fieldName, boolean isRoot) {
        return isRoot ? ROOT_FIELD : fieldName.trim();
    }

    /**
     * 规范化字段类型和节点类型。
     *
     * @param entity 参数实体
     */
    private void normalizeFieldType(ApiParamConfig entity) {
        if (entity.getParentId() == null) {
            entity.setNodeType("OBJECT");
            entity.setFieldType("object");
            entity.setFieldLabel(ROOT_FIELD);
            return;
        }
        if (!StringUtils.hasText(entity.getFieldLabel())) {
            entity.setFieldLabel(entity.getFieldName());
        }
    }

    /**
     * 获取下一个排序号。
     *
     * @param apiConfigId      接口配置 ID
     * @param outboundTargetId 出站目标 ID，可为空
     * @param parentId         父节点 ID
     * @return 下一个排序号
     */
    private int getNextOrderNum(Long apiConfigId, Long outboundTargetId, Long parentId) {
        LambdaQueryWrapper<ApiParamConfig> wrapper = baseWrapper(apiConfigId, outboundTargetId, null);
        if (parentId == null) {
            wrapper.isNull(ApiParamConfig::getParentId);
        } else {
            wrapper.eq(ApiParamConfig::getParentId, parentId);
        }
        List<ApiParamConfig> siblings = this.list(wrapper);
        return siblings.size() + 1;
    }

    /**
     * 递归更新子节点字段路径。
     *
     * @param parentId        父节点 ID
     * @param parentFieldPath 父节点字段路径
     */
    private void updateChildrenFieldPath(Long parentId, String parentFieldPath) {
        LambdaQueryWrapper<ApiParamConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiParamConfig::getParentId, parentId);
        wrapper.eq(ApiParamConfig::getDeleted, false);
        List<ApiParamConfig> children = this.list(wrapper);
        for (ApiParamConfig child : children) {
            child.setFieldPath(parentFieldPath + "." + child.getFieldName());
            child.setUpdateTime(LocalDateTime.now());
            child.setUpdateBy(resolveOperator());
            this.updateById(child);
            updateChildrenFieldPath(child.getId(), child.getFieldPath());
        }
    }

    /**
     * 递归逻辑删除子节点。
     *
     * @param parentId 父节点 ID
     */
    private void deleteChildren(Long parentId) {
        LambdaQueryWrapper<ApiParamConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiParamConfig::getParentId, parentId);
        wrapper.eq(ApiParamConfig::getDeleted, false);
        List<ApiParamConfig> children = this.list(wrapper);
        for (ApiParamConfig child : children) {
            deleteChildren(child.getId());
            child.setDeleted(Boolean.TRUE);
            child.setUpdateTime(LocalDateTime.now());
            child.setUpdateBy(resolveOperator());
            this.updateById(child);
        }
    }

    /**
     * 持久化参数树节点。
     *
     * @param apiConfigId      接口配置 ID
     * @param outboundTargetId 出站目标 ID，可为空
     * @param direction        参数方向
     * @param item             当前树节点
     * @param parentId         父节点 ID
     * @param orderNum         排序号
     */
    private void persistTreeNode(Long apiConfigId,
                                 Long outboundTargetId,
                                 String direction,
                                 ApiParamConfigDTO item,
                                 Long parentId,
                                 int orderNum) {
        ApiParamConfig entity = new ApiParamConfig();
        BeanUtils.copyProperties(item, entity);
        entity.setId(null);
        entity.setApiConfigId(apiConfigId);
        entity.setOutboundTargetId(outboundTargetId);
        entity.setDirection(direction);
        entity.setParentId(parentId);
        entity.setOrderNum(orderNum);
        entity.setFieldName(normalizeFieldName(item.getFieldName(), parentId == null));
        entity.setFieldLabel(parentId == null ? ROOT_FIELD : defaultText(item.getFieldLabel(), entity.getFieldName()));
        if (parentId == null) {
            entity.setNodeType("OBJECT");
            entity.setFieldType("object");
            entity.setFieldPath(ROOT_FIELD);
        } else {
            entity.setNodeType(item.getNodeType());
            entity.setFieldType(defaultText(item.getFieldType(), inferFieldType(item)));
            ApiParamConfig parent = this.baseMapper.selectById(parentId);
            entity.setFieldPath(parent.getFieldPath() + "." + entity.getFieldName());
        }
        entity.setRequired(item.getRequired() == null ? 0 : item.getRequired());
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        entity.setCreateBy(resolveOperator());
        entity.setUpdateBy(resolveOperator());
        entity.setDeleted(Boolean.FALSE);
        this.save(entity);

        if (item.getChildren() != null && !item.getChildren().isEmpty()) {
            int childOrder = 1;
            for (ApiParamConfigDTO child : item.getChildren()) {
                persistTreeNode(apiConfigId, outboundTargetId, direction, child, entity.getId(), childOrder++);
            }
        }
    }

    /**
     * 确保整树保存时存在 root 根节点。
     *
     * @param tree             原始树
     * @param apiConfigId      接口配置 ID
     * @param outboundTargetId 出站目标 ID，可为空
     * @param direction        参数方向
     * @return 根节点 DTO
     */
    private ApiParamConfigDTO ensureRoot(List<ApiParamConfigDTO> tree,
                                         Long apiConfigId,
                                         Long outboundTargetId,
                                         String direction) {
        ApiParamConfigDTO root;
        if (tree != null && tree.size() == 1 && ROOT_FIELD.equalsIgnoreCase(tree.get(0).getFieldName())) {
            root = tree.get(0);
        } else {
            root = new ApiParamConfigDTO();
            root.setApiConfigId(apiConfigId);
            root.setOutboundTargetId(outboundTargetId);
            root.setDirection(direction);
            root.setFieldName(ROOT_FIELD);
            root.setFieldLabel(ROOT_FIELD);
            root.setFieldType("object");
            root.setNodeType("OBJECT");
            root.setRequired(0);
            root.setChildren(tree == null ? new ArrayList<>() : tree);
        }
        if (root.getChildren() == null) {
            root.setChildren(new ArrayList<>());
        }
        return root;
    }

    /**
     * 构建 root 根节点 VO。
     *
     * @param children 子节点
     * @return root 节点
     */
    private ApiParamTreeVO buildRootTree(List<ApiParamTreeVO> children) {
        ApiParamTreeVO root = new ApiParamTreeVO();
        root.setFieldName(ROOT_FIELD);
        root.setFieldLabel(ROOT_FIELD);
        root.setFieldType("object");
        root.setNodeType("OBJECT");
        root.setFieldPath(ROOT_FIELD);
        root.setRequired(0);
        root.setChildren(children);
        return root;
    }

    /**
     * 将 JSON 节点转换为参数树。
     *
     * @param jsonNode   JSON 节点
     * @param currentName 当前字段名
     * @param parentPath 父路径
     * @return 参数树节点列表
     */
    private List<ApiParamTreeVO> convertJsonToTree(JsonNode jsonNode, String currentName, String parentPath) {
        if (jsonNode == null) {
            return Collections.emptyList();
        }

        if (jsonNode.isObject()) {
            List<ApiParamTreeVO> children = new ArrayList<>();
            int order = 1;
            var iterator = jsonNode.fields();
            while (iterator.hasNext()) {
                Map.Entry<String, JsonNode> entry = iterator.next();
                children.addAll(convertJsonChild(entry.getKey(), entry.getValue(), buildPath(parentPath, currentName), order++));
            }
            return children;
        }

        if (jsonNode.isArray()) {
            JsonNode sample = jsonNode.size() > 0 ? jsonNode.get(0) : null;
            ApiParamTreeVO node = createTreeNode(currentName, "ARRAY", "array", buildPath(parentPath, currentName), 0);
            node.setChildren(sample == null ? Collections.emptyList() : convertJsonToTree(sample, currentName + "Item", node.getFieldPath()));
            return Collections.singletonList(node);
        }

        ApiParamTreeVO field = createTreeNode(currentName, "FIELD", detectJsonFieldType(jsonNode), buildPath(parentPath, currentName), 0);
        return Collections.singletonList(field);
    }

    /**
     * 将 JSON 子字段转换为参数树节点。
     *
     * @param fieldName  字段名
     * @param jsonNode   JSON 节点
     * @param parentPath 父路径
     * @param order      排序号
     * @return 参数树节点列表
     */
    private List<ApiParamTreeVO> convertJsonChild(String fieldName, JsonNode jsonNode, String parentPath, int order) {
        ApiParamTreeVO node;
        if (jsonNode.isObject()) {
            node = createTreeNode(fieldName, "OBJECT", "object", buildPath(parentPath, fieldName), order);
            List<ApiParamTreeVO> children = new ArrayList<>();
            int childOrder = 1;
            var iterator = jsonNode.fields();
            while (iterator.hasNext()) {
                Map.Entry<String, JsonNode> entry = iterator.next();
                children.addAll(convertJsonChild(entry.getKey(), entry.getValue(), node.getFieldPath(), childOrder++));
            }
            node.setChildren(children);
        } else if (jsonNode.isArray()) {
            node = createTreeNode(fieldName, "ARRAY", "array", buildPath(parentPath, fieldName), order);
            JsonNode sample = jsonNode.size() > 0 ? jsonNode.get(0) : null;
            node.setChildren(sample == null ? Collections.emptyList() : convertJsonToTree(sample, fieldName + "Item", node.getFieldPath()));
        } else {
            node = createTreeNode(fieldName, "FIELD", detectJsonFieldType(jsonNode), buildPath(parentPath, fieldName), order);
        }
        return Collections.singletonList(node);
    }

    /**
     * 将 Java 字段转换为参数树节点。
     *
     * @param variable  字段变量声明
     * @param fieldName 字段名
     * @param parentPath 父路径
     * @param orderNum 排序号
     * @param classMap  源码中的类声明映射
     * @return 参数树节点
     */
    private ApiParamTreeVO buildTreeFromJavaField(VariableDeclarator variable,
                                                  String fieldName,
                                                  String parentPath,
                                                  int orderNum,
                                                  Map<String, ClassOrInterfaceDeclaration> classMap,
                                                  Map<String, String> importMap,
                                                  String currentPackage,
                                                  Set<String> visitingTypes) {
        Type type = variable.getType();
        String fieldPath = buildPath(parentPath, fieldName);
        if (type.isPrimitiveType() || isSimpleJavaType(type)) {
            return createTreeNode(fieldName, "FIELD", normalizeJavaType(type), fieldPath, orderNum);
        }
        if (type.isArrayType()) {
            ArrayType arrayType = type.asArrayType();
            ApiParamTreeVO node = createTreeNode(fieldName, "ARRAY", "array", fieldPath, orderNum);
            node.setChildren(buildArrayChildren(
                arrayType.getComponentType(),
                fieldName,
                fieldPath,
                classMap,
                importMap,
                currentPackage,
                visitingTypes
            ));
            return node;
        }
        if (type.isClassOrInterfaceType()) {
            ClassOrInterfaceType classType = type.asClassOrInterfaceType();
            if (isCollectionType(classType)) {
                ApiParamTreeVO node = createTreeNode(fieldName, "ARRAY", "array", fieldPath, orderNum);
                Type childType = classType.getTypeArguments()
                    .filter(args -> !args.isEmpty())
                    .map(args -> args.get(0))
                    .orElse(StaticJavaParser.parseType("java.lang.Object"));
                node.setChildren(buildArrayChildren(
                    childType,
                    fieldName,
                    fieldPath,
                    classMap,
                    importMap,
                    currentPackage,
                    visitingTypes
                ));
                return node;
            }

            ClassOrInterfaceDeclaration nested = classMap.get(classType.getNameAsString());
            if (nested != null) {
                ApiParamTreeVO node = createTreeNode(fieldName, "OBJECT", "object", fieldPath, orderNum);
                node.setChildren(buildChildrenFromDeclaration(
                    nested,
                    fieldPath,
                    classMap,
                    importMap,
                    currentPackage,
                    visitingTypes
                ));
                return node;
            }

            Class<?> runtimeClass = resolveRuntimeClass(classType, importMap, currentPackage);
            if (runtimeClass != null && !isSimpleRuntimeType(runtimeClass)) {
                ApiParamTreeVO node = createTreeNode(fieldName, "OBJECT", "object", fieldPath, orderNum);
                node.setChildren(buildChildrenFromRuntimeClass(runtimeClass, fieldPath, visitingTypes));
                if (!node.getChildren().isEmpty()) {
                    return node;
                }
            }
        }

        return createTreeNode(fieldName, "FIELD", "string", fieldPath, orderNum);
    }

    /**
     * 创建参数树节点。
     *
     * @param fieldName 字段名
     * @param nodeType  节点类型
     * @param fieldType 字段类型
     * @param fieldPath 字段路径
     * @param orderNum  排序号
     * @return 参数树节点
     */
    private ApiParamTreeVO createTreeNode(String fieldName, String nodeType, String fieldType, String fieldPath, int orderNum) {
        ApiParamTreeVO node = new ApiParamTreeVO();
        node.setFieldName(fieldName);
        node.setFieldLabel(fieldName);
        node.setNodeType(nodeType);
        node.setFieldType(fieldType);
        node.setFieldPath(fieldPath);
        node.setRequired(0);
        node.setOrderNum(orderNum);
        node.setChildren(new ArrayList<>());
        return node;
    }

    private List<ApiParamTreeVO> buildArrayChildren(Type elementType,
                                                    String fieldName,
                                                    String parentPath,
                                                    Map<String, ClassOrInterfaceDeclaration> classMap,
                                                    Map<String, String> importMap,
                                                    String currentPackage,
                                                    Set<String> visitingTypes) {
        ApiParamTreeVO itemNode = buildTreeFromJavaField(
            new VariableDeclarator(elementType, fieldName + "Item"),
            fieldName + "Item",
            parentPath,
            1,
            classMap,
            importMap,
            currentPackage,
            visitingTypes
        );
        if ("OBJECT".equalsIgnoreCase(itemNode.getNodeType())
            && itemNode.getChildren() != null
            && !itemNode.getChildren().isEmpty()) {
            return itemNode.getChildren();
        }
        return Collections.singletonList(itemNode);
    }

    private List<ApiParamTreeVO> buildChildrenFromDeclaration(ClassOrInterfaceDeclaration declaration,
                                                              String parentPath,
                                                              Map<String, ClassOrInterfaceDeclaration> classMap,
                                                              Map<String, String> importMap,
                                                              String currentPackage,
                                                              Set<String> visitingTypes) {
        String typeKey = declaration.getFullyQualifiedName().orElse(declaration.getNameAsString());
        if (!visitingTypes.add(typeKey)) {
            return Collections.emptyList();
        }
        try {
            List<ApiParamTreeVO> children = new ArrayList<>();
            int childOrder = 1;
            for (FieldDeclaration field : declaration.getFields()) {
                if (field.isStatic()) {
                    continue;
                }
                for (VariableDeclarator child : field.getVariables()) {
                    children.add(buildTreeFromJavaField(
                        child,
                        child.getNameAsString(),
                        parentPath,
                        childOrder++,
                        classMap,
                        importMap,
                        currentPackage,
                        visitingTypes
                    ));
                }
            }
            return children;
        } finally {
            visitingTypes.remove(typeKey);
        }
    }

    private List<ApiParamTreeVO> buildChildrenFromRuntimeClass(Class<?> runtimeClass,
                                                               String parentPath,
                                                               Set<String> visitingTypes) {
        if (runtimeClass == null) {
            return Collections.emptyList();
        }
        String typeKey = runtimeClass.getName();
        if (!visitingTypes.add(typeKey)) {
            return Collections.emptyList();
        }
        try {
            List<ApiParamTreeVO> children = new ArrayList<>();
            int childOrder = 1;
            for (java.lang.reflect.Field field : runtimeClass.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                    continue;
                }
                children.add(buildTreeFromRuntimeField(field, parentPath, childOrder++, visitingTypes));
            }
            return children;
        } finally {
            visitingTypes.remove(typeKey);
        }
    }

    private ApiParamTreeVO buildTreeFromRuntimeField(java.lang.reflect.Field field,
                                                     String parentPath,
                                                     int orderNum,
                                                     Set<String> visitingTypes) {
        String fieldName = field.getName();
        String fieldPath = buildPath(parentPath, fieldName);
        Class<?> fieldClass = field.getType();
        if (isSimpleRuntimeType(fieldClass)) {
            return createTreeNode(fieldName, "FIELD", normalizeRuntimeType(fieldClass), fieldPath, orderNum);
        }
        if (fieldClass.isArray()) {
            ApiParamTreeVO node = createTreeNode(fieldName, "ARRAY", "array", fieldPath, orderNum);
            node.setChildren(buildArrayChildrenFromRuntimeType(fieldClass.getComponentType(), fieldName, fieldPath, visitingTypes));
            return node;
        }
        if (java.util.Collection.class.isAssignableFrom(fieldClass)) {
            ApiParamTreeVO node = createTreeNode(fieldName, "ARRAY", "array", fieldPath, orderNum);
            node.setChildren(buildArrayChildrenFromRuntimeType(resolveCollectionItemClass(field.getGenericType()), fieldName, fieldPath, visitingTypes));
            return node;
        }

        ApiParamTreeVO node = createTreeNode(fieldName, "OBJECT", "object", fieldPath, orderNum);
        node.setChildren(buildChildrenFromRuntimeClass(fieldClass, fieldPath, visitingTypes));
        if (!node.getChildren().isEmpty()) {
            return node;
        }
        return createTreeNode(fieldName, "FIELD", "string", fieldPath, orderNum);
    }

    private List<ApiParamTreeVO> buildArrayChildrenFromRuntimeType(Class<?> itemClass,
                                                                   String fieldName,
                                                                   String parentPath,
                                                                   Set<String> visitingTypes) {
        String itemFieldName = fieldName + "Item";
        String itemPath = buildPath(parentPath, itemFieldName);
        if (itemClass == null || Object.class == itemClass || isSimpleRuntimeType(itemClass)) {
            return Collections.singletonList(createTreeNode(
                itemFieldName,
                "FIELD",
                normalizeRuntimeType(itemClass),
                itemPath,
                1
            ));
        }
        List<ApiParamTreeVO> itemChildren = buildChildrenFromRuntimeClass(itemClass, itemPath, visitingTypes);
        if (!itemChildren.isEmpty()) {
            return itemChildren;
        }
        return Collections.singletonList(createTreeNode(itemFieldName, "FIELD", "string", itemPath, 1));
    }

    private Map<String, String> buildImportMap(CompilationUnit compilationUnit) {
        Map<String, String> imports = new LinkedHashMap<>();
        for (ImportDeclaration declaration : compilationUnit.getImports()) {
            if (declaration.isAsterisk()) {
                continue;
            }
            String qualifiedName = declaration.getNameAsString();
            imports.put(extractSimpleTypeName(qualifiedName), qualifiedName);
        }
        return imports;
    }

    private boolean isCollectionType(ClassOrInterfaceType classType) {
        return COLLECTION_TYPE_NAMES.contains(extractSimpleTypeName(classType.getNameAsString()));
    }

    private Class<?> resolveRuntimeClass(ClassOrInterfaceType classType,
                                         Map<String, String> importMap,
                                         String currentPackage) {
        String typeName = classType.getNameAsString();
        String simpleName = extractSimpleTypeName(typeName);
        String importedType = importMap.get(simpleName);
        if (StringUtils.hasText(importedType)) {
            Class<?> clazz = loadClass(importedType);
            if (clazz != null) {
                return clazz;
            }
        }
        if (typeName.contains(".")) {
            Class<?> clazz = loadClass(typeName);
            if (clazz != null) {
                return clazz;
            }
        }
        if (StringUtils.hasText(currentPackage)) {
            Class<?> clazz = loadClass(currentPackage + "." + simpleName);
            if (clazz != null) {
                return clazz;
            }
        }
        return loadClass("java.lang." + simpleName);
    }

    private Class<?> loadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }

    private Class<?> resolveCollectionItemClass(java.lang.reflect.Type genericType) {
        if (genericType instanceof ParameterizedType parameterizedType) {
            java.lang.reflect.Type[] actualTypes = parameterizedType.getActualTypeArguments();
            if (actualTypes.length > 0) {
                return resolveRuntimeClass(actualTypes[0]);
            }
        }
        return Object.class;
    }

    private Class<?> resolveRuntimeClass(java.lang.reflect.Type type) {
        if (type instanceof Class<?> clazz) {
            return clazz;
        }
        if (type instanceof ParameterizedType parameterizedType) {
            java.lang.reflect.Type rawType = parameterizedType.getRawType();
            if (rawType instanceof Class<?> clazz) {
                return clazz;
            }
        }
        return Object.class;
    }

    /**
     * 参数树节点转换为保存 DTO。
     *
     * @param tree 参数树节点
     * @return 保存 DTO
     */
    private ApiParamConfigDTO treeToDto(ApiParamTreeVO tree) {
        ApiParamConfigDTO dto = new ApiParamConfigDTO();
        dto.setFieldName(tree.getFieldName());
        dto.setFieldLabel(tree.getFieldLabel());
        dto.setNodeType(tree.getNodeType());
        dto.setFieldType(tree.getFieldType());
        dto.setFieldPath(tree.getFieldPath());
        dto.setRequired(tree.getRequired());
        dto.setOrderNum(tree.getOrderNum());
        dto.setRemark(tree.getRemark());
        if (tree.getChildren() != null) {
            dto.setChildren(tree.getChildren().stream().map(this::treeToDto).collect(Collectors.toList()));
        }
        return dto;
    }

    /**
     * 根据 JSON 节点推断字段类型。
     *
     * @param jsonNode JSON 节点
     * @return 字段类型
     */
    private String detectJsonFieldType(JsonNode jsonNode) {
        if (jsonNode == null || jsonNode.isNull()) {
            return "string";
        }
        if (jsonNode.isNumber()) {
            return "number";
        }
        if (jsonNode.isBoolean()) {
            return "boolean";
        }
        return "string";
    }

    /**
     * 拼接字段路径。
     *
     * @param parentPath 父路径
     * @param fieldName  字段名
     * @return 字段路径
     */
    private String buildPath(String parentPath, String fieldName) {
        return StringUtils.hasText(parentPath) ? parentPath + "." + fieldName : fieldName;
    }

    /**
     * 判断是否 Java 简单类型。
     *
     * @param type Java 字段类型
     * @return true 表示简单类型
     */
    private boolean isSimpleJavaType(Type type) {
        if (type instanceof PrimitiveType) {
            return true;
        }
        return SIMPLE_JAVA_TYPE_NAMES.contains(extractSimpleTypeName(type.asString()));
    }

    /**
     * 将 Java 类型映射为参数字段类型。
     *
     * @param type Java 字段类型
     * @return 参数字段类型
     */
    private String normalizeJavaType(Type type) {
        String name = extractSimpleTypeName(type.asString());
        return switch (name) {
            case "int", "Integer", "long", "Long", "double", "Double", "float", "Float",
                "short", "Short", "byte", "Byte", "BigDecimal", "BigInteger" -> "number";
            case "boolean", "Boolean" -> "boolean";
            case "LocalDate", "LocalDateTime", "LocalTime", "Date" -> "date";
            default -> "string";
        };
    }

    private boolean isSimpleRuntimeType(Class<?> clazz) {
        return clazz == null
            || clazz.isPrimitive()
            || clazz.isEnum()
            || Number.class.isAssignableFrom(clazz)
            || CharSequence.class.isAssignableFrom(clazz)
            || Boolean.class == clazz
            || Character.class == clazz
            || Date.class.isAssignableFrom(clazz)
            || TemporalAccessor.class.isAssignableFrom(clazz)
            || BigDecimal.class == clazz
            || BigInteger.class == clazz;
    }

    private String normalizeRuntimeType(Class<?> clazz) {
        if (clazz == null) {
            return "string";
        }
        if (Boolean.class == clazz || boolean.class == clazz) {
            return "boolean";
        }
        if (Date.class.isAssignableFrom(clazz) || TemporalAccessor.class.isAssignableFrom(clazz)) {
            return "date";
        }
        if (clazz.isPrimitive() && clazz != boolean.class && clazz != char.class) {
            return "number";
        }
        if (Number.class.isAssignableFrom(clazz) || BigDecimal.class == clazz || BigInteger.class == clazz) {
            return "number";
        }
        return "string";
    }

    private String extractSimpleTypeName(String typeName) {
        if (!StringUtils.hasText(typeName)) {
            return typeName;
        }
        int index = typeName.lastIndexOf('.');
        return index >= 0 ? typeName.substring(index + 1) : typeName;
    }

    /**
     * 根据节点信息推断字段类型。
     *
     * @param item 参数节点 DTO
     * @return 字段类型
     */
    private String inferFieldType(ApiParamConfigDTO item) {
        if ("ARRAY".equalsIgnoreCase(item.getNodeType())) {
            return "array";
        }
        if ("OBJECT".equalsIgnoreCase(item.getNodeType()) || (item.getChildren() != null && !item.getChildren().isEmpty())) {
            return "object";
        }
        return "string";
    }

    /**
     * 获取非空文本。
     *
     * @param value    原始值
     * @param fallback 兜底值
     * @return 非空文本
     */
    private String defaultText(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    /**
     * 解析当前操作人。
     *
     * @return 当前登录 ID，未登录时返回 system
     */
    private String resolveOperator() {
        try {
            return StpUtil.getLoginIdAsString();
        } catch (NotLoginException ex) {
            return "system";
        }
    }
}
