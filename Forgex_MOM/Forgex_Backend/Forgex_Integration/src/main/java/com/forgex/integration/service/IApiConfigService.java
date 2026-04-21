package com.forgex.integration.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.integration.domain.dto.ApiConfigDTO;
import com.forgex.integration.domain.entity.ApiConfig;
import com.forgex.integration.domain.param.ApiConfigParam;

import java.util.List;

/**
 * 接口配置信息服务接口
 * <p>
 * 提供接口配置的增删改查、启用/停用等基础服务
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
public interface IApiConfigService extends IService<ApiConfig> {

    /**
     * 分页查询接口配置列表
     * <p>
     * 支持按接口编码、接口名称、状态、模块编码等条件查询
     * </p>
     *
     * @param param 查询参数
     * @return 分页结果
     * @see ApiConfigParam
     * @see ApiConfigDTO
     */
    Page<ApiConfigDTO> pageApiConfigs(ApiConfigParam param);

    /**
     * 查询接口配置列表
     * <p>
     * 不分页查询所有符合条件的配置
     * </p>
     *
     * @param param 查询参数
     * @return 接口配置列表
     * @see ApiConfigParam
     * @see ApiConfigDTO
     */
    List<ApiConfigDTO> listApiConfigs(ApiConfigParam param);

    /**
     * 根据 ID 获取接口配置详情
     * <p>
     * 用于编辑时回显数据
     * </p>
     *
     * @param id 配置 ID
     * @return 接口配置详情
     * @throws BusinessException 当配置不存在时抛出异常
     * @see ApiConfigDTO
     */
    ApiConfigDTO getApiConfigById(Long id);

    /**
     * 根据接口编码获取接口配置
     * <p>
     * 用于校验接口编码唯一性
     * </p>
     *
     * @param apiCode 接口编码
     * @return 接口配置信息，不存在返回 null
     * @see ApiConfigDTO
     */
    ApiConfigDTO getByApiCode(String apiCode);

    /**
     * 根据 processorBean 查询接口配置
     * <p>
     * 用于根据处理器 bean 名称查找对应的接口配置
     * </p>
     *
     * @param processorBean 处理器 bean 名称
     * @return 接口配置信息，不存在返回 null
     * @see ApiConfigDTO
     */
    ApiConfigDTO getByProcessorBean(String processorBean);

    /**
     * 创建接口配置
     * <p>
     * 自动校验接口编码唯一性
     * </p>
     *
     * @param dto 接口配置信息
     * @throws BusinessException 当接口编码已存在时抛出异常
     * @see ApiConfigDTO
     */
    ApiConfigDTO createApiConfig(ApiConfigDTO dto);

    /**
     * 更新接口配置
     * <p>
     * 自动校验接口编码唯一性（排除自身）
     * </p>
     *
     * @param dto 接口配置信息
     * @throws BusinessException 当配置不存在或接口编码已存在时抛出异常
     * @see ApiConfigDTO
     */
    ApiConfigDTO updateApiConfig(ApiConfigDTO dto);

    /**
     * 删除接口配置
     * <p>
     * 逻辑删除
     * </p>
     *
     * @param id 配置 ID
     * @throws BusinessException 当配置不存在时抛出异常
     */
    void deleteApiConfig(Long id);

    /**
     * 批量删除接口配置
     * <p>
     * 支持批量删除多个配置
     * </p>
     *
     * @param ids 配置 ID 列表
     * @throws BusinessException 当 ID 列表为空时抛出异常
     */
    void batchDeleteApiConfigs(List<Long> ids);

    /**
     * 启用接口配置
     * <p>
     * 将接口配置状态设置为启用（status=1）
     * </p>
     *
     * @param id 配置 ID
     * @throws BusinessException 当配置不存在时抛出异常
     * @see com.forgex.common.constant.SystemConstants#STATUS_NORMAL
     */
    void enableApiConfig(Long id);

    /**
     * 停用接口配置
     * <p>
     * 将接口配置状态设置为禁用（status=0）
     * </p>
     *
     * @param id 配置 ID
     * @throws BusinessException 当配置不存在时抛出异常
     * @see com.forgex.common.constant.SystemConstants#STATUS_DISABLED
     */
    void disableApiConfig(Long id);
}
