package com.forgex.integration.service.impl;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.web.StatusCode;
import com.forgex.integration.domain.dto.ThirdAuthorizationDTO;
import com.forgex.integration.enums.IntegrationPromptEnum;
import com.forgex.integration.domain.entity.ThirdAuthorization;
import com.forgex.integration.domain.param.ThirdAuthorizationParam;
import com.forgex.integration.mapper.ThirdAuthorizationMapper;
import com.forgex.integration.service.IThirdAuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 第三方授权管理服务实现类
 * <p>
 * 提供第三方授权的增删改查、Token 生成、Token 校验、白名单校验等服务实现
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ThirdAuthorizationServiceImpl extends ServiceImpl<ThirdAuthorizationMapper, ThirdAuthorization>
    implements IThirdAuthorizationService {

    private final ThirdAuthorizationMapper thirdAuthorizationMapper;

    @Override
    public Page<ThirdAuthorizationDTO> pageThirdAuthorizations(ThirdAuthorizationParam param) {
        Page<ThirdAuthorization> page = new Page<>(param.getPageNum(), param.getPageSize());
        
        LambdaQueryWrapper<ThirdAuthorization> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThirdAuthorization::getDeleted, 0);
        wrapper.eq(param.getThirdSystemId() != null, ThirdAuthorization::getThirdSystemId, param.getThirdSystemId());
        wrapper.eq(param.getAuthType() != null && !param.getAuthType().isEmpty(), 
                   ThirdAuthorization::getAuthType, param.getAuthType());
        wrapper.eq(param.getStatus() != null, ThirdAuthorization::getStatus, param.getStatus());
        wrapper.orderByDesc(ThirdAuthorization::getCreateTime);
        
        Page<ThirdAuthorization> resultPage = this.page(page, wrapper);
        
        Page<ThirdAuthorizationDTO> dtoPage = new Page<>();
        BeanUtils.copyProperties(resultPage, dtoPage, "records");
        dtoPage.setRecords(resultPage.getRecords().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList()));
        
        return dtoPage;
    }

    @Override
    public List<ThirdAuthorizationDTO> listThirdAuthorizations(ThirdAuthorizationParam param) {
        LambdaQueryWrapper<ThirdAuthorization> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThirdAuthorization::getDeleted, 0);
        wrapper.eq(param.getThirdSystemId() != null, ThirdAuthorization::getThirdSystemId, param.getThirdSystemId());
        wrapper.eq(param.getAuthType() != null && !param.getAuthType().isEmpty(), 
                   ThirdAuthorization::getAuthType, param.getAuthType());
        wrapper.eq(param.getStatus() != null, ThirdAuthorization::getStatus, param.getStatus());
        wrapper.orderByDesc(ThirdAuthorization::getCreateTime);
        
        List<ThirdAuthorization> list = this.list(wrapper);
        return list.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    @Override
    public ThirdAuthorizationDTO getThirdAuthorizationById(Long id) {
        ThirdAuthorization authorization = this.baseMapper.selectById(id);
        if (authorization == null || authorization.getDeleted()) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.THIRD_AUTH_NOT_FOUND);
        }
        return convertToDTO(authorization);
    }

    @Override
    public ThirdAuthorizationDTO getByThirdSystemId(Long thirdSystemId) {
        LambdaQueryWrapper<ThirdAuthorization> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThirdAuthorization::getDeleted, 0);
        wrapper.eq(ThirdAuthorization::getThirdSystemId, thirdSystemId);
        wrapper.last("LIMIT 1");
        
        ThirdAuthorization authorization = thirdAuthorizationMapper.selectOne(wrapper);
        return authorization != null ? convertToDTO(authorization) : null;
    }

    @Override
    public ThirdAuthorizationDTO getByTokenValue(String tokenValue) {
        LambdaQueryWrapper<ThirdAuthorization> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThirdAuthorization::getDeleted, 0);
        wrapper.eq(ThirdAuthorization::getTokenValue, tokenValue);
        wrapper.eq(ThirdAuthorization::getStatus, 1);
        wrapper.last("LIMIT 1");
        
        ThirdAuthorization authorization = thirdAuthorizationMapper.selectOne(wrapper);
        return authorization != null ? convertToDTO(authorization) : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createThirdAuthorization(ThirdAuthorizationDTO dto) {
        // 校验第三方系统是否已存在授权配置
        ThirdAuthorizationDTO existing = getByThirdSystemId(dto.getThirdSystemId());
        if (existing != null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.THIRD_AUTH_EXISTS);
        }
        
        // 校验授权方式与 Token/白名单的匹配性
        validateAuthTypeConfig(dto);
        
        // 如果是 TOKEN 方式，生成 Token
        if ("TOKEN".equals(dto.getAuthType())) {
            dto.setTokenValue(generateTokenValue());
            if (dto.getTokenExpireHours() != null && dto.getTokenExpireHours() > 0) {
                dto.setTokenExpireTime(calculateExpireTime(dto.getTokenExpireHours()));
            }
        }
        
        ThirdAuthorization authorization = new ThirdAuthorization();
        BeanUtils.copyProperties(dto, authorization);
        authorization.setCreateBy(getCurrentUsername());
        authorization.setUpdateBy(getCurrentUsername());
        
        boolean success = this.save(authorization);
        if (!success) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.THIRD_AUTH_CREATE_FAILED);
        }
        
        log.info("创建第三方授权成功：thirdSystemId={}, authType={}", 
                 dto.getThirdSystemId(), dto.getAuthType());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateThirdAuthorization(ThirdAuthorizationDTO dto) {
        if (dto.getId() == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.ID_REQUIRED);
        }
        
        // 校验授权是否存在
        ThirdAuthorization existing = this.baseMapper.selectById(dto.getId());
        if (existing == null || existing.getDeleted()) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.THIRD_AUTH_NOT_FOUND);
        }
        
        // 校验授权方式与 Token/白名单的匹配性
        validateAuthTypeConfig(dto);
        
        // 如果是 TOKEN 方式且 Token 为空，生成新 Token
        if ("TOKEN".equals(dto.getAuthType()) && dto.getTokenValue() == null) {
            dto.setTokenValue(generateTokenValue());
            if (dto.getTokenExpireHours() != null && dto.getTokenExpireHours() > 0) {
                dto.setTokenExpireTime(calculateExpireTime(dto.getTokenExpireHours()));
            }
        }
        
        ThirdAuthorization authorization = new ThirdAuthorization();
        BeanUtils.copyProperties(dto, authorization);
        authorization.setUpdateTime(LocalDateTime.now());
        authorization.setUpdateBy(getCurrentUsername());
        
        boolean success = this.updateById(authorization);
        if (!success) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.THIRD_AUTH_UPDATE_FAILED);
        }
        
        log.info("更新第三方授权成功：id={}, authType={}", dto.getId(), dto.getAuthType());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteThirdAuthorization(Long id) {
        ThirdAuthorization authorization = this.baseMapper.selectById(id);
        if (authorization == null || authorization.getDeleted()) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.THIRD_AUTH_NOT_FOUND);
        }
        
        authorization.setDeleted(true);
        authorization.setUpdateTime(LocalDateTime.now());
        authorization.setUpdateBy(getCurrentUsername());
        
        boolean success = this.updateById(authorization);
        if (!success) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.THIRD_AUTH_DELETE_FAILED);
        }
        
        log.info("删除第三方授权成功：id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteThirdAuthorizations(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.DELETE_IDS_REQUIRED);
        }
        
        for (Long id : ids) {
            try {
                deleteThirdAuthorization(id);
            } catch (I18nBusinessException e) {
                log.warn("删除第三方授权失败：id={}, 原因：{}", id, e.getMessage());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String generateToken(Long thirdSystemId, Integer expireHours) {
        // 查询现有授权
        ThirdAuthorizationDTO authorizationDTO = getByThirdSystemId(thirdSystemId);
        if (authorizationDTO == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.THIRD_AUTH_NOT_CONFIGURED);
        }
        
        if (!"TOKEN".equals(authorizationDTO.getAuthType())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.THIRD_AUTH_NOT_TOKEN_TYPE);
        }
        
        // 生成新 Token
        String tokenValue = generateTokenValue();
        LocalDateTime expireTime = null;
        if (expireHours != null && expireHours > 0) {
            expireTime = calculateExpireTime(expireHours);
        }
        
        // 更新授权记录
        ThirdAuthorization authorization = new ThirdAuthorization();
        authorization.setId(authorizationDTO.getId());
        authorization.setTokenValue(tokenValue);
        authorization.setTokenExpireHours(expireHours);
        authorization.setTokenExpireTime(expireTime);
        authorization.setUpdateTime(LocalDateTime.now());
        authorization.setUpdateBy(getCurrentUsername());
        
        this.updateById(authorization);
        
        log.info("生成 Token 成功：thirdSystemId={}, tokenValue={}, expireHours={}", 
                 thirdSystemId, tokenValue, expireHours);
        
        return tokenValue;
    }

    @Override
    public boolean validateToken(String tokenValue) {
        if (tokenValue == null || tokenValue.trim().isEmpty()) {
            log.warn("Token 为空，校验失败");
            return false;
        }
        
        LambdaQueryWrapper<ThirdAuthorization> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThirdAuthorization::getDeleted, 0);
        wrapper.eq(ThirdAuthorization::getTokenValue, tokenValue);
        wrapper.eq(ThirdAuthorization::getStatus, 1);
        wrapper.last("LIMIT 1");
        
        ThirdAuthorization authorization = thirdAuthorizationMapper.selectOne(wrapper);
        if (authorization == null) {
            log.warn("Token 不存在：tokenValue={}", tokenValue);
            return false;
        }
        
        // 检查 Token 是否过期
        if (authorization.getTokenExpireTime() != null && 
            LocalDateTime.now().isAfter(authorization.getTokenExpireTime())) {
            log.warn("Token 已过期：tokenValue={}, expireTime={}", 
                     tokenValue, authorization.getTokenExpireTime());
            return false;
        }
        
        log.info("Token 校验成功：tokenValue={}", tokenValue);
        return true;
    }

    @Override
    public boolean checkIpWhitelist(Long thirdSystemId, String ipAddress) {
        if (thirdSystemId == null || ipAddress == null || ipAddress.trim().isEmpty()) {
            log.warn("参数无效：thirdSystemId={}, ipAddress={}", thirdSystemId, ipAddress);
            return false;
        }
        
        LambdaQueryWrapper<ThirdAuthorization> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThirdAuthorization::getDeleted, 0);
        wrapper.eq(ThirdAuthorization::getThirdSystemId, thirdSystemId);
        wrapper.eq(ThirdAuthorization::getStatus, 1);
        wrapper.last("LIMIT 1");
        
        ThirdAuthorization authorization = thirdAuthorizationMapper.selectOne(wrapper);
        if (authorization == null) {
            log.warn("授权不存在：thirdSystemId={}", thirdSystemId);
            return false;
        }
        
        // 如果不是白名单方式，直接返回 false
        if (!"WHITELIST".equals(authorization.getAuthType())) {
            log.warn("授权方式不是白名单：thirdSystemId={}, authType={}", 
                     thirdSystemId, authorization.getAuthType());
            return false;
        }
        
        String whitelistIps = authorization.getWhitelistIps();
        if (whitelistIps == null || whitelistIps.trim().isEmpty()) {
            log.warn("白名单 IP 列表为空：thirdSystemId={}", thirdSystemId);
            return false;
        }
        
        // 检查 IP 是否在白名单中
        List<String> ipList = Arrays.asList(whitelistIps.split(","));
        boolean inWhitelist = ipList.stream()
            .map(String::trim)
            .anyMatch(ip -> ip.equals(ipAddress));
        
        if (!inWhitelist) {
            log.warn("IP 不在白名单中：thirdSystemId={}, ipAddress={}, whitelist={}", 
                     thirdSystemId, ipAddress, whitelistIps);
        }
        
        return inWhitelist;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refreshTokenExpire(String tokenValue, Integer expireHours) {
        if (tokenValue == null || tokenValue.trim().isEmpty()) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.ID_REQUIRED);
        }
        
        LambdaQueryWrapper<ThirdAuthorization> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThirdAuthorization::getDeleted, 0);
        wrapper.eq(ThirdAuthorization::getTokenValue, tokenValue);
        wrapper.last("LIMIT 1");
        
        ThirdAuthorization authorization = thirdAuthorizationMapper.selectOne(wrapper);
        if (authorization == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.THIRD_AUTH_NOT_FOUND);
        }
        
        LocalDateTime newExpireTime = null;
        if (expireHours != null && expireHours > 0) {
            newExpireTime = calculateExpireTime(expireHours);
        }
        
        authorization.setTokenExpireHours(expireHours);
        authorization.setTokenExpireTime(newExpireTime);
        authorization.setUpdateTime(LocalDateTime.now());
        authorization.setUpdateBy(getCurrentUsername());
        
        this.updateById(authorization);
        
        log.info("刷新 Token 有效期成功：tokenValue={}, newExpireHours={}, newExpireTime={}", 
                 tokenValue, expireHours, newExpireTime);
    }

    /**
     * 校验授权方式与配置信息的匹配性
     *
     * @param dto 授权信息 DTO
     * @throws I18nBusinessException 当配置不匹配时抛出
     */
    private void validateAuthTypeConfig(ThirdAuthorizationDTO dto) {
        if ("TOKEN".equals(dto.getAuthType())) {
            // TOKEN 方式不需要校验 whitelistIps
            log.debug("授权方式为 TOKEN，无需校验白名单");
        } else if ("WHITELIST".equals(dto.getAuthType())) {
            // 白名单方式必须配置 whitelistIps
            if (dto.getWhitelistIps() == null || dto.getWhitelistIps().trim().isEmpty()) {
                throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.THIRD_AUTH_WHITELIST_REQUIRED);
            }
        } else {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.THIRD_AUTH_UNSUPPORTED_TYPE, dto.getAuthType());
        }
    }

    /**
     * 生成 Token 值
     * <p>
     * 使用 UUID 生成随机 Token，去除横杠
     * </p>
     *
     * @return Token 值
     */
    private String generateTokenValue() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 计算 Token 过期时间
     * <p>
     * 从当前时间开始计算，加上指定的小时数
     * </p>
     *
     * @param expireHours 有效期（小时）
     * @return 过期时间
     */
    private LocalDateTime calculateExpireTime(Integer expireHours) {
        return LocalDateTime.now().plusHours(expireHours);
    }

    /**
     * 实体转 DTO
     *
     * @param authorization 授权实体
     * @return 授权 DTO
     */
    private ThirdAuthorizationDTO convertToDTO(ThirdAuthorization authorization) {
        ThirdAuthorizationDTO dto = new ThirdAuthorizationDTO();
        BeanUtils.copyProperties(authorization, dto);
        return dto;
    }

    /**
     * 获取当前登录用户
     *
     * @return 当前登录用户名
     */
    private String getCurrentUsername() {
        try {
            return StpUtil.getLoginIdAsString();
        } catch (NotLoginException e) {
            return "system";
        }
    }
}
