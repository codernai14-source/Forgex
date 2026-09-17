package com.forgex.auth.mfa;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forgex.auth.domain.entity.SysUser;
import com.forgex.auth.domain.entity.SysUserMfa;
import com.forgex.auth.domain.entity.SysUserTenant;
import com.forgex.auth.domain.vo.LoginResultVO;
import com.forgex.auth.domain.vo.TenantVO;
import com.forgex.auth.enums.AuthPromptEnum;
import com.forgex.auth.mapper.SysTenantMapper;
import com.forgex.auth.mapper.SysUserMapper;
import com.forgex.auth.mapper.SysUserMfaMapper;
import com.forgex.auth.mapper.SysUserTenantMapper;
import com.forgex.auth.domain.entity.SysTenant;
import com.forgex.auth.service.LoginInteractionCodeService;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.web.R;
import com.forgex.common.web.StatusCode;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * MFA 绑定、校验与登录挑战实现。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see TotpService
 */
@Service
@RequiredArgsConstructor
public class MfaServiceImpl implements MfaService {

    private static final String CHALLENGE_PREFIX = "fx:auth:mfa:challenge:";
    private static final long PLATFORM_TENANT = 0L;

    private final TotpService totpService;
    private final SysUserMfaMapper mfaMapper;
    private final SysUserMapper userMapper;
    private final SysUserTenantMapper userTenantMapper;
    private final SysTenantMapper tenantMapper;
    private final LoginInteractionCodeService loginInteractionCodeService;
    private final RedissonClient redissonClient;

    /**
     * 开始绑定并返回 otpauth URI。
     *
     * @param userId  用户 ID
     * @param account 账号
     * @return 绑定信息
     */
    @Override
    public Map<String, Object> bind(Long userId, String account) {
        String secret = totpService.generateSecret();
        List<String> recoveryCodes = generateRecoveryCodes();
        SysUserMfa existing = findByUser(userId);
        SysUserMfa entity = existing == null ? new SysUserMfa() : existing;
        entity.setTenantId(PLATFORM_TENANT);
        entity.setUserId(userId);
        entity.setSecretCiphertext(secret);
        entity.setEnabled(false);
        entity.setRecoveryCodesCiphertext(String.join(",", recoveryCodes));
        entity.setUpdatedTime(LocalDateTime.now());
        if (existing == null) {
            entity.setCreatedTime(LocalDateTime.now());
            mfaMapper.insert(entity);
        } else {
            mfaMapper.updateById(entity);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("otpAuthUri", "otpauth://totp/Forgex:" + account + "?secret=" + secret + "&issuer=Forgex");
        result.put("secret", secret);
        result.put("recoveryCodes", recoveryCodes);
        return result;
    }

    /**
     * 确认绑定。
     *
     * @param userId 用户 ID
     * @param code   动态码
     */
    @Override
    public void confirmBind(Long userId, String code) {
        SysUserMfa entity = findByUser(userId);
        if (entity == null || !totpService.verify(entity.getSecretCiphertext(), code)) {
            throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, AuthPromptEnum.MFA_CODE_INVALID);
        }
        entity.setEnabled(true);
        entity.setUpdatedTime(LocalDateTime.now());
        mfaMapper.updateById(entity);
        SysUser update = new SysUser();
        update.setId(userId);
        update.setMfaEnabled(true);
        userMapper.updateById(update);
    }

    /**
     * 解绑 MFA。
     *
     * @param userId 用户 ID
     */
    @Override
    public void unbind(Long userId) {
        SysUserMfa entity = findByUser(userId);
        if (entity != null) {
            mfaMapper.deleteById(entity.getId());
        }
        SysUser update = new SysUser();
        update.setId(userId);
        update.setMfaEnabled(false);
        userMapper.updateById(update);
    }

    /**
     * 签发 5 分钟挑战票据。
     *
     * @param userId  用户 ID
     * @param account 账号
     * @return 挑战 ID
     */
    @Override
    public String issueChallenge(Long userId, String account) {
        String challengeId = UUID.randomUUID().toString().replace("-", "");
        redissonClient.getBucket(CHALLENGE_PREFIX + challengeId)
                .set(userId + ":" + account, Duration.ofMinutes(5));
        return challengeId;
    }

