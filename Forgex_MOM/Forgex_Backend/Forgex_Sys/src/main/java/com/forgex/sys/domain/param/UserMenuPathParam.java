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
 * 用户菜单路径参数类。
 * <p>
 * 用于菜单收藏切换、访问上报等接口接收前端传入的菜单完整路径。
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-12
 */
@Data
public class UserMenuPathParam {

    /**
     * 前端完整菜单路径，例如 /workspace/sys/authorization/role。
     * 服务端会基于该路径定位当前用户有权限访问的菜单。
     */
    private String path;
}

