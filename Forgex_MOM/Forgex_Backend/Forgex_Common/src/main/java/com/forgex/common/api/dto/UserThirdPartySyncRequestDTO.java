package com.forgex.common.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 用户thirdparty同步请求数据传输对象。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
public class UserThirdPartySyncRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long tenantId;

    private List<UserThirdPartySyncDTO> users;
}
