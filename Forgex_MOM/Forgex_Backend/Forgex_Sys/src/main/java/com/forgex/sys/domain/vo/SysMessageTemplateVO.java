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
 * 消息模板详情VO
 * <p>包含主表信息、接收人配置、模板内容配置</p>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
public class SysMessageTemplateVO {
    
    /** 模板ID */
    private Long id;
    
    /** 模板编号 */
    private String templateCode;
    
    /** 模板名称 */
    private String templateName;
    
    /** 模板版本 */
    private String templateVersion;
    
    /** 消息类型(NOTICE=通知,WARNING=警告,ALARM=报警) */
    private String messageType;
    
    /** 状态(false=禁用,true=启用) */
    private Boolean status;
    
    /** 备注 */
    private String remark;
    
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    
    /** 创建人 */
    private String createBy;
    
    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
    
    /** 更新人 */
    private String updateBy;
    
    /** 接收人配置列表 */
    private List<ReceiverVO> receivers;
    
    /** 模板内容配置列表 */
    private List<ContentVO> contents;
    
    /**
     * 接收人配置VO
     */
    @Data
    public static class ReceiverVO {
        /** ID */
        private Long id;
        
        /** 接收类型(ROLE=角色,DEPT=部门,POSITION=职位,USER=指定人) */
        private String receiverType;
        
        /** 接收人ID列表 */
        private List<Long> receiverIds;
    }
    
    /**
     * 模板内容配置VO
     */
    @Data
    public static class ContentVO {
        /** ID */
        private Long id;
        
        /** 消息平台(INTERNAL=站内,WECHAT=企业微信,SMS=短信,EMAIL=邮箱) */
        private String platform;
        
        /** 消息标题(支持占位符) */
        private String contentTitle;
        
        /** 消息内容(支持占位符) */
        private String contentBody;
        
        /** 跳转链接 */
        private String linkUrl;
    }
}



