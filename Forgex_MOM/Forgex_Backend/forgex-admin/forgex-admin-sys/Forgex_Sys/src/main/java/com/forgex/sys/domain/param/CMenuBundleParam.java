/*
 * Copyright 2026 coder_nai@163.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.forgex.sys.domain.param;

import lombok.Data;

/**
 * C 端菜单聚合包查询参数
 *
 * @author Forgex Team
 * @since 2026-09-19
 *
 * @version 1.0.0
 */
@Data
public class CMenuBundleParam {

    /**
     * 设备类型：MOBILE=手机/PDA, TABLET=Pad；空或非法值按 MOBILE 处理，
     * 匹配 device_type IN (入参, 'ALL')。
     */
    private String deviceType;
}
