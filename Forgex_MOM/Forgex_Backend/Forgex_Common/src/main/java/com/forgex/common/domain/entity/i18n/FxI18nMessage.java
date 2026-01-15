package com.forgex.common.domain.entity.i18n;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;

@Data
@TableName("fx_i18n_message")
public class FxI18nMessage extends BaseEntity {
    private String module;
    private String promptCode;
    private String textI18nJson;
    private Boolean enabled;
    private Integer version;
}

