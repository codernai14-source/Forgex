package com.forgex.sys.domain.vo;

import lombok.Data;

/** 在线用户的单个有效会话明细。 */
@Data
public class OnlineSessionVO {

    private String token;

    private String loginTerminal;

    private String clientIp;

    private String loginRegion;

    private String loginTime;

    private Long ttlSeconds;
}
