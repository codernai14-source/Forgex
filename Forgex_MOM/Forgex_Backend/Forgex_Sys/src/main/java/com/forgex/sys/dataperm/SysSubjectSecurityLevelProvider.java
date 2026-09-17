package com.forgex.sys.dataperm;

import com.forgex.common.dataperm.SubjectSecurityLevelProvider;
import com.forgex.common.security.SecurityLabel;
import com.forgex.common.security.compliance.ComplianceContext;
import com.forgex.common.security.compliance.ComplianceLevel;
import com.forgex.sys.domain.entity.SysUser;
import com.forgex.sys.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 从用户档案读取主体密级。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
public class SysSubjectSecurityLevelProvider implements SubjectSecurityLevelProvider {

    private final SysUserMapper userMapper;

    /**
     * L3 才启用强制访问控制。
     *
     * @return true 表示启用
     */
    @Override
    public boolean mandatoryAccessEnabled() {
        return ComplianceContext.atLeast(ComplianceLevel.L3);
    }

    /**
     * 解析用户密级，缺失时按公开处理。
     *
     * @param userId 用户 ID
     * @return 密级数值
     */
    @Override
    public int resolve(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null || user.getSecurityLevel() == null) {
            return SecurityLabel.PUBLIC.getLevel();
        }
        return user.getSecurityLevel();
    }
}
