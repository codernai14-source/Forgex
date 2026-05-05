package com.forgex.common.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户thirdpartypull结果数据传输对象。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
public class UserThirdPartyPullResultDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer totalCount = 0;

    private Integer createdCount = 0;

    private Integer updatedCount = 0;

    private List<String> failedAccounts = new ArrayList<>();
}
