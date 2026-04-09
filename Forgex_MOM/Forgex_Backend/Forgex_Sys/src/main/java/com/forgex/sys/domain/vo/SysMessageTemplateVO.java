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

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 娑堟伅妯℃澘璇︽儏VO
 * <p>鍖呭惈涓昏〃淇℃伅銆佹帴鏀朵汉閰嶇疆銆佹ā鏉垮唴瀹归厤缃?/p>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
public class SysMessageTemplateVO {
    
    /** 妯℃澘ID */
    private Long id;
    
    /** 妯℃澘缂栧彿 */
    private String templateCode;
    
    /** 妯℃澘鍚嶇О */
    private String templateName;

    private String templateNameI18nJson;
    
    /** 妯℃澘鐗堟湰 */
    private String templateVersion;
    
    /** 娑堟伅绫诲瀷(NOTICE=閫氱煡,WARNING=璀﹀憡,ALARM=鎶ヨ) */
    private String messageType;
    
    /** 娑撴艾濮熺猾璇茬€?*/
    private String bizType;
    
    /** 鐘舵€?false=绂佺敤,true=鍚敤) */
    private Boolean status;
    
    /** 澶囨敞 */
    private String remark;
    
    /** 閺勵垰鎯侀崗顒€鍙￠柊宥囩枂閿涘rue=tenantId=0 */
    private Boolean publicConfig;
    
    /** 鍒涘缓鏃堕棿 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    
    /** 鍒涘缓浜?*/
    private String createBy;
    
    /** 鏇存柊鏃堕棿 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
    
    /** 鏇存柊浜?*/
    private String updateBy;
    
    /** 鎺ユ敹浜洪厤缃垪琛?*/
    private List<ReceiverVO> receivers;
    
    /** 妯℃澘鍐呭閰嶇疆鍒楄〃 */
    private List<ContentVO> contents;
    
    /**
     * 鎺ユ敹浜洪厤缃甐O
     */
    @Data
    public static class ReceiverVO {
        /** ID */
        private Long id;
        
        /** 鎺ユ敹绫诲瀷(ROLE=瑙掕壊,DEPT=閮ㄩ棬,POSITION=鑱屼綅,USER=鎸囧畾浜? */
        private String receiverType;
        
        /** 鎺ユ敹浜篒D鍒楄〃 */
        private List<Long> receiverIds;
    }
    
    /**
     * 妯℃澘鍐呭閰嶇疆VO
     */
    @Data
    public static class ContentVO {
        /** ID */
        private Long id;
        
        /** 娑堟伅骞冲彴(INTERNAL=绔欏唴,WECHAT=浼佷笟寰俊,SMS=鐭俊,EMAIL=閭) */
        private String platform;
        
        /** 娑堟伅鏍囬(鏀寔鍗犱綅绗? */
        private String contentTitle;

        private String contentTitleI18nJson;
        
        /** 娑堟伅鍐呭(鏀寔鍗犱綅绗? */
        private String contentBody;

        private String contentBodyI18nJson;
        
        /** 璺宠浆閾炬帴 */
        private String linkUrl;
    }
}



