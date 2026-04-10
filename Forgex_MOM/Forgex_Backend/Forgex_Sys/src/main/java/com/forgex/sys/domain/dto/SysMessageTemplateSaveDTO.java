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
package com.forgex.sys.domain.dto;

import lombok.Data;
import java.util.List;

/**
 * 消息模板保存 DTO
 * <p>包含主表信息、接收人配置、模板内容配置</p>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
public class SysMessageTemplateSaveDTO {
    
    /** 模板 ID(修改时传入) */
    private Long id;
    
    /** 模板编号 */
    private String templateCode;
    
    /** 模板名称 */
    private String templateName;

    private String templateNameI18nJson;
    
    /** 模板版本 */
    private String templateVersion;
    
    /** 消息类型 (NOTICE=通知，WARNING=警告，ALARM=报警) */
    private String messageType;
    
    /** 业务类型 */
    private String bizType;
    
    /** 状态 (false=禁用，true=启用) */
    private Boolean status;
    
    /** 备注 */
    private String remark;
    
    /** 是否公共配置 (true=tenantId=0) */
    private Boolean publicConfig;
    
    /** 接收人配置列表 */
    private List<ReceiverConfig> receivers;
    
    /** 模板内容配置列表 */
    private List<ContentConfig> contents;
    
    /**
     * 接收人配置
     */
    @Data
    public static class ReceiverConfig {
        /** 接收类型 (ROLE=角色，DEPT=部门，POSITION=职位，USER=指定人，CUSTOM=自定义) */
        private String receiverType;
        
        /** 接收人 ID 列表 */
        private List<Long> receiverIds;
    }
    
    /**
     * 模板内容配置
     */
    @Data
    public static class ContentConfig {
        /** 消息平台 (INTERNAL=站内，WECHAT=企业微信，SMS=短信，EMAIL=邮箱) */
        private String platform;
        
        /** 消息标题 (支持占位符) */
        private String contentTitle;

        private String contentTitleI18nJson;
        
        /** 消息内容 (支持占位符) */
        private String contentBody;

        private String contentBodyI18nJson;
        
        /** 跳转链接 */
        private String linkUrl;
    }
}
