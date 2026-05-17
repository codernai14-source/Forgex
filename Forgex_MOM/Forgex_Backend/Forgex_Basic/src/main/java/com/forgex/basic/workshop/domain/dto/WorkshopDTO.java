package com.forgex.basic.workshop.domain.dto;

import com.forgex.basic.workshop.domain.entity.BasicWorkshop;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 车间主数据 DTO。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WorkshopDTO extends BasicWorkshop {

    /** 工厂编码。 */
    private String factoryCode;

    /** 工厂名称。 */
    private String factoryName;
}
