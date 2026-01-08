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
package com.forgex.sys.domain.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 用户路由VO
 * 
 * @author Forgex Team
 * @date 2025-01-07
 */
@Data
public class UserRoutesVO {
    
    /**
     * 模块列表
     */
    private List<Map<String, Object>> modules;
    
    /**
     * 路由列表
     */
    private List<Map<String, Object>> routes;
    
    /**
     * 按钮权限列表
     */
    private List<String> buttons;
}
