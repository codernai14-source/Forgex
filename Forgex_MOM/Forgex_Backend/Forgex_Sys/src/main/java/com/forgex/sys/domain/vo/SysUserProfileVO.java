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

import com.forgex.common.base.BaseEntity;
import com.forgex.common.dict.DictI18n;
import lombok.Data;

import java.util.List;

/**
 * 用户附属信息视图对象
 * 用于向前端返回用户扩展信息，包含展示需要的衍生字段
 *
 * @author coder_nai
 * @version 1.0.0
 */
@Data
public class SysUserProfileVO extends BaseEntity {
    
    /** 用户ID */
    private Long userId;
    
    /** 政治面貌 */
    @DictI18n(nodePathConst = "political_status", targetField = "politicalStatusText")
    private String politicalStatus;
    
    /** 政治面貌文本（字典翻译结果） */
    private String politicalStatusText;
    
    /** 家庭住址 */
    private String homeAddress;
    
    /** 紧急联系人 */
    private String emergencyContact;
    
    /** 紧急联系人电话 */
    private String emergencyPhone;
    
    /** 引荐人 */
    private String referrer;
    
    /** 学历 */
    @DictI18n(nodePathConst = "education", targetField = "educationText")
    private String education;
    
    /** 学历文本（字典翻译结果） */
    private String educationText;

    /** 籍贯 */
    private String birthPlace;

    /** 个人简介 */
    private String intro;

    /** 工作经历（JSON格式） */
    private List<WorkHistory> workHistory;
    
    /**
     * 工作经历内部类
     */
    @Data
    public static class WorkHistory {
        /** 公司名称 */
        private String company;
        /** 职位 */
        private String position;
        /** 开始时间 */
        private String startDate;
        /** 结束时间 */
        private String endDate;
        /** 工作描述 */
        private String description;
    }
}