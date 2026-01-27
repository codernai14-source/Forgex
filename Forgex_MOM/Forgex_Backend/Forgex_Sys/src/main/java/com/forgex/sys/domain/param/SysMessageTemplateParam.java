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
package com.forgex.sys.domain.param;

import com.forgex.common.base.BaseGetParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消息模板查询参数
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysMessageTemplateParam extends BaseGetParam {
    
    /** 模板编号(模糊查询) */
    private String templateCode;
    
    /** 模板名称(模糊查询) */
    private String templateName;
    
    /** 消息类型 */
    private String messageType;
    
    /** 状态 */
    private Boolean status;
}



