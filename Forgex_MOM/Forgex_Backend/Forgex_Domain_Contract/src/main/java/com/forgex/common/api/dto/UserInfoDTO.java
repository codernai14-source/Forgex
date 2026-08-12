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
package com.forgex.common.api.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 用户信息 DTO
 * <p>用于 Feign 调用返回的用户基本信息</p>
 * 
 * @author coder_nai@163.com
 * @date 2026-01-27
 */
@Data
public class UserInfoDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 用户ID */
    private Long id;
    
    /** 账号 */
    private String account;
    
    /** 用户名（显示名） */
    private String username;
    
    /** 邮箱 */
    private String email;
    
    /** 手机号 */
    private String phone;
    
    /** 性别（0=未知，1=男，2=女） */
    private Integer gender;
    
    /** 头像URL */
    private String avatar;
    
    /** 所属部门ID */
    private Long departmentId;
    
    /** 职位ID */
    private Long positionId;
    
    /** 状态：false=禁用，true=启用 */
    private Boolean status;
}



