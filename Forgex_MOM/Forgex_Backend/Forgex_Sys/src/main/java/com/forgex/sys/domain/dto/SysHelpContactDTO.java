package com.forgex.sys.domain.dto;

import lombok.Data;

/**
 * 帮助中心联系方式配置。
 * <p>
 * 持久化在 {@code sys_config}，键为 {@code system.help.contact}。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-09-11
 */
@Data
public class SysHelpContactDTO {

    /** 联系电话。 */
    private String phone = "";

    /** 联系邮箱。 */
    private String email = "";

    /** 企业微信或其他二维码图片地址。 */
    private String wechatQrUrl = "";

    /** 联系地址。 */
    private String address = "";

    /** 工作时间说明。 */
    private String workTime = "";

    /** 补充说明。 */
    private String remark = "";
}
