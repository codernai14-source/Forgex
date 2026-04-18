package com.forgex.integration.domain.param;

import lombok.Data;

/**
 * 接口参数 JSON 导入参数
 * <p>
 * 用于接收前端粘贴 JSON 后的预览确认导入请求。
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-17
 */
@Data
public class ApiParamJsonImportParam {

    /**
     * 接口配置 ID
     */
    private Long apiConfigId;

    /**
     * 参数方向：REQUEST/RESPONSE
     */
    private String direction;

    /**
     * JSON 文本，兼容前端当前 jsonString 字段
     */
    private String jsonString;

    /**
     * JSON 文本，兼容方案中的 jsonText 字段
     */
    private String jsonText;

    /**
     * 获取实际 JSON 文本
     *
     * @return JSON 文本
     */
    public String getEffectiveJsonText() {
        return jsonString != null ? jsonString : jsonText;
    }
}
