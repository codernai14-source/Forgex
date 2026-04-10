package com.forgex.sys.domain.param;

import com.forgex.common.base.BaseGetParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DictPageParam extends BaseGetParam {

    private String dictCode;

    private String dictName;

    private Long moduleId;
}
