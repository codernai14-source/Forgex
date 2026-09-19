// 3. 打印异常查询参数
package com.forgex.basic.label.domain.param;

import com.forgex.common.base.BaseGetParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "打印异常查询参数")
public class PrintExceptionQueryParam extends BaseGetParam {

    @Schema(description = "错误代码")
    private String errorCode;

    @Schema(description = "开始时间")
    private String printTimeStart;

    @Schema(description = "结束时间")
    private String printTimeEnd;
}
