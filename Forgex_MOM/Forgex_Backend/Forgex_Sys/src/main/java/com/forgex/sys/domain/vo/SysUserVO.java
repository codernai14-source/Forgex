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
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户视图对象
 * 用于向前端返回用户数据，包含展示需要的衍生字段
 *
 * @author coder_nai
 * @version 1.0.0
 */
@Data
public class SysUserVO extends BaseEntity {

    /** 账号 */
    private String account;

    /** 显示名 */
    private String username;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String phone;

    /** 性别：0=未知，1=男，2=女 */
    @DictI18n(nodePathConst = "/gender", targetField = "genderText")
    private Integer gender;
    
    /** 性别文本（字典翻译结果） */
    private String genderText;

    /** 头像URL */
    private String avatar;

    /** 入职时间 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate entryDate;

    /** 所属部门ID */
    private Long departmentId;
    
    /** 所属部门名称（关联查询结果） */
    private String departmentName;

    /** 职位ID */
    private Long positionId;
    
    /** 职位名称（关联查询结果） */
    private String positionName;

    private Long employeeId;

    @DictI18n(nodePathConst = "user_source", targetField = "userSourceText")
    private Integer userSource;

    private String userSourceText;

    /** 当前租户下角色ID列表 */
    private List<Long> roleIds;

    /** 当前租户下角色名称列表 */
    private List<String> roleNames;

    /** 状态：false=禁用，true=启用 */
    @DictI18n(nodePathConst = "status", targetField = "statusText")
    private Boolean status;
    
    /** 状态文本（字典翻译结果） */
    private String statusText;
    
    /** 最后登录IP */
    private String lastLoginIp;
    
    /** 最后登录地区 */
    private String lastLoginRegion;
    
    /** 最后登录时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastLoginTime;
}
