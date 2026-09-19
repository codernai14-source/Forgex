package com.forgex.sys.domain.dto;

import lombok.Data;

/**
 * 消息已读DTO
 * <p>标记消息已读的请求参数。</p>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
public class SysMessageReadDTO {
    /**
     * 消息ID
     */
    private Long id;
}

