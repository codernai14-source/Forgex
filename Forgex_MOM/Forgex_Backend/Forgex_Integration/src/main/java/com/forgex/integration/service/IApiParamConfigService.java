package com.forgex.integration.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.integration.domain.dto.ApiParamConfigDTO;
import com.forgex.integration.domain.entity.ApiParamConfig;
import com.forgex.integration.domain.param.ApiParamConfigParam;
import com.forgex.integration.domain.vo.ApiParamTreeVO;

import java.util.List;
import java.util.Map;

/**
 * 接口参数配置服务接口
 * <p>
 * 提供接口参数的树形结构管理、增删改查、JSON 导入解析等功能
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
public interface IApiParamConfigService extends IService<ApiParamConfig> {

    /**
     * 查询参数配置树形列表
     * <p>
     * 根据接口配置 ID 和参数方向构建完整的树形结构
     * </p>
     *
     * @param apiConfigId 接口配置 ID
     * @param direction 参数方向（REQUEST/RESPONSE）
     * @return 树形结构列表
     */
    List<ApiParamTreeVO> listParamTree(Long apiConfigId, String direction);

    /**
     * 根据父节点 ID 查询子节点列表
     * <p>
     * 用于懒加载树形节点
     * </p>
     *
     * @param param 查询参数
     *              - apiConfigId: 接口配置 ID
     *              - parentId: 父节点 ID（null 表示根节点）
     *              - direction: 参数方向
     * @return 子节点 DTO 列表
     */
    List<ApiParamConfigDTO> listChildren(ApiParamConfigParam param);

    /**
     * 根据 ID 获取参数配置详情
     * <p>
     * 用于编辑时回显数据
     * </p>
     *
     * @param id 参数配置 ID
     * @return 参数配置 DTO
     */
    ApiParamConfigDTO getById(Long id);

    /**
     * 创建参数配置
     * <p>
     * 自动计算字段路径和排序号
     * </p>
     *
     * @param dto 参数配置 DTO
     */
    void create(ApiParamConfigDTO dto);

    /**
     * 更新参数配置
     * <p>
     * 自动更新字段路径
     * </p>
     *
     * @param dto 参数配置 DTO
     */
    void update(ApiParamConfigDTO dto);

    /**
     * 删除参数配置
     * <p>
     * 级联删除所有子节点
     * </p>
     *
     * @param id 参数配置 ID
     */
    void delete(Long id);

    /**
     * 批量删除参数配置
     * <p>
     * 支持批量删除多个参数配置
     * </p>
     *
     * @param ids 参数配置 ID 列表
     */
    void batchDelete(List<Long> ids);

    /**
     * 从 JSON 导入参数配置
     * <p>
     * 解析 JSON 结构并转换为树形参数配置
     * </p>
     *
     * @param apiConfigId 接口配置 ID
     * @param direction 参数方向（REQUEST/RESPONSE）
     * @param jsonString JSON 字符串
     */
    void importFromJson(Long apiConfigId, String direction, String jsonString);

    /**
     * 解析 JSON 为树形结构
     * <p>
     * 将 JSON 对象解析为参数配置树
     * </p>
     *
     * @param jsonString JSON 字符串
     * @return 树形结构 VO 列表
     */
    List<ApiParamTreeVO> parseJsonToTree(String jsonString);

    /**
     * 根据字段路径查询参数配置
     * <p>
     * 用于参数映射时查找对应的字段
     * </p>
     *
     * @param apiConfigId 接口配置 ID
     * @param fieldPath 字段路径
     * @param direction 参数方向
     * @return 参数配置 DTO，不存在返回 null
     */
    ApiParamConfigDTO getByFieldPath(Long apiConfigId, String fieldPath, String direction);
}
