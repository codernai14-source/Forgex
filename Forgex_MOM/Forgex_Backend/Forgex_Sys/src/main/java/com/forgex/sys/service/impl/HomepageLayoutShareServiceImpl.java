package com.forgex.sys.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forgex.common.domain.config.PersonalHomepageConfig;
import com.forgex.sys.domain.entity.SysHomepageLayoutShare;
import com.forgex.sys.domain.vo.HomepageLayoutShareVO;
import com.forgex.sys.mapper.SysHomepageLayoutShareMapper;
import com.forgex.sys.service.HomepageLayoutShareService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.util.Locale;

/**
 * 首页布局分享码服务实现。
 */
@Service
@RequiredArgsConstructor
public class HomepageLayoutShareServiceImpl implements HomepageLayoutShareService {

    private static final String DEFAULT_MODULE_CODE = "personal";

    private static final char[] SHARE_CODE_CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray();

    private static final int SHARE_CODE_LENGTH = 10;

    private static final int MAX_GENERATE_RETRY = 8;

    private final SecureRandom secureRandom = new SecureRandom();

    private final SysHomepageLayoutShareMapper layoutShareMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HomepageLayoutShareVO createShare(Long tenantId, String moduleCode, PersonalHomepageConfig config) {
        String normalizedModuleCode = normalizeModuleCode(moduleCode);
        for (int index = 0; index < MAX_GENERATE_RETRY; index++) {
            SysHomepageLayoutShare share = new SysHomepageLayoutShare();
            share.setTenantId(tenantId);
            share.setShareCode(generateShareCode());
            share.setModuleCode(normalizedModuleCode);
            share.setConfigJson(JSONUtil.toJsonStr(config));
            share.setDeleted(Boolean.FALSE);

            try {
                layoutShareMapper.insert(share);
                return toVO(share);
            } catch (DuplicateKeyException ignored) {
                // 分享码碰撞概率极低，发生时重新生成。
            }
        }
        throw new IllegalStateException("Failed to generate homepage layout share code.");
    }

    @Override
    public HomepageLayoutShareVO previewShare(Long tenantId, String shareCode, String moduleCode) {
        if (tenantId == null || !StringUtils.hasText(shareCode)) {
            return null;
        }
        LambdaQueryWrapper<SysHomepageLayoutShare> wrapper = new LambdaQueryWrapper<SysHomepageLayoutShare>()
                .eq(SysHomepageLayoutShare::getTenantId, tenantId)
                .eq(SysHomepageLayoutShare::getShareCode, shareCode.trim().toUpperCase(Locale.ROOT));
        if (StringUtils.hasText(moduleCode)) {
            wrapper.eq(SysHomepageLayoutShare::getModuleCode, normalizeModuleCode(moduleCode));
        }
        wrapper.last("limit 1");
        return toVO(layoutShareMapper.selectOne(wrapper));
    }

    private String generateShareCode() {
        StringBuilder builder = new StringBuilder(SHARE_CODE_LENGTH);
        for (int index = 0; index < SHARE_CODE_LENGTH; index++) {
            builder.append(SHARE_CODE_CHARS[secureRandom.nextInt(SHARE_CODE_CHARS.length)]);
        }
        return builder.toString();
    }

    private String normalizeModuleCode(String moduleCode) {
        if (!StringUtils.hasText(moduleCode)) {
            return DEFAULT_MODULE_CODE;
        }
        String normalized = moduleCode.trim().toLowerCase(Locale.ROOT);
        if ("system".equals(normalized)) {
            return "sys";
        }
        if ("workflow".equals(normalized)) {
            return "approval";
        }
        return normalized;
    }

    private HomepageLayoutShareVO toVO(SysHomepageLayoutShare share) {
        if (share == null) {
            return null;
        }
        HomepageLayoutShareVO vo = new HomepageLayoutShareVO();
        vo.setShareCode(share.getShareCode());
        vo.setModuleCode(share.getModuleCode());
        vo.setCreateTime(share.getCreateTime());
        vo.setConfig(JSONUtil.toBean(share.getConfigJson(), PersonalHomepageConfig.class));
        return vo;
    }
}
