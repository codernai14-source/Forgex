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

package com.forgex.sys.domain.vo;

import lombok.Data;

/**
 * 用户菜单打开上报结果。
 * <p>
 * 前端根据 firstOpen 判断是否需要启动当前菜单的引导。
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-29
 */
@Data
public class UserMenuOpenReportVO {

    /**
     * 菜单完整路径。
     */
    private String path;

    /**
     * 累计打开次数。
     */
    private Integer openCount;

    /**
     * 是否首次打开。
     */
    private Boolean firstOpen;
}
