package com.forgex.integration.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.integration.domain.dto.ThirdSystemDTO;
import com.forgex.integration.domain.entity.ThirdSystem;
import com.forgex.integration.domain.param.ThirdSystemParam;

import java.util.List;

/**
 * 第三方系统信息服务接口
 * <p>
 * 提供第三方系统的增删改查等基础服务
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
public interface IThirdSystemService extends IService<ThirdSystem> {

    /**
     * 分页查询第三方系统列表
     * <p>
     * 支持按系统编码、系统名称、状态等条件查询
     * </p>
     *
     * @param param 查询参数
     * @return 分页结果
     */
    Page<ThirdSystemDTO> pageThirdSystems(ThirdSystemParam param);

    /**
     * 查询第三方系统列表
     * <p>
     * 不分页查询所有符合条件的系统
     * </p>
     *
     * @param param 查询参数
     * @return 系统列表
     */
    List<ThirdSystemDTO> listThirdSystems(ThirdSystemParam param);

    /**
     * 根据 ID 获取第三方系统详情
     * <p>
     * 用于编辑时回显数据
     * </p>
     *
     * @param id 系统 ID
     * @return 系统详情
     */
    ThirdSystemDTO getThirdSystemById(Long id);

    /**
     * 根据系统编码获取第三方系统
     * <p>
     * 用于校验系统编码唯一性
     * </p>
     *
     * @param systemCode 系统编码
     * @return 系统信息，不存在返回 null
     */
    ThirdSystemDTO getBySystemCode(String systemCode);

    /**
     * 创建第三方系统
     * <p>
     * 自动校验系统编码唯一性
     * </p>
     *
     * @param dto 系统信息
     */
    void createThirdSystem(ThirdSystemDTO dto);

    /**
     * 更新第三方系统
     * <p>
     * 自动校验系统编码唯一性（排除自身）
     * </p>
     *
     * @param dto 系统信息
     */
    void updateThirdSystem(ThirdSystemDTO dto);

    /**
     * 删除第三方系统
     * <p>
     * 逻辑删除，同时检查是否有关联的授权记录
     * </p>
     *
     * @param id 系统 ID
     */
    void deleteThirdSystem(Long id);

    /**
     * 批量删除第三方系统
     * <p>
     * 支持批量删除多个系统
     * </p>
     *
     * @param ids 系统 ID 列表
     */
    void batchDeleteThirdSystems(List<Long> ids);
}
