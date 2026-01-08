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
package com.forgex.sys.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;

@Data
@TableName("sys_menu")
public class SysMenu extends BaseEntity {
    private Long moduleId;
    private Long parentId;
    private String type;
    private Integer menuLevel;  // 菜单层级：1=一级菜单(目录), 2=二级菜单, 3=三级菜单
    private String path;
    private String name;
    private String icon;
    private String componentKey;
    private String permKey;
    private String menuMode;    // 菜单模式：embedded=内嵌，external=外联
    private String externalUrl; // 外联URL
    private Integer orderNum;
    private Integer visible;
    private Integer status;
}