    /**
     * 校验挑战并返回登录租户列表。
     *
     * @param challengeId 挑战 ID
     * @param code        动态码或恢复码
     * @return 登录结果
     */
    @Override
    public R<LoginResultVO> verifyChallenge(String challengeId, String code) {
        if (!StringUtils.hasText(challengeId) || !StringUtils.hasText(code)) {
            return R.fail(AuthPromptEnum.MFA_CODE_INVALID);
        }
        RBucket<String> bucket = redissonClient.getBucket(CHALLENGE_PREFIX + challengeId);
        String payload = bucket.get();
        if (!StringUtils.hasText(payload)) {
            return R.fail(AuthPromptEnum.LOGIN_INTERACTION_EXPIRED);
        }
        String[] parts = payload.split(":", 2);
        Long userId = Long.valueOf(parts[0]);
        String account = parts.length > 1 ? parts[1] : "";
        SysUserMfa entity = findByUser(userId);
        if (entity == null || !Boolean.TRUE.equals(entity.getEnabled())) {
            return R.fail(AuthPromptEnum.MFA_CODE_INVALID);
        }
        boolean ok = totpService.verify(entity.getSecretCiphertext(), code);
        if (!ok) {
            ok = consumeRecoveryCode(entity, code);
        }
        if (!ok) {
            return R.fail(AuthPromptEnum.MFA_CODE_INVALID);
        }
        bucket.delete();
        return R.ok(buildLoginResult(userId, account));
    }

    private boolean consumeRecoveryCode(SysUserMfa entity, String code) {
        String stored = entity.getRecoveryCodesCiphertext();
        if (!StringUtils.hasText(stored)) {
            return false;
        }
        List<String> remain = new ArrayList<>(List.of(stored.split(",")));
        boolean matched = remain.removeIf(item -> item.equalsIgnoreCase(code.trim()));
        if (!matched) {
            return false;
        }
        entity.setRecoveryCodesCiphertext(String.join(",", remain));
        entity.setUpdatedTime(LocalDateTime.now());
        mfaMapper.updateById(entity);
        return true;
    }

    private LoginResultVO buildLoginResult(Long userId, String account) {
        List<SysUserTenant> binds = userTenantMapper.selectList(new LambdaQueryWrapper<SysUserTenant>()
                .eq(SysUserTenant::getUserId, userId)
                .orderByDesc(SysUserTenant::getPrefOrder)
                .orderByDesc(SysUserTenant::getLastUsed));
        List<Long> tenantIds = binds.stream().map(SysUserTenant::getTenantId).toList();
        List<TenantVO> vos = new ArrayList<>();
        if (!tenantIds.isEmpty()) {
            List<SysTenant> tenants = tenantMapper.selectList(new LambdaQueryWrapper<SysTenant>()
                    .in(SysTenant::getId, tenantIds));
            for (SysTenant tenant : tenants) {
                TenantVO vo = new TenantVO();
                vo.setId(tenant.getId() == null ? null : String.valueOf(tenant.getId()));
                vo.setName(tenant.getTenantName());
                vo.setIntro(tenant.getDescription());
                vo.setLogo(tenant.getLogo());
                vo.setTenantType(tenant.getTenantType() == null ? null : tenant.getTenantType().getCode());
                for (SysUserTenant bind : binds) {
                    if (bind.getTenantId().equals(tenant.getId())) {
                        vo.setIsDefault(bind.getIsDefault());
                        break;
                    }
                }
                vos.add(vo);
            }
        }
        String interactionCode = loginInteractionCodeService.issue(userId, account, "B");
        return new LoginResultVO(interactionCode, vos);
    }

    private SysUserMfa findByUser(Long userId) {
        return mfaMapper.selectOne(new LambdaQueryWrapper<SysUserMfa>()
                .eq(SysUserMfa::getUserId, userId)
                .last("limit 1"));
    }

    private List<String> generateRecoveryCodes() {
        SecureRandom random = new SecureRandom();
        List<String> codes = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            codes.add(String.format("%08X", random.nextInt()));
        }
        return codes;
    }
}
