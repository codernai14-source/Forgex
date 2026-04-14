package com.forgex.integration.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.forgex.integration.domain.dto.ThirdAuthorizationDTO;
import com.forgex.integration.domain.entity.ThirdAuthorization;
import com.forgex.integration.domain.param.ThirdAuthorizationParam;

import java.util.List;

/**
 * 第三方授权管理服务接口
 * <p>
 * 提供第三方授权的增删改查、Token 生成、Token 校验、白名单校验等服务
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
public interface IThirdAuthorizationService extends IService<ThirdAuthorization> {

    /**
     * 分页查询第三方授权列表
     * <p>
     * 支持按第三方系统 ID、授权方式、状态等条件查询
     * </p>
     *
     * @param param 查询参数
     * @return 分页结果
     */
    Page<ThirdAuthorizationDTO> pageThirdAuthorizations(ThirdAuthorizationParam param);

    /**
     * 查询第三方授权列表
     * <p>
     * 不分页查询所有符合条件的授权记录
     * </p>
     *
     * @param param 查询参数
     * @return 授权列表
     */
    List<ThirdAuthorizationDTO> listThirdAuthorizations(ThirdAuthorizationParam param);

    /**
     * 根据 ID 获取第三方授权详情
     * <p>
     * 用于编辑时回显数据
     * </p>
     *
     * @param id 授权 ID
     * @return 授权详情
     */
    ThirdAuthorizationDTO getThirdAuthorizationById(Long id);

    /**
     * 根据第三方系统 ID 获取授权信息
     * <p>
     * 一个第三方系统只能有一个授权配置
     * </p>
     *
     * @param thirdSystemId 第三方系统 ID
     * @return 授权信息，不存在返回 null
     */
    ThirdAuthorizationDTO getByThirdSystemId(Long thirdSystemId);

    /**
     * 根据 Token 值获取授权信息
     * <p>
     * 用于 Token 校验时查询授权信息
     * </p>
     *
     * @param tokenValue Token 值
     * @return 授权信息，不存在返回 null
     */
    ThirdAuthorizationDTO getByTokenValue(String tokenValue);

    /**
     * 创建第三方授权
     * <p>
     * 自动校验第三方系统是否已存在授权配置
     * </p>
     *
     * @param dto 授权信息
     */
    void createThirdAuthorization(ThirdAuthorizationDTO dto);

    /**
     * 更新第三方授权
     * <p>
     * 自动校验授权是否存在
     * </p>
     *
     * @param dto 授权信息
     */
    void updateThirdAuthorization(ThirdAuthorizationDTO dto);

    /**
     * 删除第三方授权
     * <p>
     * 逻辑删除授权记录
     * </p>
     *
     * @param id 授权 ID
     */
    void deleteThirdAuthorization(Long id);

    /**
     * 批量删除第三方授权
     * <p>
     * 支持批量删除多个授权记录
     * </p>
     *
     * @param ids 授权 ID 列表
     */
    void batchDeleteThirdAuthorizations(List<Long> ids);

    /**
     * 生成 Token
     * <p>
     * 使用 UUID 生成随机 Token，并设置过期时间
     * </p>
     *
     * @param thirdSystemId 第三方系统 ID
     * @param expireHours Token 有效期（小时），null 表示永不过期
     * @return 生成的 Token 值
     */
    String generateToken(Long thirdSystemId, Integer expireHours);

    /**
     * 校验 Token 是否有效
     * <p>
     * 校验 Token 是否存在、是否过期、是否启用
     * </p>
     *
     * @param tokenValue Token 值
     * @return true-有效，false-无效
     */
    boolean validateToken(String tokenValue);

    /**
     * 校验 IP 是否在白名单中
     * <p>
     * 校验指定 IP 是否在授权记录的白名单 IP 列表中
     * </p>
     *
     * @param thirdSystemId 第三方系统 ID
     * @param ipAddress IP 地址
     * @return true-在白名单中，false-不在白名单中
     */
    boolean checkIpWhitelist(Long thirdSystemId, String ipAddress);

    /**
     * 刷新 Token 有效期
     * <p>
     * 更新 Token 的过期时间，适用于 Token 续期场景
     * </p>
     *
     * @param tokenValue Token 值
     * @param expireHours 新的有效期（小时）
     */
    void refreshTokenExpire(String tokenValue, Integer expireHours);
}
