package com.forgex.integration.service.impl;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.web.StatusCode;
import com.forgex.integration.domain.dto.ThirdAuthorizationDTO;
import com.forgex.integration.domain.entity.ThirdAuthorization;
import com.forgex.integration.domain.param.ThirdAuthorizationParam;
import com.forgex.integration.enums.IntegrationPromptEnum;
import com.forgex.integration.mapper.ThirdAuthorizationMapper;
import com.forgex.integration.service.IThirdAuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 第三方授权管理服务实现。
 * <p>
 * 支持 Token、IP 白名单，以及“白名单命中免 Token、非白名单使用 Token”的组合授权方式。
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

    private static final String AUTH_TOKEN = "TOKEN";
    private static final String AUTH_WHITELIST = "WHITELIST";
    private static final String AUTH_TOKEN_WHITELIST = "TOKEN_WHITELIST";
    private static final String EXPIRE_HOURS = "HOURS";
    private static final String EXPIRE_DAY = "DAY";
    private static final String EXPIRE_MONTH = "MONTH";
    private static final String EXPIRE_YEAR = "YEAR";
    private static final String EXPIRE_CUSTOM = "CUSTOM";
    private static final String EXPIRE_FOREVER = "FOREVER";

    private final ThirdAuthorizationMapper thirdAuthorizationMapper;

    @Override
    public Page<ThirdAuthorizationDTO> pageThirdAuthorizations(ThirdAuthorizationParam param) {
        Page<ThirdAuthorization> page = new Page<>(param.getPageNum(), param.getPageSize());
        LambdaQueryWrapper<ThirdAuthorization> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThirdAuthorization::getDeleted, false);
        wrapper.eq(param.getThirdSystemId() != null, ThirdAuthorization::getThirdSystemId, param.getThirdSystemId());
        wrapper.eq(hasText(param.getAuthType()), ThirdAuthorization::getAuthType, param.getAuthType());
        wrapper.eq(param.getStatus() != null, ThirdAuthorization::getStatus, param.getStatus());
        wrapper.orderByDesc(ThirdAuthorization::getCreateTime);

        Page<ThirdAuthorization> resultPage = this.page(page, wrapper);
        Page<ThirdAuthorizationDTO> dtoPage = new Page<>();
        BeanUtils.copyProperties(resultPage, dtoPage, "records");
        dtoPage.setRecords(resultPage.getRecords().stream().map(this::convertToDTO).collect(Collectors.toList()));
        return dtoPage;
    }

    @Override
    public List<ThirdAuthorizationDTO> listThirdAuthorizations(ThirdAuthorizationParam param) {
        LambdaQueryWrapper<ThirdAuthorization> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThirdAuthorization::getDeleted, false);
        wrapper.eq(param.getThirdSystemId() != null, ThirdAuthorization::getThirdSystemId, param.getThirdSystemId());
        wrapper.eq(hasText(param.getAuthType()), ThirdAuthorization::getAuthType, param.getAuthType());
        wrapper.eq(param.getStatus() != null, ThirdAuthorization::getStatus, param.getStatus());
        wrapper.orderByDesc(ThirdAuthorization::getCreateTime);
        return this.list(wrapper).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public ThirdAuthorizationDTO getThirdAuthorizationById(Long id) {
        ThirdAuthorization authorization = this.baseMapper.selectById(id);
        if (authorization == null || Boolean.TRUE.equals(authorization.getDeleted())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.THIRD_AUTH_NOT_FOUND);
        }
        return convertToDTO(authorization);
    }

    @Override
    public ThirdAuthorizationDTO getByThirdSystemId(Long thirdSystemId) {
        LambdaQueryWrapper<ThirdAuthorization> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThirdAuthorization::getDeleted, false);
        wrapper.eq(ThirdAuthorization::getThirdSystemId, thirdSystemId);
        wrapper.last("LIMIT 1");
        ThirdAuthorization authorization = thirdAuthorizationMapper.selectOne(wrapper);
        return authorization != null ? convertToDTO(authorization) : null;
    }

    @Override
    public ThirdAuthorizationDTO getByTokenValue(String tokenValue) {
        LambdaQueryWrapper<ThirdAuthorization> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThirdAuthorization::getDeleted, false);
        wrapper.eq(ThirdAuthorization::getTokenValue, tokenValue);
        wrapper.eq(ThirdAuthorization::getStatus, 1);
        wrapper.last("LIMIT 1");
        ThirdAuthorization authorization = thirdAuthorizationMapper.selectOne(wrapper);
        return authorization != null ? convertToDTO(authorization) : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createThirdAuthorization(ThirdAuthorizationDTO dto) {
        ThirdAuthorizationDTO existing = getByThirdSystemId(dto.getThirdSystemId());
        if (existing != null) {
            dto.setId(existing.getId());
            updateThirdAuthorization(dto);
            return;
        }
        validateAuthTypeConfig(dto);
        prepareTokenConfig(dto);

        ThirdAuthorization authorization = new ThirdAuthorization();
        BeanUtils.copyProperties(dto, authorization);
        authorization.setCreateBy(getCurrentUsername());
        authorization.setUpdateBy(getCurrentUsername());
        if (!this.save(authorization)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.THIRD_AUTH_CREATE_FAILED);
        }
        log.info("创建第三方授权成功：thirdSystemId={}, authType={}", dto.getThirdSystemId(), dto.getAuthType());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateThirdAuthorization(ThirdAuthorizationDTO dto) {
        if (dto.getId() == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.ID_REQUIRED);
        }
        ThirdAuthorization existing = this.baseMapper.selectById(dto.getId());
        if (existing == null || Boolean.TRUE.equals(existing.getDeleted())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.THIRD_AUTH_NOT_FOUND);
        }
        validateAuthTypeConfig(dto);
        prepareTokenConfig(dto);

        ThirdAuthorization authorization = new ThirdAuthorization();
        BeanUtils.copyProperties(dto, authorization);
        authorization.setUpdateTime(LocalDateTime.now());
        authorization.setUpdateBy(getCurrentUsername());
        if (!this.updateById(authorization)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.THIRD_AUTH_UPDATE_FAILED);
        }
        log.info("更新第三方授权成功：id={}, authType={}", dto.getId(), dto.getAuthType());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteThirdAuthorization(Long id) {
        ThirdAuthorization authorization = this.baseMapper.selectById(id);
        if (authorization == null || Boolean.TRUE.equals(authorization.getDeleted())) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.THIRD_AUTH_NOT_FOUND);
        }
        authorization.setDeleted(true);
        authorization.setUpdateTime(LocalDateTime.now());
        authorization.setUpdateBy(getCurrentUsername());
        if (!this.updateById(authorization)) {
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
            deleteThirdAuthorization(id);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String generateToken(Long thirdSystemId, Integer expireHours) {
        ThirdAuthorizationDTO dto = new ThirdAuthorizationDTO();
        dto.setTokenExpireHours(expireHours);
        dto.setTokenExpireType(expireHours == null || expireHours <= 0 ? EXPIRE_FOREVER : EXPIRE_HOURS);
        return generateToken(thirdSystemId, dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String generateToken(Long thirdSystemId, ThirdAuthorizationDTO dto) {
        if (thirdSystemId == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.ID_REQUIRED);
        }
        ThirdAuthorizationDTO existing = getByThirdSystemId(thirdSystemId);
        ThirdAuthorizationDTO tokenConfig = dto == null ? new ThirdAuthorizationDTO() : dto;
        tokenConfig.setThirdSystemId(thirdSystemId);
        if (!hasText(tokenConfig.getAuthType())) {
            tokenConfig.setAuthType(existing == null ? AUTH_TOKEN : existing.getAuthType());
        }
        if (!supportsToken(tokenConfig.getAuthType())) {
            tokenConfig.setAuthType(AUTH_TOKEN_WHITELIST);
        }
        tokenConfig.setTokenValue(generateTokenValue());
        resolveTokenExpireConfig(tokenConfig);

        ThirdAuthorization authorization = new ThirdAuthorization();
        BeanUtils.copyProperties(tokenConfig, authorization);
        authorization.setUpdateTime(LocalDateTime.now());
        authorization.setUpdateBy(getCurrentUsername());
        if (existing == null) {
            authorization.setStatus(tokenConfig.getStatus() == null ? 1 : tokenConfig.getStatus());
            authorization.setCreateBy(getCurrentUsername());
            authorization.setUpdateBy(getCurrentUsername());
            if (!this.save(authorization)) {
                throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.THIRD_AUTH_CREATE_FAILED);
            }
        } else {
            authorization.setId(existing.getId());
            authorization.setWhitelistIps(tokenConfig.getWhitelistIps() == null ? existing.getWhitelistIps() : tokenConfig.getWhitelistIps());
            authorization.setRemark(tokenConfig.getRemark() == null ? existing.getRemark() : tokenConfig.getRemark());
            authorization.setStatus(tokenConfig.getStatus() == null ? existing.getStatus() : tokenConfig.getStatus());
            if (!this.updateById(authorization)) {
                throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.THIRD_AUTH_UPDATE_FAILED);
            }
        }

        log.info("生成 Token 成功：thirdSystemId={}, expireType={}, expireTime={}",
            thirdSystemId, tokenConfig.getTokenExpireType(), tokenConfig.getTokenExpireTime());
        return tokenConfig.getTokenValue();
    }

    @Override
    public boolean validateToken(String tokenValue) {
        return validateToken(null, tokenValue);
    }

    @Override
    public boolean validateToken(Long thirdSystemId, String tokenValue) {
        if (!hasText(tokenValue)) {
            log.error("Token 为空，校验失败");
            return false;
        }
        LambdaQueryWrapper<ThirdAuthorization> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThirdAuthorization::getDeleted, false);
        wrapper.eq(ThirdAuthorization::getTokenValue, tokenValue);
        wrapper.eq(ThirdAuthorization::getStatus, 1);
        wrapper.eq(thirdSystemId != null, ThirdAuthorization::getThirdSystemId, thirdSystemId);
        wrapper.last("LIMIT 1");

        ThirdAuthorization authorization = thirdAuthorizationMapper.selectOne(wrapper);
        if (authorization == null || !supportsToken(authorization.getAuthType())) {
            log.error("Token 不存在或授权方式不支持 Token：thirdSystemId={}, tokenValue={}", thirdSystemId, tokenValue);
            return false;
        }
        if (authorization.getTokenExpireTime() != null && LocalDateTime.now().isAfter(authorization.getTokenExpireTime())) {
            log.error("Token 已过期：tokenValue={}, expireTime={}", tokenValue, authorization.getTokenExpireTime());
            return false;
        }
        return true;
    }

    @Override
    public boolean checkIpWhitelist(Long thirdSystemId, String ipAddress) {
        if (thirdSystemId == null || !hasText(ipAddress)) {
            log.error("参数无效：thirdSystemId={}, ipAddress={}", thirdSystemId, ipAddress);
            return false;
        }
        LambdaQueryWrapper<ThirdAuthorization> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThirdAuthorization::getDeleted, false);
        wrapper.eq(ThirdAuthorization::getThirdSystemId, thirdSystemId);
        wrapper.eq(ThirdAuthorization::getStatus, 1);
        wrapper.last("LIMIT 1");
        ThirdAuthorization authorization = thirdAuthorizationMapper.selectOne(wrapper);
        return authorization != null
            && supportsWhitelist(authorization.getAuthType())
            && containsIp(authorization.getWhitelistIps(), ipAddress);
    }

    @Override
    public boolean checkAnyIpWhitelist(String ipAddress) {
        if (!hasText(ipAddress)) {
            return false;
        }
        LambdaQueryWrapper<ThirdAuthorization> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThirdAuthorization::getDeleted, false);
        wrapper.eq(ThirdAuthorization::getStatus, 1);
        wrapper.in(ThirdAuthorization::getAuthType, AUTH_WHITELIST, AUTH_TOKEN_WHITELIST);
        return this.list(wrapper).stream().anyMatch(authorization -> containsIp(authorization.getWhitelistIps(), ipAddress));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refreshTokenExpire(String tokenValue, Integer expireHours) {
        if (!hasText(tokenValue)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.ID_REQUIRED);
        }
        LambdaQueryWrapper<ThirdAuthorization> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ThirdAuthorization::getDeleted, false);
        wrapper.eq(ThirdAuthorization::getTokenValue, tokenValue);
        wrapper.last("LIMIT 1");
        ThirdAuthorization authorization = thirdAuthorizationMapper.selectOne(wrapper);
        if (authorization == null) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.THIRD_AUTH_NOT_FOUND);
        }
        authorization.setTokenExpireType(expireHours == null || expireHours <= 0 ? EXPIRE_FOREVER : EXPIRE_HOURS);
        authorization.setTokenExpireHours(expireHours);
        authorization.setTokenExpireValue(null);
        authorization.setTokenExpireTime(expireHours == null || expireHours <= 0 ? null : calculateExpireTime(expireHours));
        authorization.setUpdateTime(LocalDateTime.now());
        authorization.setUpdateBy(getCurrentUsername());
        this.updateById(authorization);
        log.info("刷新 Token 有效期成功：tokenValue={}, newExpireHours={}", tokenValue, expireHours);
    }

    private void validateAuthTypeConfig(ThirdAuthorizationDTO dto) {
        if (supportsToken(dto.getAuthType())) {
            if (AUTH_TOKEN_WHITELIST.equals(dto.getAuthType()) && !hasText(dto.getWhitelistIps())) {
                throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.THIRD_AUTH_WHITELIST_REQUIRED);
            }
            return;
        }
        if (AUTH_WHITELIST.equals(dto.getAuthType())) {
            if (!hasText(dto.getWhitelistIps())) {
                throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, IntegrationPromptEnum.THIRD_AUTH_WHITELIST_REQUIRED);
            }
            return;
        }
        throw new I18nBusinessException(StatusCode.BUSINESS_ERROR,
            IntegrationPromptEnum.THIRD_AUTH_UNSUPPORTED_TYPE, dto.getAuthType());
    }

    private void prepareTokenConfig(ThirdAuthorizationDTO dto) {
        if (!supportsToken(dto.getAuthType())) {
            dto.setTokenValue(null);
            dto.setTokenExpireTime(null);
            dto.setTokenExpireHours(null);
            dto.setTokenExpireType(null);
            dto.setTokenExpireValue(null);
            return;
        }
        if (!hasText(dto.getTokenValue())) {
            dto.setTokenValue(generateTokenValue());
        }
        resolveTokenExpireConfig(dto);
    }

    private void resolveTokenExpireConfig(ThirdAuthorizationDTO dto) {
        String expireType = hasText(dto.getTokenExpireType())
            ? dto.getTokenExpireType()
            : (dto.getTokenExpireHours() != null && dto.getTokenExpireHours() > 0 ? EXPIRE_HOURS : EXPIRE_FOREVER);
        dto.setTokenExpireType(expireType);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime = switch (expireType) {
            case EXPIRE_HOURS -> dto.getTokenExpireHours() == null || dto.getTokenExpireHours() <= 0
                ? null
                : now.plusHours(dto.getTokenExpireHours());
            case EXPIRE_DAY -> now.plusDays(resolveExpireValue(dto));
            case EXPIRE_MONTH -> now.plusMonths(resolveExpireValue(dto));
            case EXPIRE_YEAR -> now.plusYears(resolveExpireValue(dto));
            case EXPIRE_CUSTOM -> dto.getTokenExpireTime();
            case EXPIRE_FOREVER -> null;
            default -> throw new I18nBusinessException(StatusCode.BUSINESS_ERROR,
                IntegrationPromptEnum.THIRD_AUTH_UNSUPPORTED_TYPE, expireType);
        };
        dto.setTokenExpireTime(expireTime);
        if (expireTime == null) {
            dto.setTokenExpireHours(null);
        } else {
            dto.setTokenExpireHours((int) Math.max(Duration.between(now, expireTime).toHours(), 1));
        }
    }

    private int resolveExpireValue(ThirdAuthorizationDTO dto) {
        Integer value = dto.getTokenExpireValue();
        if (value == null || value <= 0) {
            value = 1;
        }
        dto.setTokenExpireValue(value);
        return value;
    }

    private boolean supportsToken(String authType) {
        return AUTH_TOKEN.equals(authType) || AUTH_TOKEN_WHITELIST.equals(authType);
    }

    private boolean supportsWhitelist(String authType) {
        return AUTH_WHITELIST.equals(authType) || AUTH_TOKEN_WHITELIST.equals(authType);
    }

    private boolean containsIp(String whitelistIps, String ipAddress) {
        if (!hasText(whitelistIps) || !hasText(ipAddress)) {
            return false;
        }
        return java.util.Arrays.stream(whitelistIps.split("[,，;；\\r\\n]+"))
            .map(String::trim)
            .anyMatch(ipAddress::equals);
    }

    private String generateTokenValue() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private LocalDateTime calculateExpireTime(Integer expireHours) {
        return LocalDateTime.now().plusHours(expireHours);
    }

    private ThirdAuthorizationDTO convertToDTO(ThirdAuthorization authorization) {
        ThirdAuthorizationDTO dto = new ThirdAuthorizationDTO();
        BeanUtils.copyProperties(authorization, dto);
        return dto;
    }

    private String getCurrentUsername() {
        try {
            return StpUtil.getLoginIdAsString();
        } catch (NotLoginException e) {
            return "system";
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
