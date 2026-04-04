package com.forgex.auth.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_role")
public class SysRolePerm {

    private Long id;

    private Long tenantId;

    private String roleKey;

    private Boolean deleted;
}
