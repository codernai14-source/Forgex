package com.forgex.common.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class UserThirdPartySyncRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long tenantId;

    private List<UserThirdPartySyncDTO> users;
}
