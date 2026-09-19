package com.forgex.admin.client;

import com.forgex.common.api.dto.UserInfoDTO;
import com.forgex.common.api.feign.SysUserFeignClient;
import com.forgex.common.spi.UserDirectory;
import com.forgex.common.web.R;
import java.util.List;
import java.util.Map;

/** 通过平台用户 API 实现公共用户目录，不暴露平台数据库。 */
public class FeignUserDirectory implements UserDirectory {
    private final SysUserFeignClient client;

    /** @param client 平台用户客户端 */
    public FeignUserDirectory(SysUserFeignClient client) { this.client = client; }

    /** {@inheritDoc} */
    @Override public R<UserInfoDTO> getUserById(Long userId) { return client.getUserById(userId); }
    /** {@inheritDoc} */
    @Override public R<UserInfoDTO> getUserByAccount(String account) { return client.getUserByAccount(account); }
    /** {@inheritDoc} */
    @Override public R<List<UserInfoDTO>> getUsersByIds(List<Long> userIds) { return client.getUsersByIds(userIds); }
    /** {@inheritDoc} */
    @Override public R<Map<Long, String>> getUsernameMap(List<Long> userIds) { return client.getUsernameMap(userIds); }
}
