package com.forgex.common.spi;

import com.forgex.common.api.dto.UserInfoDTO;
import com.forgex.common.web.R;
import java.util.List;
import java.util.Map;

/** 用户目录扩展接口，由接入方提供本地或远程实现。 */
public interface UserDirectory {
    /** @param userId 用户 ID @return 用户信息 */
    R<UserInfoDTO> getUserById(Long userId);
    /** @param account 用户账号 @return 用户信息 */
    R<UserInfoDTO> getUserByAccount(String account);
    /** @param userIds 用户 ID 列表 @return 用户信息列表 */
    R<List<UserInfoDTO>> getUsersByIds(List<Long> userIds);
    /** @param userIds 用户 ID 列表 @return 用户名映射 */
    R<Map<Long, String>> getUsernameMap(List<Long> userIds);
}
