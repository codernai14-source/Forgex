package com.forgex.integration.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.integration.domain.dto.ApiParamConfigDTO;
import com.forgex.integration.domain.entity.ApiParamConfig;
import com.forgex.integration.domain.param.ApiParamConfigParam;
import com.forgex.integration.domain.vo.ApiParamTreeVO;

import java.util.List;

/**
 * 接口参数配置服务接口。
 * <p>
 * 负责维护接口参数树，包括节点增删改查、整树保存、JSON 导入、JSON/Java 实体解析和按字段路径查询。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 */
public interface IApiParamConfigService extends IService<ApiParamConfig> {

    /**
     * 查询参数树。
     *
     * @param apiConfigId      接口配置 ID
     * @param outboundTargetId 出站目标 ID，可为空
     * @param direction        参数方向
     * @return 参数树
     */
    List<ApiParamTreeVO> listParamTree(Long apiConfigId, Long outboundTargetId, String direction);

    /**
     * 查询参数树。
     *
     * @param apiConfigId 接口配置 ID
     * @param direction   参数方向
     * @return 参数树
     */
    default List<ApiParamTreeVO> listParamTree(Long apiConfigId, String direction) {
        return listParamTree(apiConfigId, null, direction);
    }

    /**
     * 查询子节点。
     *
     * @param param 查询参数
     * @return 子节点列表
     */
    List<ApiParamConfigDTO> listChildren(ApiParamConfigParam param);

    /**
     * 根据 ID 查询参数节点。
     *
     * @param id 参数节点 ID
     * @return 参数节点 DTO
     */
    ApiParamConfigDTO getById(Long id);

    /**
     * 新增参数节点。
     *
     * @param dto 参数节点 DTO
     */
    void create(ApiParamConfigDTO dto);

    /**
     * 更新参数节点。
     *
     * @param dto 参数节点 DTO
     */
    void update(ApiParamConfigDTO dto);

    /**
     * 删除参数节点。
     *
     * @param id 参数节点 ID
     */
    void delete(Long id);

    /**
     * 批量删除参数节点。
     *
     * @param ids 参数节点 ID 列表
     */
    void batchDelete(List<Long> ids);

    /**
     * 整树替换参数配置。
     *
     * @param apiConfigId      接口配置 ID
     * @param outboundTargetId 出站目标 ID，可为空
     * @param direction        参数方向
     * @param tree             参数树
     */
    void replaceTree(Long apiConfigId, Long outboundTargetId, String direction, List<ApiParamConfigDTO> tree);

    /**
     * 整树替换参数配置。
     *
     * @param apiConfigId 接口配置 ID
     * @param direction   参数方向
     * @param tree        参数树
     */
    default void replaceTree(Long apiConfigId, String direction, List<ApiParamConfigDTO> tree) {
        replaceTree(apiConfigId, null, direction, tree);
    }

    /**
     * 从 JSON 导入参数树。
     *
     * @param apiConfigId      接口配置 ID
     * @param outboundTargetId 出站目标 ID，可为空
     * @param direction        参数方向
     * @param jsonString       JSON 文本
     */
    void importFromJson(Long apiConfigId, Long outboundTargetId, String direction, String jsonString);

    /**
     * 从 JSON 导入参数树。
     *
     * @param apiConfigId 接口配置 ID
     * @param direction   参数方向
     * @param jsonString  JSON 文本
     */
    default void importFromJson(Long apiConfigId, String direction, String jsonString) {
        importFromJson(apiConfigId, null, direction, jsonString);
    }

    /**
     * 解析 JSON 文本为参数树。
     *
     * @param jsonString JSON 文本
     * @return 参数树
     */
    List<ApiParamTreeVO> parseJsonToTree(String jsonString);

    /**
     * 解析 Java 实体源码为参数树。
     *
     * @param javaSource Java 源码
     * @return 参数树
     */
    List<ApiParamTreeVO> parseJavaToTree(String javaSource);

    /**
     * 按字段路径查询参数节点。
     *
     * @param apiConfigId      接口配置 ID
     * @param outboundTargetId 出站目标 ID，可为空
     * @param fieldPath        字段路径
     * @param direction        参数方向
     * @return 参数节点 DTO，不存在时返回 null
     */
    ApiParamConfigDTO getByFieldPath(Long apiConfigId, Long outboundTargetId, String fieldPath, String direction);

    /**
     * 按字段路径查询参数节点。
     *
     * @param apiConfigId 接口配置 ID
     * @param fieldPath   字段路径
     * @param direction   参数方向
     * @return 参数节点 DTO，不存在时返回 null
     */
    default ApiParamConfigDTO getByFieldPath(Long apiConfigId, String fieldPath, String direction) {
        return getByFieldPath(apiConfigId, null, fieldPath, direction);
    }
}
