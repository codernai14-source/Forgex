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

/**
 * 标签样式配置视图对象
 * 用于字典值标签的样式配置，支持颜色和图标配置
 * 
 * @author coder_nai@163.com
 * @date 2026-01-16
 * @version 1.0.0
 */
@Data
public class TagStyleVO {

    /**
     * 标签颜色
     * 支持状态颜色：success（成功）、processing（进行中）、error（错误）、warning（警告）、default（默认）
     * 支持预设颜色：pink（粉色）、red（红色）、orange（橙色）、green（绿色）、cyan（青色）、blue（蓝色）、purple（紫色）
     * 支持自定义颜色：如 #f50、#2db7f5、#87d068、#108ee9 等
     *
     * @see <a href="https://www.antdv.com/components/tag-cn#api">Ant Design Vue Tag API</a>
     */
    private String color;

    /**
     * 图标名称
     * 使用 Ant Design Icons 图标组件名称
     * 常用图标：CheckCircleOutlined（成功）、CloseCircleOutlined（错误）、ExclamationCircleOutlined（警告）、SyncOutlined（加载）、ClockCircleOutlined（时钟）等
     *
     * @see <a href="https://www.antdv.com/components/icon-cn">Ant Design Vue Icons</a>
     */
    private String icon;
}