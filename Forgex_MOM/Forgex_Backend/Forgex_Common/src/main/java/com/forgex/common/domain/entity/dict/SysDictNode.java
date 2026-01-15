package com.forgex.common.domain.entity.dict;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;

@Data
@TableName("sys_dict")
public class SysDictNode extends BaseEntity {
    private Long parentId;
    private String dictName;
    private String dictCode;
    private String dictValue;
    private String dictValueI18nJson;
    private String nodePath;
    private Integer level;
    private Integer childrenCount;
    private Integer orderNum;
    private Integer status;
    private String remark;
}

