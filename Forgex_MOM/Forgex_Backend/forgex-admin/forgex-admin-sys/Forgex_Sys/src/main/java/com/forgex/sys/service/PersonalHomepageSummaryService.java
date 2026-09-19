/*Copyright 2026 coder_nai@163.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package com.forgex.sys.service;

import com.forgex.sys.domain.vo.PersonalHomepageSummaryVO;

/**
 * 个人首页摘要信息服务接口。
 * <p>
 * 提供个人首页顶部用户卡片信息的查询功能，
 * 包括头像、昵称、在线时长和问候语。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-06
 */
public interface PersonalHomepageSummaryService {

    /**
     * 获取个人首页摘要信息。
     * <p>
     * 查询用户的头像、昵称，计算在线时长，生成问候语。
     * </p>
     *
     * @param userId   用户ID，不能为空
     * @param tenantId 租户ID，不能为空
     * @return 摘要信息VO
     * @throws IllegalArgumentException 当 userId 或 tenantId 为空时抛出
     */
    PersonalHomepageSummaryVO getSummary(Long userId, Long tenantId);
}