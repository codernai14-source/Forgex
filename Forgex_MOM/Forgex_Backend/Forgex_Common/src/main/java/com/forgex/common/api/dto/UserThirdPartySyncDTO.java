package com.forgex.common.api.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserThirdPartySyncDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long tenantId;

    private String account;

    private String username;

    private String email;

    private String phone;

    private Boolean status;

    private Integer gender;

    private String avatar;

    private Long departmentId;

    private Long positionId;

    private Long employeeId;

    private Integer userSource;
}
