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
 * 娑堟伅妯℃澘鏌ヨ鍙傛暟
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysMessageTemplateParam extends BaseGetParam {
    
    /** 妯℃澘缂栧彿(妯＄硦鏌ヨ) */
    private String templateCode;
    
    /** 妯℃澘鍚嶇О(妯＄硦鏌ヨ) */
    private String templateName;
    
    /** 娑堟伅绫诲瀷 */
    private String messageType;
    
    /** 娑撴艾濮熺猾璇茬€?*/
    private String bizType;
    
    /** 鐘舵€?*/
    private Boolean status;
    
    /** 閺勵垰鎯侀弻銉嚄閸忣剙鍙￠柊宥囩枂閿涘rue=tenantId=0 */
    private Boolean publicConfig;
}



