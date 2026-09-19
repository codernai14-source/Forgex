// 4. 打印异常 VO
package com.forgex.basic.label.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "打印异常 VO")
public class PrintExceptionVO {

    @Schema(description = "异常 ID")
    private Long id;

    @Schema(description = "错误代码")
    private String errorCode;

    @Schema(description = "错误消息")
    private String errorMessage;

    @Schema(description = "打印记录 ID")
    private Long printRecordId;

    @Schema(description = "模板 ID")
    private Long templateId;

    @Schema(description = "工厂 ID")
    private Long factoryId;

    @Schema(description = "操作人 ID")
    private Long operatorId;

    @Schema(description = "异常发生时间")
    private LocalDateTime exceptionTime;

    @Schema(description = "堆栈信息")
    private String stackTrace;

    @Schema(description = "租户 ID")
    private Long tenantId;
}
