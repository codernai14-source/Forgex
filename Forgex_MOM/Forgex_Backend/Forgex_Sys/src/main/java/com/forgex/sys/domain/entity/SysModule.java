package com.forgex.sys.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;

@Data
@TableName("sys_module")
public class SysModule extends BaseEntity {
    private String code;
    private String name;
    private String icon;
    private Integer orderNum;
    private Integer visible;
    private Integer status;
}
