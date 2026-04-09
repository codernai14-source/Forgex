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

/**
 * 娑堟伅妯℃澘涓昏〃瀹炰綋
 * <p>
 * 鏄犲皠琛細sys_message_template
 * 鐢ㄤ簬閰嶇疆娑堟伅妯℃澘鐨勫熀鏈俊鎭?
 * </p>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
@TableName("sys_message_template")
public class SysMessageTemplate extends BaseEntity {
    
    /** 妯℃澘缂栧彿 */
    private String templateCode;
    
    /** 妯℃澘鍚嶇О */
    private String templateName;
    
    /** 妯℃澘鍚嶇О澶氳瑷€JSON */
    private String templateNameI18nJson;
    
    /** 妯℃澘鐗堟湰 */
    private String templateVersion;
    
    /** 娑堟伅绫诲瀷(NOTICE=閫氱煡,WARNING=璀﹀憡,ALARM=鎶ヨ) */
    private String messageType;

    /** 娑撴艾濮熺猾璇茬€烽敍宀€鏁ゆ禍搴″隘閸掑棔绗夐崥宀勩€夐棃顫瑢閸旂喕鍏橀惃鍕Х閹垱膩閺?*/
    private String bizType;
    
    /** 鐘舵€?false=绂佺敤,true=鍚敤) */
    private Boolean status;
    
    /** 澶囨敞 */
    private String remark;
}

