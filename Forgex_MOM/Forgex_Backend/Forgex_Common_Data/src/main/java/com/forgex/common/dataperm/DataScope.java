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
package com.forgex.common.dataperm;

/**
 * 数据权限范围枚举
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
public enum DataScope {
    
    /**
     * 全部数据权限
     */
    ALL("ALL", "全部数据"),
    
    /**
     * 本部门及下级部门数据权限
     */
    DEPT_AND_CHILD("DEPT_AND_CHILD", "本部门及下级"),
    
    /**
     * 本部门数据权限
     */
    DEPT("DEPT", "本部门"),
    
    /**
     * 仅本人数据权限
     */
    SELF("SELF", "仅本人"),
    
    /**
     * 自定义数据权限
     */
    CUSTOM("CUSTOM", "自定义");
    
    private final String code;
    private final String name;
    
    DataScope(String code, String name) {
        this.code = code;
        this.name = name;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getName() {
        return name;
    }
    
    /**
     * 根据代码获取枚举
     */
    public static DataScope fromCode(String code) {
        for (DataScope scope : values()) {
            if (scope.code.equals(code)) {
                return scope;
            }
        }
        return SELF; // 默认返回仅本人
    }
}

